package de.upb.sse.cutNRun.downloader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import com.google.common.io.Files;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArtifactDownloaderAdaptor implements ArtifactDownloaderPort {

    private static final String SEPARATOR = ":";
    private static final String JAR_NAME_SEPARATOR = "-";
    //private static final String DESTINATION_PATH = "src/main/resources/jars/";
    private static final String RELATIVE_JAR_RESOURCES_PATH = "jars/";
    private static final String JAR_FILE_EXTENSION = ".jar";
    private final ResourceLoader resourceLoader;
    @Value("${jar.resource.path}")
    private String jarResourcePath;

    @Override
    public void download(ArtifactDetailList artifactDetailList) throws IOException {
        for(ArtifactDetailList.ArtifactDetail artifactDetail : artifactDetailList.getDetails()) {
            if(isArtifactNotDownloaded(artifactDetail)) {
                log.info("Downloading jar: {}", artifactDetail.getArtifactId());
                File file = getMavenResolvedArtifact(artifactDetail);
                /*Maven.configureResolver().withMavenCentralRepo(false)
                                 .withRemoteRepo("bedatadriven","https://nexus.bedatadriven.com/content/groups/public/","default")*/
                                 //.resolver()
                                /* .resolve(artifactDetail.getGroupId() + SEPARATOR
                                                  + artifactDetail.getArtifactId() + SEPARATOR
                                                  + artifactDetail.getVersion())
                                 .withoutTransitivity()
                                 .asSingleFile();*/
                Files.copy(file, new File(jarResourcePath + file.getName()));
                log.info("Download completed for jar: {}", file.getName());
            }
        }
    }

    private boolean isArtifactNotDownloaded(ArtifactDetailList.ArtifactDetail artifactDetail) {
        Resource resource = resourceLoader.getResource(RELATIVE_JAR_RESOURCES_PATH + artifactDetail.getArtifactId()
                                                               + JAR_NAME_SEPARATOR + artifactDetail.getVersion()
                                                               + JAR_FILE_EXTENSION);
        return !resource.exists();
    }

    private File getMavenResolvedArtifact(ArtifactDetailList.ArtifactDetail artifactDetail){
        if(artifactDetail.getRepository() != null){
            return Maven.configureResolver().withMavenCentralRepo(false)
                 .withRemoteRepo(artifactDetail.getRepository().getId(), artifactDetail.getRepository().getUrl(),"default")
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
}
