package de.upb.sse.cutNRun.analyzer.catsTests;

import de.upb.sse.cutNRun.analyzer.ProgramAnalyzerAdaptor;
import org.junit.jupiter.api.Test;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class ModernReflectionTest {
    @Test
    public void test1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/catsTestFiles/ModernReflection/TMR1"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void test2() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/catsTestFiles/ModernReflection/TMR2"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void test3() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/catsTestFiles/ModernReflection/TMR3"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void test4() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/catsTestFiles/ModernReflection/TMR4"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void test5() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/catsTestFiles/ModernReflection/TMR5"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void test6() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/catsTestFiles/ModernReflection/TMR6"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void test7() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/catsTestFiles/ModernReflection/TMR7"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void test8() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/catsTestFiles/ModernReflection/TMR8"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }
}

