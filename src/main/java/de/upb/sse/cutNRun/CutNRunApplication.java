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
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

import java.nio.file.Files;
import java.nio.file.Path;
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
        int test = 1;
		/*methodExecutorPort.executeMethod("doNothing",
										 "src/test/resources/jars/CutNRunTestJars.jar");*/
        //TODO: uncomment to download jars
        //artifactDownloaderPort.download(artifactDetailList);
        for (ArtifactDetailList.ArtifactDetail artifactDetail : artifactDetailList.getDetails()) {
            String jarName = artifactDetail.getArtifactId() + "-" + artifactDetail.getVersion() + ".jar";
            String pathToBinary = jarResourcePath + jarName;
            //TODO: below resource check probably not working at runtime after file is downloaded
            if (Files.exists(Path.of(pathToBinary))/*resourceLoader.getResource("jars/" + jarName).exists()*/) {
                View view = new JavaView(new JavaClassPathAnalysisInputLocation(pathToBinary));
                programAnalyzerPort = new ProgramAnalyzerAdaptor(view, jarName);
                log.info("Analyzing project = {}: {}", artifactDetail.getGroupId(), jarName);
                /*if(test<=15) {
                    programAnalyzerPort.analyze();
                }*/
                programAnalyzerPort.analyze();
            } else {
                log.error("jar {} not found", jarName);
                //throw new RuntimeException("jar " + jarName + " not found");
            }
            /*test++;
            if(test>15){
                break;
            }*/
        }
    }
}
