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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModernReflectionAnalysisTest {
    @Test
    public void shouldAnalyzeFindStaticCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile1"));
        ClassType classType = view.getIdentifierFactory().getClassType("ModernReflection");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "main", "void", List.of("java.lang.String[]")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(3);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(LOCAL, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());

        //For manual Testing
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindVirtualCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile2"));
        ClassType classType = view.getIdentifierFactory().getClassType("FindVirtualReflection");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "main", "void", List.of("java.lang.String[]")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(3);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(LOCAL, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());

        //For manual Testing
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindConstructorCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile3"));
        ClassType classType = view.getIdentifierFactory().getClassType("FindConstructorReflection");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "main", "void", List.of("java.lang.String[]")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(4);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(LOCAL, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());

        //For manual Testing
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindStaticGetterCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile4"));
        ClassType classType = view.getIdentifierFactory().getClassType("FindStaticGetterReflection");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "main", "void", List.of("java.lang.String[]")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(2);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(LOCAL, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());

        //For manual Testing
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindGetterCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile5"));
        ClassType classType = view.getIdentifierFactory().getClassType("FindGetterReflection");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "main", "void", List.of("java.lang.String[]")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(2);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(LOCAL, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());

        //For manual Testing
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindSpecialCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile6"));
        ClassType classType = view.getIdentifierFactory().getClassType("FindSpecialReflection");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "main", "void", List.of("java.lang.String[]")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(3);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(LOCAL, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());

        //For manual Testing
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindStaticParameterizedCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile7"));
        ClassType classType = view.getIdentifierFactory().getClassType("findStaticParameterizedReflection");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "main", "void", List.of("java.lang.String[]")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(3);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(LOCAL, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());

        //For manual Testing
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindStaticMultipleParameterCall() {
        //DownloadJarAnalysisInputLocation
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile8"));
        ClassType classType = view.getIdentifierFactory().getClassType("findStaticMultipleParameterReflection");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "main", "void", List.of("java.lang.String[]")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(5);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(LOCAL, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());

        //For manual Testing
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindStaticMethodSource() {
        //DownloadJarAnalysisInputLocation
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile9-MethodSource"));
        ClassType classType = view.getIdentifierFactory().getClassType("FindStaticMethodSource");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "main", "void", List.of("java.lang.String[]")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(4);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(RETURN_FROM_METHOD, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());

        //For manual Testing
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindGetterParameterSource() {
        //DownloadJarAnalysisInputLocation
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile10-ParameterSource"));
        ClassType classType = view.getIdentifierFactory().getClassType("FindGetterParameterSourceReflection");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "testMethod", "void", List.of("java.lang.String")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(2);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(METHOD_PARAMETER, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());

        //For manual Testing
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindConstructorFieldSource() {
        //DownloadJarAnalysisInputLocation
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile11-FieldSource"));
        ClassType classType = view.getIdentifierFactory().getClassType("FindConstructorFieldSourceReflection");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "main", "void", List.of("java.lang.String[]")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(8);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(FIELD, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());

        //For manual Testing
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFindSpecialMultipleSource() {
        //DownloadJarAnalysisInputLocation
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/modernReflection/testFile12-ifBranchMultipleSource"));
        ClassType classType = view.getIdentifierFactory().getClassType("FindSpecialReflectionMultipleSource");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "main", "void", List.of("java.lang.String[]")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(9);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        List<ArgumentSource> sources = argumentSourceAnalysis.getFlowBefore(stmtGraph.getStartingStmt())
                                                             .stream()
                                                             .map(result1 -> result1.getArgumentSource())
                                                             .collect(Collectors.toUnmodifiableList());
        assertEquals(true, stringConcatResult.isEmpty());
        assertEquals(true, argumentSourceAnalysis.isBranching());
        assertTrue(CollectionUtils.isEqualCollection(List.of(LOCAL, RETURN_FROM_METHOD), sources));

        //For manual Testing
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }
}
