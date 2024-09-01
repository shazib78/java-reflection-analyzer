package de.upb.sse.cutNRun.downloader;

import java.io.IOException;

public interface ArtifactDownloaderPort {
    void download(ArtifactDetailList artifactDetailList) throws IOException;
}
