package de.upb.sse.cutNRun.analyzer;

import org.junit.jupiter.api.Test;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.inputlocation.DownloadJarAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class ModernReflectionAnalysisTest {
    @Test
    public void shouldAnalyzeFindStaticCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile1"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view);
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindVirtualCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile2"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view);
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindConstructorCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile3"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view);
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindStaticGetterCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile4"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view);
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindGetterCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile5"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view);
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindSpecialCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile6"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view);
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindStaticParameterizedCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile7"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view);
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindStaticMultipleParameterCall() {
        //DownloadJarAnalysisInputLocation
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile8"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view);
        programAnalyzerPort.analyze();
    }
}
