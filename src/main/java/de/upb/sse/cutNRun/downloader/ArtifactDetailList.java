package de.upb.sse.cutNRun.downloader;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "artifact")
public class ArtifactDetailList {
    private List<ArtifactDetail> details;

    @Getter
    @Setter
    public static class ArtifactDetail {
        private String groupId;
        private String artifactId;
        private String version;
        private Repository repository;

        @Getter
        @Setter
        private static class Repository {
            private String id;
            private String url;
        }
    }
}
