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
import elemental.json.JsonArray;
import elemental.json.JsonBoolean;
import elemental.json.JsonNumber;
import elemental.json.JsonObject;
import elemental.json.JsonString;
import elemental.json.JsonValue;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

/**
 * Utility class for instrumenting classes at runtime.
 *
 * <p>
 * This class provides methods to dynamically create subclasses of a given parent class using
 * bytecode instrumentation. Methods annotated with {@link ClientCallable} that return a type
 * assignable to {@link JsonValue} are automatically overridden to convert the result through
 * {@link JsonMigration#convertToClientCallableResult(JsonValue)}.
 * </p>
 *
 * @author Javier Godoy / Flowing Code
 */
final class ClassInstrumentationUtil {

  private final int version;

  private static final Map<ClassLoader, InstrumentedClassLoader> classLoaderCache = new WeakHashMap<>();

  ClassInstrumentationUtil(int version) {
    this.version = version;
  }

  /**
   * Creates and returns an instance of a dynamically instrumented class that extends the specified
   * parent class.
   *
   * <p>
   * This method generates a new class at runtime that extends {@code parent}, and returns a new
   * instance of that generated class. The instrumented class will have a default constructor that
   * delegates to the parent's default constructor. All methods annotated with
   * {@link ClientCallable} that return a type assignable to {@link JsonValue} will be overridden to
   * convert the result via JsonMigration.convertClientCallableResult().
   * </p>
   *
   * <p>
   * <b>Requirements:</b>
   * </p>
   * <ul>
   * <li>The parent class must not be final</li>
   * <li>The parent class must have an accessible no-argument constructor</li>
   * </ul>
   *
   * @param <T>    the type of the parent class
   * @param parent the parent class to extend
   * @return a new instance of the instrumented class extending {@code parent}
   * @throws IllegalArgumentException if the parent class is final, an interface, a primitive type,
   *         an array type, or does not have an accessible no-argument constructor
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
      return parent;
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

  private static Stream<Method> getDeclaredCallables(Class<?> parent) {
    return Stream.of(parent.getDeclaredMethods()).filter(method -> {
      int modifiers = method.getModifiers();
      if (!Modifier.isStatic(modifiers) && !Modifier.isPrivate(modifiers)) {
        boolean isCallable = method.isAnnotationPresent(ClientCallable.class);
        boolean isLegacyCallable = method.isAnnotationPresent(LegacyClientCallable.class);
        return isCallable || isLegacyCallable;
      }
      return false;
    });
  }

  private List<Method> getInstrumentableMethods(Class<?> parent) {
    return getDeclaredCallables(parent).filter(method -> {
      boolean isCallable = method.isAnnotationPresent(ClientCallable.class);
      boolean isLegacyCallable = method.isAnnotationPresent(LegacyClientCallable.class);

      if (hasLegacyVaadin()) {
        return isLegacyCallable;
      }

      if (isCallable || isLegacyCallable) {
        boolean hasJsonValueReturn = JsonValue.class.isAssignableFrom(method.getReturnType());
        boolean hasJsonValueParams = hasJsonValueParameters(method);

        if (isCallable && hasJsonValueParams) {
          throw new IllegalArgumentException(String.format(
              "Instrumented method '%s' in class '%s' has JsonValue arguments and must be annotated with @%s instead of @ClientCallable",
              method.getName(), method.getDeclaringClass().getName(),
              LegacyClientCallable.class.getName()));
        } else if (isCallable && hasJsonValueReturn) {
          return true;
        } else if (isLegacyCallable) {
          return true;
        }
      }

      return false;

    }).collect(Collectors.toList());
  }

  private static boolean hasJsonValueParameters(Method method) {
    for (Class<?> paramType : method.getParameterTypes()) {
      if (JsonValue.class.isAssignableFrom(paramType)) {
        return true;
      }
    }
    return false;
  }

  private <T extends Component> Class<? extends T> createInstrumentedClass(Class<T> parent,
      String className) throws Exception {
    InstrumentedClassLoader classLoader = getOrCreateInstrumentedClassLoader(parent.getClassLoader());
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
      return instrumentedClassCache.computeIfAbsent(parent, p -> {
        byte[] bytecode = generateBytecode(className, p);
        return defineClass(className, bytecode, 0, bytecode.length);
      });
    }

    private byte[] generateBytecode(String className, Class<?> parent) {
      String internalClassName = className.replace('.', '/');
      String internalParentName = parent.getName().replace('.', '/');

      ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

      cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, internalClassName, null, internalParentName, null);

      generateConstructor(cw, internalParentName);
      generateClientCallableOverrides(cw, parent, internalParentName);

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

    private void generateClientCallableOverrides(ClassWriter cw, Class<?> parent,
        String internalParentName) {
      for (Method method : getInstrumentableMethods(parent)) {
        generateMethodOverride(cw, method, internalParentName);
      }
    }

    private void generateMethodOverride(ClassWriter cw, Method method, String internalParentName) {
      boolean hasJsonValueReturn = !hasLegacyVaadin() && JsonValue.class.isAssignableFrom(method.getReturnType());
      boolean hasJsonValueParams = !hasLegacyVaadin() && hasJsonValueParameters(method);

      String overrideDescriptor = getMethodDescriptor(method, hasJsonValueParams);
      String superDescriptor = getMethodDescriptor(method, false);
      int access = method.getModifiers() & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED);

      MethodVisitor mv = cw.visitMethod(access, method.getName(), overrideDescriptor, null,
          getExceptionInternalNames(method.getExceptionTypes()));

      mv.visitAnnotation(Type.getDescriptor(ClientCallable.class), true);
      mv.visitCode();

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
          mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/flowingcode/vaadin/jsonmigration/JsonMigration",
              "convertToJsonValue", "(Ljava/lang/Object;)Lelemental/json/JsonValue;", false);

          // Cast to the original type if not JsonValue
          if (paramType != JsonValue.class) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(paramType));
          }

          localVarIndex++;
        } else {
          localVarIndex += loadParameter(mv, paramType, localVarIndex);
        }
      }

      // Call super.methodName(params) with original descriptor
      mv.visitMethodInsn(Opcodes.INVOKESPECIAL, internalParentName, method.getName(), superDescriptor,
          false);

      if (hasJsonValueReturn) {
        // Store result in local variable
        mv.visitVarInsn(Opcodes.ASTORE, localVarIndex);

        // Load result back
        mv.visitVarInsn(Opcodes.ALOAD, localVarIndex);

        // Call JsonMigration.convertToClientCallableResult(aux)
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/flowingcode/vaadin/jsonmigration/JsonMigration",
            "convertToClientCallableResult", "(Lelemental/json/JsonValue;)Lelemental/json/JsonValue;",
            false);
      }

      // Return result or void
      if (method.getReturnType() == Void.TYPE) {
        mv.visitInsn(Opcodes.RETURN);
      } else {
        mv.visitInsn(Opcodes.ARETURN);
      }

      mv.visitMaxs(0, 0);
      mv.visitEnd();
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

    private String getConvertedTypeDescriptor(Class<?> type) {
      if (type == JsonObject.class) {
        return Type.getDescriptor(ObjectNode.class);
      } else if (type == JsonArray.class) {
        return Type.getDescriptor(ArrayNode.class);
      } else if (type == JsonBoolean.class) {
        return Type.getDescriptor(BooleanNode.class);
      } else if (type == JsonNumber.class) {
        return Type.getDescriptor(DoubleNode.class);
      } else if (type == JsonString.class) {
        return Type.getDescriptor(StringNode.class);
      } else if (JsonValue.class.isAssignableFrom(type)) {
        return Type.getDescriptor(JsonNode.class);
      }
      return Type.getDescriptor(type);
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
