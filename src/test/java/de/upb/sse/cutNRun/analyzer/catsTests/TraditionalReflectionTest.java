package de.upb.sse.cutNRun.analyzer.catsTests;

import de.upb.sse.cutNRun.analyzer.ProgramAnalyzerAdaptor;
import org.junit.jupiter.api.Test;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class TraditionalReflectionTest {
    @Test
    public void test1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/catsTestFiles/TraditionalReflection/TR1"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void test2() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/catsTestFiles/TraditionalReflection/TR2"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void test3() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/catsTestFiles/TraditionalReflection/TR3"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void test4() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/catsTestFiles/TraditionalReflection/TR4"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void test5() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/catsTestFiles/TraditionalReflection/TR5"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void test6() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/catsTestFiles/TraditionalReflection/TR6"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void test7() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/catsTestFiles/TraditionalReflection/TR7"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void test8() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/catsTestFiles/TraditionalReflection/TR8"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }
}

