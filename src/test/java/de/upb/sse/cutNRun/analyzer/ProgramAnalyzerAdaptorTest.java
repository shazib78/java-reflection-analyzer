package de.upb.sse.cutNRun.analyzer;

import de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource;
import de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSourceAnalysis;
import de.upb.sse.cutNRun.analyzer.intraprocedural.Result;
import de.upb.sse.cutNRun.analyzer.intraprocedural.StringConcatenationSource;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

import java.util.Arrays;
import java.util.List;

import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgramAnalyzerAdaptorTest {

    @Test
    public void shouldAnalyzeSimpleReflectionCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/testFileSimple"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeTestFile1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/File1"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeTestFile2() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/File2"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeTestFile3() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/File3"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeTestFile4() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/File4"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeTestJar() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/testJars/CutNRunTestJars.jar"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    //Parameter tests
    @Test
    public void shouldAnalyzeParameterTestFile1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/parameter/File1"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeParameterTestFile2() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/parameter/File2"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    //method return value tests
    @Test
    public void shouldAnalyzeReturnValueTestFile3() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/parameter/File3"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeReturnValueTestFile4() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/parameter/File4"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeReturnValueTestFile5() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/parameter/File5"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    //field tests
    @Test
    public void shouldAnalyzeFieldTestFile1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/field/File1"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFieldTestFile2() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/field/File2"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFieldTestFile3() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/field/File3"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    //multiple factors tests -concat
    @Test
    public void shouldAnalyzeMultipleFactorsTestStringConcat1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/multipleSources/StringConcat1"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test //test only for 2 factors - concat logic
    public void shouldAnalyzeMultipleFactorsTestStringConcat2() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/multipleSources/StringConcat2"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeMultipleFactorsTestStringConcat3() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/multipleSources/StringConcat3"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeMultipleFactorsTestStringConcat4() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/multipleSources/StringConcat4"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeIfBranchTest1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/multipleSources/ifBranch1"));
        ClassType classType = view.getIdentifierFactory().getClassType("Main");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "test", "void", List.of("java.lang.String")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(9);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        stmtGraph.getStmts().stream().forEach(stmt->
                                              {
                                                  System.out.println("====================");
                                                  System.out.println("Stmt: [" + stmt + "]");
                                                  System.out.println("before: " + argumentSourceAnalysis.getFlowBefore(stmt));
                                                  System.out.println("after: " + argumentSourceAnalysis.getFlowAfter(stmt));
                                                  System.out.println("====================");
                                              });
        assertEquals(UNKOWN, result.getArgumentSource());
        assertEquals(false, stringConcatResult.isEveryStringFromSameSource());
        assertEquals(2, stringConcatResult.getArgumentSources().size());
        assertTrue(CollectionUtils.isEqualCollection(stringConcatResult.getArgumentSources(), Arrays.asList(RETURN_FROM_METHOD, LOCAL)));
    }

    //TODO: Test if-else from different sources and same
    //TODO: Test if-else and string concat
    //TODO: Test field and constructor too

    @Test
    public void testing() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/testJars/spring-test-6.1.12.jar"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }
}