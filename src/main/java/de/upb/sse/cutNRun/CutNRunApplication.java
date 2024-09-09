package de.upb.sse.cutNRun;

import de.upb.sse.cutNRun.analyzer.ProgramAnalyzerAdaptor;
import de.upb.sse.cutNRun.analyzer.ProgramAnalyzerPort;
import de.upb.sse.cutNRun.downloader.ArtifactDetailList;
import de.upb.sse.cutNRun.downloader.ArtifactDownloaderPort;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ResourceLoader;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class CutNRunApplication implements CommandLineRunner {

    /*private MethodExecutorPort methodExecutorPort;*/
    private ProgramAnalyzerPort programAnalyzerPort;
    @NonNull
    private ArtifactDownloaderPort artifactDownloaderPort;
    @NonNull
    private ArtifactDetailList artifactDetailList;
    private final ResourceLoader resourceLoader;
    @Value("${jar.resource.path}")
    private String jarResourcePath;

    public static void main(String[] args) {
        SpringApplication.run(CutNRunApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
		/*methodExecutorPort.executeMethod("doNothing",
										 "src/test/resources/jars/CutNRunTestJars.jar");*/
        //TODO: Uucomment below
        /*artifactDownloaderPort.download(artifactDetailList);
        for (ArtifactDetailList.ArtifactDetail artifactDetail : artifactDetailList.getDetails()) {
            String jarName = artifactDetail.getArtifactId() + "-" + artifactDetail.getVersion() + ".jar";
            String pathToBinary = jarResourcePath + jarName;
            //TODO: below resource check probably not working at runtime after file is downloaded
            if (resourceLoader.getResource("jars/" + jarName).exists()) {
                View view = new JavaView(new JavaClassPathAnalysisInputLocation(pathToBinary));
                programAnalyzerPort = new ProgramAnalyzerAdaptor(view);
                log.info("Analyzing project = {}: {}", artifactDetail.getGroupId(), jarName);
                programAnalyzerPort.analyze();
            } else {
                log.error("jar {} not found", jarName);
                throw new RuntimeException("jar " + jarName + " not found");
            }
        }*/

        //TODO: Remove - For testing
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/"));
        programAnalyzerPort = new ProgramAnalyzerAdaptor(view);
        programAnalyzerPort.analyze();
    }
}
