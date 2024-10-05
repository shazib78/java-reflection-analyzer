package de.upb.sse.cutNRun.analyzer;

import org.junit.jupiter.api.Test;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

import static org.junit.jupiter.api.Assertions.*;

class ProgramAnalyzerAdaptorTest {

    @Test
    public void shouldAnalyzeSimpleReflectionCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/testFileSimple"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view);
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeTestFile1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/testFile1"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view);
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeTestFile2() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/testFile2"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view);
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeTestFile3() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/testFile3"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view);
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeTestFile4() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/testFile4"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view);
        programAnalyzerPort.analyze();
    }
}