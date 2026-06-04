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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

/**
 * Replaces references to the inner LitRenderer stand-in interface with
 * com.vaadin.flow.data.renderer.LitRenderer, allowing the class to be compiled against Vaadin 14
 * while referencing the real LitRenderer available in Vaadin 24+.
 */
public class LitRendererAsmPostProcessor {

  private static final String SOURCE =
      "com/flowingcode/vaadin/jsonmigration/LitRendererMigrationExtension$LitRenderer";
  private static final String TARGET = "com/vaadin/flow/data/renderer/LitRenderer";

  public static void main(String[] args) throws Exception {
    for (String arg : args) {
      Path classPath = Paths.get(arg);
      byte[] original = Files.readAllBytes(classPath);

      ClassReader cr = new ClassReader(original);
      ClassWriter cw = new ClassWriter(0);

      Remapper remapper = new Remapper() {
        @Override
        public String map(String internalName) {
          if (internalName.equals(SOURCE)) {
            return TARGET;
          }
          return internalName;
        }
      };

      // Drop the InnerClasses entry that ClassRemapper would rewrite to point at the real
      // LitRenderer, which is not actually an inner class of LitRendererMigrationExtension.
      ClassVisitor filter = new ClassVisitor(Opcodes.ASM9, cw) {
        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
          if (!TARGET.equals(name)) {
            super.visitInnerClass(name, outerName, innerName, access);
          }
        }
      };

      cr.accept(new ClassRemapper(filter, remapper), 0);
      Files.write(classPath, cw.toByteArray());
      System.out.println("Successfully patched: " + classPath.getFileName());
    }

    // Delete the orphaned inner class file produced by the compiler
    if (args.length > 0) {
      Path innerClass =
          Paths.get(args[0]).resolveSibling("LitRendererMigrationExtension$LitRenderer.class");
      if (Files.deleteIfExists(innerClass)) {
        System.out.println("Deleted orphaned inner class: " + innerClass.getFileName());
      }
    }
  }
}
