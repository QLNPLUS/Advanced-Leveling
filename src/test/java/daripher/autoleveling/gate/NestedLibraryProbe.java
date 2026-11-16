package daripher.autoleveling.gate;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Test-only probe: opens the packaged release jar, extracts its nested expression library and uses
 * that copy to evaluate an expression.
 *
 * <p>It lives in the test sourceset, so it is never part of the distributable jar. It exists to show
 * that the nested archive a loader would put on the classpath is a working library, not only a byte
 * sequence equal to the pinned artifact.
 */
public final class NestedLibraryProbe {
  private NestedLibraryProbe() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      throw new IllegalArgumentException("usage: <packaged jar> <nested library entry>");
    }
    Path packaged = Path.of(args[0]);
    String nestedEntry = args[1];
    try (ZipFile archive = new ZipFile(packaged.toFile())) {
      ZipEntry entry = archive.getEntry(nestedEntry);
      if (entry == null) {
        throw new IllegalStateException("missing nested library " + nestedEntry + " in " + packaged);
      }
      Path extracted = Files.createTempFile("yiran-expression-lib", ".jar");
      try {
        Files.copy(archive.getInputStream(entry), extracted, StandardCopyOption.REPLACE_EXISTING);
        // A JDK-only parent: the test runtime classpath already contains the canonical library, so a
        // parent-first loader would happily serve that copy and make the "loaded from the nested
        // archive" claim meaningless. fastutil is the one third-party type the library needs, and it
        // is added explicitly instead of by inheritance.
        URL fastutil =
            Class.forName("it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap")
                .getProtectionDomain()
                .getCodeSource()
                .getLocation();
        try (URLClassLoader loader =
            new URLClassLoader(
                new URL[] {extracted.toUri().toURL(), fastutil},
                ClassLoader.getPlatformClassLoader())) {
          Class<?> builderType = loader.loadClass("net.yiran.expressionlib.expr.ExpressionBuilder");
          if (builderType.getClassLoader() != loader) {
            throw new IllegalStateException(
                "ExpressionBuilder was not defined by the isolated loader: " + builderType.getClassLoader());
          }
          String codeSource =
              builderType.getProtectionDomain().getCodeSource().getLocation().toString();
          if (!codeSource.equals(extracted.toUri().toURL().toString())) {
            throw new IllegalStateException(
                "ExpressionBuilder came from " + codeSource + " instead of the extracted nested jar");
          }
          Object builder = builderType.getConstructor(String.class).newInstance("base + level");
          builder =
              builderType
                  .getMethod("variables", String[].class)
                  .invoke(builder, (Object) new String[] {"base", "level"});
          Object expression = builderType.getMethod("build").invoke(builder);
          double value =
              (double)
                  expression
                      .getClass()
                      .getMethod("evaluate", Map.class)
                      .invoke(expression, Map.of("base", 2.0D, "level", 3.0D));
          if (value != 5.0D) {
            throw new IllegalStateException("nested library evaluated base + level to " + value);
          }
          System.out.println(
              "nested library loaded from "
                  + nestedEntry
                  + " (defined by the isolated loader, code source "
                  + codeSource
                  + ") and evaluated base + level = "
                  + value);
        }
      } finally {
        Files.deleteIfExists(extracted);
      }
    }
  }
}
