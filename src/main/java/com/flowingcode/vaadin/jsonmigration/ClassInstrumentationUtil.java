/*-
 * #%L
 * Json Migration Helper
 * %%
 * Copyright (C) 2025 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.flowingcode.vaadin.jsonmigration;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import elemental.json.JsonValue;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for instrumenting classes at runtime.
 *
 * <p>This class provides methods to dynamically create subclasses of a given parent class using
 * bytecode instrumentation. Methods annotated with {@link ClientCallable} that return a type
 * assignable to {@link JsonValue} are automatically overridden to convert the result through {@link
 * JsonMigration#convertToClientCallableResult(JsonValue)}.
 *
 * @author Javier Godoy / Flowing Code
 */
final class ClassInstrumentationUtil {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final int version;

  private final Map<ClassLoader, InstrumentedClassLoader> classLoaderCache = new WeakHashMap<>();

  private static final boolean IS_ASM_PRESENT;

  static {
    boolean isPresent;
    try {
      Class.forName(
          "org.objectweb.asm.ClassWriter", false, ClassInstrumentationUtil.class.getClassLoader());
      isPresent = true;
    } catch (ClassNotFoundException e) {
      isPresent = false;
    }
    IS_ASM_PRESENT = isPresent;
  }

  ClassInstrumentationUtil(int version) {
    this.version = version;
  }

  /**
   * Creates and returns an instance of a dynamically instrumented class that extends the specified
   * parent class.
   *
   * <p>This method generates a new class at runtime that extends {@code parent}, and returns a new
   * instance of that generated class. The instrumented class will have a default constructor that
   * delegates to the parent's default constructor. All methods annotated with {@link
   * ClientCallable} that return a type assignable to {@link JsonValue} will be overridden to
   * convert the result via JsonMigration.convertClientCallableResult().
   *
   * <p><b>Requirements:</b>
   *
   * <ul>
   *   <li>The parent class must not be final
   *   <li>The parent class must have an accessible no-argument constructor
   * </ul>
   *
   * @param <T> the type of the parent class
   * @param parent the parent class to extend
   * @return a new instance of the instrumented class extending {@code parent}
   * @throws IllegalArgumentException if the parent class is final, an interface, a primitive type,
   *     an array type, or does not have an accessible no-argument constructor
   * @throws RuntimeException if the instrumentation or instantiation fails
   */
  public <T extends Component> Class<? extends T> instrumentClass(Class<T> parent) {
    // Validate input
    if (parent == null) {
      throw new IllegalArgumentException("Parent class cannot be null");
    }

    if (parent.isInterface()) {
      throw new IllegalArgumentException("Cannot instrument an interface: " + parent.getName());
    }

    if (parent.isPrimitive()) {
      throw new IllegalArgumentException("Cannot instrument a primitive type: " + parent.getName());
    }

    if (parent.isArray()) {
      throw new IllegalArgumentException("Cannot instrument an array type: " + parent.getName());
    }

    if (Modifier.isFinal(parent.getModifiers())) {
      throw new IllegalArgumentException("Cannot instrument a final class: " + parent.getName());
    }

    if (!needsInstrumentation(parent)) {
      logger.info("{} no instrumentation needed", parent);
      return parent;
    }

    if (!IS_ASM_PRESENT) {
      throw new IllegalStateException("Missing optional dependency org.ow2.asm:asm:9.8");
    }

    // Check for accessible no-arg constructor
    try {
      Constructor<?> defaultConstructor = parent.getDeclaredConstructor();
      if (!Modifier.isPublic(defaultConstructor.getModifiers())
          && !Modifier.isProtected(defaultConstructor.getModifiers())) {
        try {
          defaultConstructor.setAccessible(true);
        } catch (Exception e) {
          throw new IllegalArgumentException(
              "Parent class must have an accessible no-argument constructor: " + parent.getName(),
              e);
        }
      }
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(
          "Parent class must have a no-argument constructor: " + parent.getName(), e);
    }

    try {
      String instrumentedClassName = parent.getName() + "$Instrumented";
      return createInstrumentedClass(parent, instrumentedClassName);
    } catch (Exception e) {
      throw new RuntimeException("Failed to instrument " + parent.getName(), e);
    }
  }

  private boolean needsInstrumentation(Class<?> parent) {
    return !getInstrumentableMethods(parent).isEmpty();
  }

  private boolean hasLegacyVaadin() {
    return version <= 24;
  }

  private static Stream<Method> getDeclaredCallables(Class<?> clazz) {
    return Stream.of(clazz.getDeclaredMethods())
        .filter(
            method -> {
              int modifiers = method.getModifiers();
              if (!Modifier.isStatic(modifiers)) {
                boolean isCallable = method.isAnnotationPresent(ClientCallable.class);
                boolean isLegacyCallable = method.isAnnotationPresent(LegacyClientCallable.class);
                return isCallable || isLegacyCallable;
              }
              return false;
            });
  }

  private static Stream<Method> getAllCallables(Class<?> baseClass) {
    Map<String, Method> map = new HashMap<>();
    for (Class<?> clazz = baseClass; clazz != Component.class; clazz = clazz.getSuperclass()) {
      getDeclaredCallables(clazz)
          .forEach(
              method -> {
                Method existing = map.get(method.getName());
                if (existing == null) {
                  map.put(method.getName(), method);
                } else if (!Arrays.equals(
                    existing.getParameterTypes(), method.getParameterTypes())) {
                  String msg =
                      String.format(
                          "There may be only one handler method with the given name. "
                              + "Class '%s' (considering its superclasses) "
                              + "contains several handler methods with the same name: '%s'",
                          baseClass.getName(), method.getName());
                  throw new IllegalStateException(msg);
                }
              });
    }
    return map.values().stream();
  }

  private List<Method> getInstrumentableMethods(Class<?> parent) {
    return getAllCallables(parent)
        .filter(
            method -> {
              boolean isCallable = method.isAnnotationPresent(ClientCallable.class);
              boolean isLegacyCallable = method.isAnnotationPresent(LegacyClientCallable.class);
              boolean hasJsonValueReturn = JsonValue.class.isAssignableFrom(method.getReturnType());
              boolean hasJsonValueParams = hasJsonValueParameters(method);

              if (isCallable && hasJsonValueParams) {
                throw new IllegalArgumentException(
                    String.format(
                        "Instrumented method '%s' in class '%s' has JsonValue arguments and must be annotated with @%s instead of @ClientCallable",
                        method.getName(),
                        method.getDeclaringClass(),
                        LegacyClientCallable.class.getSimpleName()));
              }

              if (hasLegacyVaadin()) {
                return isLegacyCallable;
              } else {
                return (isCallable && hasJsonValueReturn) || isLegacyCallable;
              }
            })
        .collect(Collectors.toList());
  }

  private static boolean hasJsonValueParameters(Method method) {
    for (Class<?> paramType : method.getParameterTypes()) {
      if (JsonValue.class.isAssignableFrom(paramType)) {
        return true;
      }
    }
    return false;
  }

  private <T extends Component> Class<? extends T> createInstrumentedClass(
      Class<T> parent, String className) throws Exception {
    InstrumentedClassLoader classLoader =
        getOrCreateInstrumentedClassLoader(parent.getClassLoader());
    return classLoader.defineInstrumentedClass(className, parent).asSubclass(parent);
  }

  private InstrumentedClassLoader getOrCreateInstrumentedClassLoader(ClassLoader parent) {
    synchronized (classLoaderCache) {
      return classLoaderCache.computeIfAbsent(parent, InstrumentedClassLoader::new);
    }
  }

  private final class InstrumentedClassLoader extends ClassLoader {

    private final Map<Class<?>, Class<?>> instrumentedClassCache = new ConcurrentHashMap<>();

    public InstrumentedClassLoader(ClassLoader parent) {
      super(parent);
    }

    public Class<?> defineInstrumentedClass(String className, Class<?> parent) {
      return instrumentedClassCache.computeIfAbsent(
          parent,
          p -> {
            byte[] bytecode = generateBytecode(className, p);
            return defineClass(className, bytecode, 0, bytecode.length);
          });
    }

    private byte[] generateBytecode(String className, Class<?> parent) {
      String internalClassName = className.replace('.', '/');
      String internalParentName = parent.getName().replace('.', '/');

      ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

      cw.visit(
          Opcodes.V1_8,
          Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
          internalClassName,
          null,
          internalParentName,
          null);

      generateConstructor(cw, internalParentName);
      generateClientCallableOverrides(cw, parent, internalClassName, internalParentName);

      cw.visitEnd();
      return cw.toByteArray();
    }

    private void generateConstructor(ClassWriter cw, String internalParentName) {
      MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
      mv.visitCode();
      mv.visitVarInsn(Opcodes.ALOAD, 0);
      mv.visitMethodInsn(Opcodes.INVOKESPECIAL, internalParentName, "<init>", "()V", false);
      mv.visitInsn(Opcodes.RETURN);
      mv.visitMaxs(0, 0);
      mv.visitEnd();
    }

    private void generateClientCallableOverrides(
        ClassWriter cw, Class<?> parent, String internalClassName, String internalParentName) {
      List<String> privateMethodNames = new ArrayList<>();
      for (Method method : getInstrumentableMethods(parent)) {
        if (Modifier.isPrivate(method.getModifiers())) {
          privateMethodNames.add(method.getName());
          createLookupHelper(cw, method);
          cw.visitField(
              Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
              method.getName(),
              Type.getDescriptor(MethodHandle.class),
              null,
              null);
        }
        generateMethodOverride(cw, method, internalClassName, internalParentName);
      }

      if (!privateMethodNames.isEmpty()) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        for (String name : privateMethodNames) {
          mv.visitMethodInsn(
              Opcodes.INVOKESTATIC,
              internalClassName,
              "lookup_" + name,
              "()" + Type.getDescriptor(MethodHandle.class),
              false);
          mv.visitFieldInsn(
              Opcodes.PUTSTATIC, internalClassName, name, "Ljava/lang/invoke/MethodHandle;");
        }
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
      }
    }

    private void createLookupHelper(ClassWriter cw, Method method) {
      MethodVisitor mv =
          cw.visitMethod(
              Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
              "lookup_" + method.getName(),
              "()" + Type.getDescriptor(MethodHandle.class),
              null,
              null);

      // Invoke static MethodHandles.lookup()
      mv.visitMethodInsn(
          Opcodes.INVOKESTATIC,
          "java/lang/invoke/MethodHandles",
          "lookup",
          "()Ljava/lang/invoke/MethodHandles$Lookup;",
          false);

      // Load the Owner class
      mv.visitLdcInsn(Type.getType(method.getDeclaringClass()));

      // Load the Method Name
      mv.visitLdcInsn(method.getName());

      // Create Class[] array
      Class<?> argTypes[] = method.getParameterTypes();
      pushInt(mv, (short) argTypes.length);
      mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class");

      // Load the specific Class objects and store in array
      for (short i = 0; i < argTypes.length; i++) {
        mv.visitInsn(Opcodes.DUP);
        pushInt(mv, i);
        loadClassConstant(mv, argTypes[i]);
        mv.visitInsn(Opcodes.AASTORE);
      }

      // Invoke getDeclaredMethod
      mv.visitMethodInsn(
          Opcodes.INVOKEVIRTUAL,
          "java/lang/Class",
          "getDeclaredMethod",
          "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
          false);

      // Invoke method.setAccessible(true)
      mv.visitInsn(Opcodes.DUP);
      mv.visitInsn(Opcodes.ICONST_1);
      mv.visitMethodInsn(
          Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "setAccessible", "(Z)V", false);

      // Invoke Lookup.unresolve(method)
      mv.visitMethodInsn(
          Opcodes.INVOKEVIRTUAL,
          "java/lang/invoke/MethodHandles$Lookup",
          "unreflect",
          "(Ljava/lang/reflect/Method;)Ljava/lang/invoke/MethodHandle;",
          false);

      // Return result
      mv.visitInsn(Opcodes.ARETURN);

      mv.visitMaxs(0, 0);
      mv.visitEnd();
    }

    private void generateMethodOverride(
        ClassWriter cw, Method method, String internalClassName, String internalParentName) {
      logger.info("Override {}", method);

      boolean hasJsonValueReturn =
          !hasLegacyVaadin() && JsonValue.class.isAssignableFrom(method.getReturnType());
      boolean hasJsonValueParams = !hasLegacyVaadin() && hasJsonValueParameters(method);

      String overrideDescriptor = getMethodDescriptor(method, hasJsonValueParams);
      String superDescriptor = getMethodDescriptor(method, false);
      int access =
          method.getModifiers()
              & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);

      MethodVisitor mv =
          cw.visitMethod(
              access,
              method.getName(),
              overrideDescriptor,
              null,
              getExceptionInternalNames(method.getExceptionTypes()));

      mv.visitAnnotation(Type.getDescriptor(ClientCallable.class), true);
      mv.visitCode();

      boolean isPrivate = Modifier.isPrivate(method.getModifiers());
      if (isPrivate) {
        // Load MethodHandle from static field
        mv.visitFieldInsn(
            Opcodes.GETSTATIC,
            internalClassName,
            method.getName(),
            "Ljava/lang/invoke/MethodHandle;");
      }

      // Load 'this'
      mv.visitVarInsn(Opcodes.ALOAD, 0);

      // Load and convert parameters
      Class<?>[] paramTypes = method.getParameterTypes();
      int localVarIndex = 1;
      for (Class<?> paramType : paramTypes) {
        if (hasJsonValueParams && JsonValue.class.isAssignableFrom(paramType)) {
          // Load the JsonNode parameter
          mv.visitVarInsn(Opcodes.ALOAD, localVarIndex);

          // Call JsonMigration.convertToJsonValue(JsonNode) -> JsonValue
          mv.visitMethodInsn(
              Opcodes.INVOKESTATIC,
              "com/flowingcode/vaadin/jsonmigration/JsonMigration",
              "convertToJsonValue",
              "(Ljava/lang/Object;)Lelemental/json/JsonValue;",
              false);

          // Cast to the original type if not JsonValue
          if (paramType != JsonValue.class) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(paramType));
          }

          localVarIndex++;
        } else {
          localVarIndex += loadParameter(mv, paramType, localVarIndex);
        }
      }

      if (isPrivate) {
        // Call private method
        String descriptor =
            "(" + Type.getDescriptor(method.getDeclaringClass()) + superDescriptor.substring(1);
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/invoke/MethodHandle",
            "invokeExact",
            descriptor,
            false);
      } else {
        // Call super.methodName(params) with original descriptor
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL, internalParentName, method.getName(), superDescriptor, false);
      }

      if (hasJsonValueReturn) {
        // Store result in local variable
        mv.visitVarInsn(Opcodes.ASTORE, localVarIndex);

        // Load result back
        mv.visitVarInsn(Opcodes.ALOAD, localVarIndex);

        // Call JsonMigration.convertToClientCallableResult(aux)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/flowingcode/vaadin/jsonmigration/JsonMigration",
            "convertToClientCallableResult",
            "(Lelemental/json/JsonValue;)Lelemental/json/JsonValue;",
            false);
      }

      // Return result or void
      if (method.getReturnType() == Void.TYPE) {
        mv.visitInsn(Opcodes.RETURN);
      } else if (method.getReturnType().isPrimitive()) {
        if (method.getReturnType() == Long.TYPE) {
          mv.visitInsn(Opcodes.LRETURN);
        } else if (method.getReturnType() == Float.TYPE) {
          mv.visitInsn(Opcodes.FRETURN);
        } else if (method.getReturnType() == Double.TYPE) {
          mv.visitInsn(Opcodes.DRETURN);
        } else {
          mv.visitInsn(Opcodes.IRETURN);
        }
      } else {
        mv.visitInsn(Opcodes.ARETURN);
      }

      mv.visitMaxs(0, 0);
      mv.visitEnd();
    }

    private void pushInt(MethodVisitor mv, short value) {
      if (value >= -1 && value <= 5) {
        mv.visitInsn(Opcodes.ICONST_0 + value);
      } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
        mv.visitIntInsn(Opcodes.BIPUSH, value);
      } else {
        mv.visitIntInsn(Opcodes.SIPUSH, value);
      }
    }

    private void loadClassConstant(MethodVisitor mv, Class<?> clazz) {
      if (clazz.isPrimitive()) {
        if (clazz == int.class) {
          mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
        } else if (clazz == boolean.class) {
          mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
        } else if (clazz == byte.class) {
          mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
        } else if (clazz == char.class) {
          mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
        } else if (clazz == short.class) {
          mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
        } else if (clazz == float.class) {
          mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
        } else if (clazz == long.class) {
          mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
        } else if (clazz == double.class) {
          mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
        } else {
          throw new IllegalArgumentException("Unsupported type: " + clazz);
        }
      } else {
        mv.visitLdcInsn(Type.getType(clazz));
      }
    }

    private int loadParameter(MethodVisitor mv, Class<?> paramType, int localVarIndex) {
      if (!paramType.isPrimitive()) {
        mv.visitVarInsn(Opcodes.ALOAD, localVarIndex);
        return 1;
      } else if (paramType == Long.TYPE) {
        mv.visitVarInsn(Opcodes.LLOAD, localVarIndex);
        return 2;
      } else if (paramType == Float.TYPE) {
        mv.visitVarInsn(Opcodes.FLOAD, localVarIndex);
        return 1;
      } else if (paramType == Double.TYPE) {
        mv.visitVarInsn(Opcodes.DLOAD, localVarIndex);
        return 2;
      } else {
        mv.visitVarInsn(Opcodes.ILOAD, localVarIndex);
        return 1;
      }
    }

    private String getMethodDescriptor(Method method, boolean convertJsonValueParams) {
      StringBuilder sb = new StringBuilder("(");
      for (Class<?> paramType : method.getParameterTypes()) {
        if (convertJsonValueParams && JsonValue.class.isAssignableFrom(paramType)) {
          sb.append(getConvertedTypeDescriptor(paramType));
        } else {
          sb.append(Type.getDescriptor(paramType));
        }
      }
      sb.append(")");
      sb.append(Type.getDescriptor(method.getReturnType()));
      return sb.toString();
    }

    private MethodHandle getConvertedTypeDescriptor;

    @SneakyThrows
    private String getConvertedTypeDescriptor(Class<?> type) {
      if (getConvertedTypeDescriptor == null) {
        Class<?> helper =
            Class.forName("com.flowingcode.vaadin.jsonmigration.ClassInstrumentationJacksonHelper");
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType methodType = MethodType.methodType(String.class, Class.class);
        getConvertedTypeDescriptor =
            lookup.findStatic(helper, "getConvertedTypeDescriptor", methodType);
      }
      return (String) getConvertedTypeDescriptor.invokeExact(type);
    }

    private String[] getExceptionInternalNames(Class<?>[] exceptionTypes) {
      if (exceptionTypes == null || exceptionTypes.length == 0) {
        return null;
      }
      String[] names = new String[exceptionTypes.length];
      for (int i = 0; i < exceptionTypes.length; i++) {
        names[i] = exceptionTypes[i].getName().replace('.', '/');
      }
      return names;
    }
  }
}
