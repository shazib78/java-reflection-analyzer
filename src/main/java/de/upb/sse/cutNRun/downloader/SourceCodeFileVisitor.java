package de.upb.sse.cutNRun.downloader;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class SourceCodeFileVisitor extends SimpleFileVisitor<Path> {
    private int javaFileCount;

    private int totalFileCount;

    public SourceCodeFileVisitor() {
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
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) throws IOException {
        if (dir.endsWith("META-INF")) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
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
                && !file.getFileName().toString().endsWith(".js")) {
            totalFileCount++;
        }
        return FileVisitResult.CONTINUE;
    }
}
