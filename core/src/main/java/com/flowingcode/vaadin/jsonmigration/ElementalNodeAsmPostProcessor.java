/*-
 * #%L
 * Json Migration Helper
 * %%
 * Copyright (C) 2025 - 2026 Flowing Code
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Dynamically modifies the class header to implement the JSON interface specified in the
 * UnsupportedJsonValueImpl<T> generic argument.
 */
public class ElementalNodeAsmPostProcessor {

  public static void main(String[] args) throws Exception {
    for (String arg : args) {
      Path classPath = Paths.get(arg);
      byte[] b = Files.readAllBytes(classPath);

      ClassReader cr = new ClassReader(b);
      ClassWriter cw = new ClassWriter(cr, 0);
      ClassVisitorImpl transformer = new ClassVisitorImpl(cw);

      cr.accept(transformer, 0);

      if (transformer.modified) {
        Files.write(classPath, cw.toByteArray());
        System.out.println("Successfully patched: " + classPath.getFileName());
      }
    }
  }

  private static class ClassVisitorImpl extends ClassVisitor {

    private final static String TARGET_INTERFACE =
        Type.getInternalName(UnsupportedJsonValueImpl.class);

    boolean modified;

    public ClassVisitorImpl(ClassVisitor cv) {
      super(Opcodes.ASM9, cv);
    }

    @Override
    @SneakyThrows
    public void visit(int version, int access, String name, String signature, String superName,
        String[] interfaces) {
      String detectedInterface = detectInterface(signature);

      for (String intf : interfaces) {
        if (intf.equals(detectedInterface)) {
          return;
        }
      }

      modified = true;
      interfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
      interfaces[interfaces.length - 1] = detectedInterface;
      super.visit(version, access, name, signature, superName, interfaces);
    }

    private String detectInterface(@NonNull String signature) {
      // Extracts the internal name of the specific generic type argument 'T' from
      // the class signature implementing UnsupportedJsonValueImpl<T>.
      String[] detectedInterface = new String[1];
      SignatureReader reader = new SignatureReader(signature);
      reader.accept(new SignatureVisitor(Opcodes.ASM9) {
        private boolean insideTargetInterface = false;

        @Override
        public SignatureVisitor visitTypeArgument(char wildcard) {
          // Move into the <T> block
          return super.visitTypeArgument(wildcard);
        }

        @Override
        public SignatureVisitor visitInterface() {
          return this;
        }

        @Override
        public void visitClassType(String name) {
          if (name.equals(TARGET_INTERFACE)) {
            insideTargetInterface = true;
          } else if (insideTargetInterface && detectedInterface[0] == null) {
            // This is the first class type found AFTER UnsupportedJsonValueImpl
            // which represents the generic argument T
            detectedInterface[0] = name;
            insideTargetInterface = false; // Stop looking
          }
        }

        @Override
        public void visitEnd() {
          insideTargetInterface = false;
          super.visitEnd();
        }

      });
      return Optional.ofNullable(detectedInterface[0])
          .orElseThrow(() -> new IllegalArgumentException("Failed to extract interface"));
    }

    @Override
    public void visitEnd() {
      super.visitEnd();
    }

  }

}