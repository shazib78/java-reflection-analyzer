package de.upb.sse.cutNRun.downloader;

import de.upb.sse.cutNRun.dataRecorder.ExcelWriterAdapter;
import de.upb.sse.cutNRun.dataRecorder.ExcelWriterPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import com.google.common.io.Files;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JavaArtifactDownloaderAdaptor implements ArtifactDownloaderPort {

    private static final String SEPARATOR = ":";
    private static final String JAR_NAME_SEPARATOR = "-";
    //private static final String DESTINATION_PATH = "src/main/resources/jars/";
    private static final String RELATIVE_JAR_RESOURCES_PATH = "jars/";
    private static final String JAR_FILE_EXTENSION = ".jar";
    private static final String JAR_SOURCES = "jar:sources";
    private static final String JAVA_FILTER_PROJECTS_FILENAME = "Total_Java_And_NonJava_Projects_Considered";
    private final ResourceLoader resourceLoader;
    private Float javaFilePercentage;
    @Value("${jar.resource.path}")
    private String jarResourcePath;

    @Override
    public void download(ArtifactDetailList artifactDetailList) {
        ExcelWriterPort studyProjectsExcelWriter = new ExcelWriterAdapter(JAVA_FILTER_PROJECTS_FILENAME, false);
        studyProjectsExcelWriter.setHeaders("ProjectName", "isJavaProject", "javaFilePercentage", "Error");
        Map<String, Object[]> excelData = new LinkedHashMap<>();
        //int testCount = 1;
        for (ArtifactDetailList.ArtifactDetail artifactDetail : artifactDetailList.getDetails()) {
            if (isArtifactNotDownloaded(artifactDetail)) {
                try {
                    boolean isJavaProject = isJavaProject(artifactDetail);
                    if (isJavaProject) {
                        log.info("Downloading jar: {}", artifactDetail.getArtifactId());
                        File file = getMavenResolvedArtifact(artifactDetail);
                        Files.copy(file, new File(jarResourcePath + file.getName()));
                        log.info("Download completed for jar: {}", file.getName());
                    }
                    String projectName = artifactDetail.getGroupId() + "_" + artifactDetail.getArtifactId() + "_" + artifactDetail.getVersion();
                    excelData.put(projectName, new Object[]{projectName, String.valueOf(isJavaProject),
                            javaFilePercentage.toString(), ""});
                } catch (Exception e) {
                    e.printStackTrace();
                    String projectName = artifactDetail.getGroupId() + "_" + artifactDetail.getArtifactId() + "_" + artifactDetail.getVersion();
                    excelData.put(projectName, new Object[]{projectName, "ERROR", "", e.getMessage()});
                }
            }
            /*if (testCount == 5) {
                break;
            } else {
                testCount++;
            }*/
        }
        studyProjectsExcelWriter.saveData(excelData);
    }

    private boolean isArtifactNotDownloaded(ArtifactDetailList.ArtifactDetail artifactDetail) {
        Resource resource = resourceLoader.getResource(RELATIVE_JAR_RESOURCES_PATH + artifactDetail.getArtifactId()
                                                               + JAR_NAME_SEPARATOR + artifactDetail.getVersion()
                                                               + JAR_FILE_EXTENSION);
        return !resource.exists();
    }

    private File getMavenResolvedArtifact(ArtifactDetailList.ArtifactDetail artifactDetail) {
        if (artifactDetail.getRepository() != null) {
            return Maven.configureResolver().withMavenCentralRepo(false)
                        .withRemoteRepo(artifactDetail.getRepository().getId(), artifactDetail.getRepository().getUrl(), "default")
                        .resolve(artifactDetail.getGroupId() + SEPARATOR
                                         + artifactDetail.getArtifactId() + SEPARATOR
                                         + artifactDetail.getVersion())
                        .withoutTransitivity()
                        .asSingleFile();
        } else {
            return Maven.resolver().resolve(artifactDetail.getGroupId() + SEPARATOR
                                                    + artifactDetail.getArtifactId() + SEPARATOR
                                                    + artifactDetail.getVersion())
                        .withoutTransitivity()
                        .asSingleFile();
        }
    }

    private boolean isJavaProject(ArtifactDetailList.ArtifactDetail artifactDetail) throws IOException {
        File sourcesJarFile = getSourcesJar(artifactDetail);
        /*boolean isJavaProject = false;
        try {
            extractJar(sourcesJarFile);
            isJavaProject = isJavaLanguage();
        } finally {
            deleteExtractedSourceCode();
        }
        return isJavaProject;*/
        List<String> confirmedJavaProjects = Arrays.asList("org.springframework.boot_spring-boot-starter-test",
                                                           "org.springframework.boot_spring-boot-starter-web",
                                                           "org.springframework.boot_spring-boot-starter");
        if (confirmedJavaProjects.contains(artifactDetail.getGroupId() + "_" + artifactDetail.getArtifactId())) {
            return true;
        }
        return isJavaProjectWithoutExtraction(sourcesJarFile);
    }

    /*private boolean isJavaLanguage() throws IOException {
        log.info("Determining if Java Project");
        //long totalFiles = 0;
        Path startingDir = Paths.get("src/main/resources/extractedSourceCode");
        SourceCodeFileVisitor sourceCodeFileVisitor = new SourceCodeFileVisitor();
        java.nio.file.Files.walkFileTree(startingDir, sourceCodeFileVisitor);
        *//*try (Stream<Path> paths = Files.walk(startingDir)) {
            totalFiles = paths.filter(Files::isRegularFile)
                              .count();
        }*//*
        javaFilePercentage = (float) sourceCodeFileVisitor.getJavaFileCount() /
                (float) sourceCodeFileVisitor.getTotalFileCount() * 100;
        log.info("totalFiles=" + sourceCodeFileVisitor.getTotalFileCount() + " javaFiles=" + sourceCodeFileVisitor.getJavaFileCount()
                         + " javaFilePercentage=" + javaFilePercentage);
        return javaFilePercentage > 90.0;
    }

    private void extractJar(File jarFile) throws IOException {
        log.info("Extracting Source Jar");
        java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile);
        java.util.Enumeration enumEntries = jar.entries();
        while (enumEntries.hasMoreElements()) {
            java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
            System.out.println(file.getName());
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

    private void deleteExtractedSourceCode() throws IOException {
        log.info("Deleting Extracted Source Code");
        Path path = Paths.get("src/main/resources/extractedSourceCode");

        java.nio.file.Files.walkFileTree(path,
                                         new SimpleFileVisitor<>() {

                                             // delete directories or folders
                                             @Override
                                             public FileVisitResult postVisitDirectory(Path dir,
                                                                                       IOException exc)
                                                     throws IOException {
                                                 if (dir.endsWith("extractedSourceCode")) {
                                                     return FileVisitResult.CONTINUE;
                                                 }
                                                 java.nio.file.Files.delete(dir);
                                                 //System.out.printf("Directory is deleted : %s%n", dir);
                                                 return FileVisitResult.CONTINUE;
                                             }

                                             // delete files
                                             @Override
                                             public FileVisitResult visitFile(Path file,
                                                                              BasicFileAttributes attrs)
                                                     throws IOException {
                                                 java.nio.file.Files.delete(file);
                                                 *//*System.out.printf("File is deleted : %s%n", file);*//*
                                                 return FileVisitResult.CONTINUE;
                                             }
                                         });
    }*/

    private File getSourcesJar(ArtifactDetailList.ArtifactDetail artifactDetail) {
        if (artifactDetail.getRepository() != null) {
            return Maven.configureResolver().withMavenCentralRepo(false)
                        .withRemoteRepo(artifactDetail.getRepository().getId(), artifactDetail.getRepository().getUrl(), "default")
                        .resolve(artifactDetail.getGroupId() + SEPARATOR
                                         + artifactDetail.getArtifactId() + SEPARATOR
                                         + JAR_SOURCES + SEPARATOR
                                         + artifactDetail.getVersion())
                        .withoutTransitivity()
                        .asSingleFile();
        } else {
            return Maven.resolver().resolve(artifactDetail.getGroupId() + SEPARATOR
                                                    + artifactDetail.getArtifactId() + SEPARATOR
                                                    + JAR_SOURCES + SEPARATOR
                                                    + artifactDetail.getVersion())
                        .withoutTransitivity()
                        .asSingleFile();
        }
    }

    private boolean isJavaProjectWithoutExtraction(File jarFile) throws IOException {
        log.info("Extracting Source Jar");
        int totalFileCount = 0;
        int javaFileCount = 0;
        java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile);
        java.util.Enumeration enumEntries = jar.entries();
        while (enumEntries.hasMoreElements()) {
            java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
            //System.out.println(file.getName());
            if (file.getName().startsWith("META-INF")) {
                continue;
            } else {
                if (!file.isDirectory()) {
                    if (file.getName().endsWith(".java")) {
                        javaFileCount++;
                    }
                    if (!file.getName().endsWith(".properties")
                            && !file.getName().endsWith(".xsd")
                            && !file.getName().endsWith(".dtd")
                            && !file.getName().endsWith(".xml")
                            && !file.getName().endsWith(".yml")
                            && !file.getName().endsWith(".yaml")
                            && !file.getName().endsWith(".html")
                            && !file.getName().endsWith(".css")
                            && !file.getName().endsWith(".js")
                            && !file.getName().endsWith(".txt")
                            && !file.getName().endsWith(".md")) {
                        totalFileCount++;
                    }
                }
            }
            /*java.io.File f = new java.io.File("src/main/resources/extractedSourceCode" + java.io.File.separator + file.getName());
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
            is.close();*/
        }
        jar.close();
        javaFilePercentage = (float) javaFileCount / (float) totalFileCount * 100;
        log.info("totalFiles=" + totalFileCount + " javaFiles=" + javaFileCount
                         + " javaFilePercentage=" + javaFilePercentage);
        return javaFilePercentage > 90.0;
    }
}
