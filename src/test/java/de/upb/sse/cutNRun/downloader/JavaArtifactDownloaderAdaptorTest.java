package de.upb.sse.cutNRun.downloader;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaArtifactDownloaderAdaptorTest {

    @Test
    public void dowloadTest() throws IOException {
        File file = Maven.resolver().resolve("org.mockito" + ":"
                                                     + "mockito-junit-jupiter" + ":jar:sources:"
                                                     + "5.14.2")
                         .withoutTransitivity()
                         .asSingleFile();

        /*File file = Maven.configureResolver().withMavenCentralRepo(false)
                            .withRemoteRepo("google", "https://maven.google.com/", "default")
                         .resolve("androidx.appcompat" + ":"
                                          + "appcompat" + ":jar:sources:"
                                          + "1.7.0")
                         .withoutTransitivity()
                         .asSingleFile();*/
        System.out.println(file);

        try {
            extractJar(file);
            assertTrue(isJavaLanguage());
        } finally {
            deleteExtractedSourceCode();
        }
    }

    private void deleteExtractedSourceCode() throws IOException {
        Path path = Paths.get("src/main/resources/extractedSourceCode");

        Files.walkFileTree(path,
                           new SimpleFileVisitor<>() {

                               // delete directories or folders
                               @Override
                               public FileVisitResult postVisitDirectory(Path dir,
                                                                         IOException exc)
                                       throws IOException {
                                   if (dir.endsWith("extractedSourceCode")) {
                                       return FileVisitResult.CONTINUE;
                                   }
                                   Files.delete(dir);
                                   //System.out.printf("Directory is deleted : %s%n", dir);
                                   return FileVisitResult.CONTINUE;
                               }

                               // delete files
                               @Override
                               public FileVisitResult visitFile(Path file,
                                                                BasicFileAttributes attrs)
                                       throws IOException {
                                   Files.delete(file);
                                   /*System.out.printf("File is deleted : %s%n", file);*/
                                   return FileVisitResult.CONTINUE;
                               }
                           });
    }

    private boolean isJavaLanguage() throws IOException {
        //long totalFiles = 0;
        Path startingDir = Paths.get("src/main/resources/extractedSourceCode");
        SourceCodeVisitor sourceCodeVisitor = new SourceCodeVisitor();
        Files.walkFileTree(startingDir, sourceCodeVisitor);
        /*try (Stream<Path> paths = Files.walk(startingDir)) {
            totalFiles = paths.filter(Files::isRegularFile)
                              .count();
        }*/
        float javaFilePercentage = (float) sourceCodeVisitor.getJavaFileCount() /
                (float) sourceCodeVisitor.getTotalFileCount() * 100;
        System.out.println("totalFiles=" + sourceCodeVisitor.getTotalFileCount() + " javaFiles=" + sourceCodeVisitor.getJavaFileCount()
                                   + " javaFilePercentage=" + javaFilePercentage);
        return javaFilePercentage > 90.0;
    }

    private void extractJar(File jarFile) throws IOException {
        java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile);
        java.util.Enumeration enumEntries = jar.entries();
        while (enumEntries.hasMoreElements()) {
            java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
            java.io.File f = new java.io.File("src/main/resources/extractedSourceCode" + java.io.File.separator + file.getName());
            f.getParentFile().mkdirs();
            if (file.isDirectory()) { // if its a directory, create it
                f.mkdir();
                continue;
            }
            java.io.InputStream is = jar.getInputStream(file); // get the input stream
            java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
            while (is.available() > 0) {  // write contents of 'is' to 'fos'
                fos.write(is.read());
            }
            fos.close();
            is.close();
        }
        jar.close();
    }

    private class SourceCodeVisitor extends SimpleFileVisitor<Path> {
        private int javaFileCount;

        private int totalFileCount;

        public SourceCodeVisitor() {
            this.javaFileCount = 0;
            this.totalFileCount = 0;
        }

        public int getJavaFileCount() {
            return javaFileCount;
        }

        public int getTotalFileCount() {
            return totalFileCount;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                                                 BasicFileAttributes attr)
                throws IOException {
            if (dir.endsWith("META-INF")) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attr)
                throws IOException {
            System.out.println("File = " + file);
            if (file.getFileName().toString().endsWith(".java")) {
                javaFileCount++;
            }
            if(!file.getFileName().toString().endsWith(".properties")
                    && !file.getFileName().toString().endsWith(".xsd")
                    && !file.getFileName().toString().endsWith(".dtd")
                    && !file.getFileName().toString().endsWith(".xml")
                    && !file.getFileName().toString().endsWith(".yml")
                    && !file.getFileName().toString().endsWith(".yaml")
                    && !file.getFileName().toString().endsWith(".html")
                    && !file.getFileName().toString().endsWith(".css")
                    && !file.getFileName().toString().endsWith(".js")
                    && !file.getFileName().toString().endsWith(".txt")
                    && !file.getFileName().toString().endsWith(".md")
                    && !file.getFileName().toString().equals("LICENSE")) {
                totalFileCount++;
            }
            return FileVisitResult.CONTINUE;
        }
    }
}