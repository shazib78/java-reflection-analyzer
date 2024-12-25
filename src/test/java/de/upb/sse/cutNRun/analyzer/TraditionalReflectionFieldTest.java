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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TraditionalReflectionFieldTest {
    @Test
    public void shouldAnalyzeFile1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/field/File1"));
        ClassType classType = view.getIdentifierFactory().getClassType("FieldFile1");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "caller", "void", Collections.emptyList()))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(17);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        /*assertEquals(argumentSourceAnalysis.getFlowBefore(stmtGraph.getStartingStmt()).size(), 2);
        List<ArgumentSource> sources = argumentSourceAnalysis.getFlowBefore(stmtGraph.getStartingStmt()).stream()
                                                             .map(result1 -> result1.getArgumentSource())
                                                             .collect(Collectors.toUnmodifiableList());
        assertTrue(CollectionUtils.isEqualCollection(sources, Arrays.asList(RETURN_FROM_METHOD, RETURN_FROM_METHOD)));*/
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(LOCAL, result.getArgumentSource());

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFile2() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/field/File2"));
        ClassType classType = view.getIdentifierFactory().getClassType("FieldFile2");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "caller", "void", Collections.emptyList()))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(6);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertEquals(UNKOWN, result.getArgumentSource());
        assertEquals(false, stringConcatResult.isEmpty());
        assertTrue(CollectionUtils.isEqualCollection(stringConcatResult.getArgumentSources(), Arrays.asList(LOCAL, LOCAL)));

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFile3() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/field/File3"));
        ClassType classType = view.getIdentifierFactory().getClassType("FieldFile3");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "caller", "void", Collections.emptyList()))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(7);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertEquals(UNKOWN, result.getArgumentSource());
        assertEquals(false, stringConcatResult.isEmpty());
        assertTrue(CollectionUtils.isEqualCollection(stringConcatResult.getArgumentSources(), Arrays.asList(LOCAL, LOCAL
                , LOCAL, LOCAL, LOCAL, LOCAL)));

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFile4() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/field/File4"));
        ClassType classType = view.getIdentifierFactory().getClassType("FieldFile4");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "caller", "void", Collections.emptyList()))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(5);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertEquals(UNKOWN, result.getArgumentSource());
        assertEquals(false, stringConcatResult.isEmpty());
        assertTrue(CollectionUtils.isEqualCollection(stringConcatResult.getArgumentSources(), Arrays.asList(LOCAL, LOCAL
                , LOCAL, LOCAL)));

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFile5() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/field/File5"));
        ClassType classType = view.getIdentifierFactory().getClassType("FieldFile5");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "caller", "void", List.of("java.lang.String")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(8);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertEquals(UNKOWN, result.getArgumentSource());
        assertEquals(false, stringConcatResult.isEmpty());
        assertEquals(false, argumentSourceAnalysis.isBranching());
        assertTrue(CollectionUtils.isEqualCollection(stringConcatResult.getArgumentSources(), Arrays.asList(LOCAL, LOCAL,
                                                                                                            LOCAL, LOCAL,
                                                                                                            METHOD_PARAMETER,
                                                                                                            RETURN_FROM_METHOD,
                                                                                                            FIELD)));

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFile6() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/field/File6-interface"));
        ClassType classType = view.getIdentifierFactory().getClassType("FieldFile6");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "caller", "void", Collections.emptyList()))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(7);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertEquals(RETURN_FROM_METHOD, result.getArgumentSource());
        assertEquals(true, stringConcatResult.isEmpty());
        assertEquals(false, argumentSourceAnalysis.isBranching());

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFile7() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/field/File7-nameFromInstanceMethod"));
        ClassType classType = view.getIdentifierFactory().getClassType("FieldFile7");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "caller", "void", Collections.emptyList()))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(5);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertEquals(RETURN_FROM_METHOD, result.getArgumentSource());
        assertEquals(true, stringConcatResult.isEmpty());
        assertEquals(false, argumentSourceAnalysis.isBranching());

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    //Multiple sources
    @Test
    public void shouldAnalyzeIfBranchFile1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/field/multipleSources/ifBranch1"));
        ClassType classType = view.getIdentifierFactory().getClassType("FieldFileIfBranch1");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "caller", "void", Collections.emptyList()))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(7);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        List<ArgumentSource> sources = argumentSourceAnalysis.getFlowBefore(stmtGraph.getStartingStmt()).stream()
                                                             .map(result1 -> result1.getArgumentSource())
                                                             .collect(Collectors.toUnmodifiableList());
        assertTrue(CollectionUtils.isEqualCollection(sources, Arrays.asList(LOCAL, RETURN_FROM_METHOD)));
        assertEquals(true, stringConcatResult.isEmpty());
        assertEquals(true, argumentSourceAnalysis.isBranching());

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeIfBranchFile2_ERROR_BRANCHING_AND_STRINGCONCAT() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/field/multipleSources/ifBranch2"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }
}
