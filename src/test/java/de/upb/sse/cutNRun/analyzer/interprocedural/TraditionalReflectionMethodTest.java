package de.upb.sse.cutNRun.analyzer.interprocedural;

import de.upb.sse.cutNRun.analyzer.ProgramAnalyzerAdaptor;
import de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource;
import de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSourceAnalysis;
import de.upb.sse.cutNRun.analyzer.intraprocedural.Result;
import de.upb.sse.cutNRun.analyzer.intraprocedural.StringConcatenationSource;
import de.upb.sse.cutNRun.analyzer.soot.BackwardsInterproceduralCFG;
import heros.InterproceduralCFG;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.analysis.interprocedural.ide.JimpleIDESolver;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.views.JavaView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraditionalReflectionMethodTest {

    @Test
    public void shouldAnalyzeSimpleReflectionCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/interprocedural/method/testFileSimple/Simple"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeSimpleInterProceduralReflectionCall() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/parameter/File1"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeSimpleInterProceduralReflectionCall1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/interprocedural/method/simpleInterProcedural"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeSimpleMethodParameters() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/interprocedural/method/parameter"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeSimpleReturnFromMethod() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/interprocedural/method/returnFromMethod"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeNonStaticFieldAccess() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/interprocedural/method/field/sameClass/nonStaticField"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    //TOdo: test fails- logic for static fields not implemented(for potential solution refer diary)
    @Test
    public void shouldAnalyzeStaticFieldAccess() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/interprocedural/method/field/sameClass/staticField"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    //TODO: below commented are stringconcat tests
    /*@Test
    public void shouldAnalyzeTestFile1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/File1"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeTestFile2() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/File2"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeTestFile3() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/File3"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeTestFile4() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/File4"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeTestFile6() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/File6"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeTestJar() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/testJars/CutNRunTestJars.jar"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }*/

    //Parameter tests
    @Test
    public void shouldAnalyzeParameterTestFile1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/parameter/File1"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeParameterTestFile2() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/parameter/File2"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    //method return value tests
    @Test
    public void shouldAnalyzeReturnValueTestFile3() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/parameter/File3"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeReturnValueTestFile4() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/parameter/File4"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeReturnValueTestFile5() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/parameter/File5"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    //field tests
    @Test
    public void shouldAnalyzeFieldTestFile1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/field/File1"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeFieldTestFile2() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/field/File2"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    //TODO: doesn't work - static field logic not written
    @Test
    public void shouldAnalyzeFieldTestFile3() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/field/File3"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    /*
    //multiple factors tests -concat
    @Test
    public void shouldAnalyzeMultipleFactorsTestStringConcat1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/multipleSources/StringConcat1"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test //test only for 2 factors - concat logic
    public void shouldAnalyzeMultipleFactorsTestStringConcat2() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/multipleSources/StringConcat2"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeMultipleFactorsTestStringConcat3() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/multipleSources/StringConcat3"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeMultipleFactorsTestStringConcat4() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/multipleSources/StringConcat4"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }
*/

    //If branch tests
    @Test
    public void shouldAnalyzeIfBranchTest1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/interprocedural/method/branching/ifBranch1"));
        /*ClassType classType = view.getIdentifierFactory().getClassType("MethodIfBranch1");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "caller", "void", Collections.emptyList()))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(8);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        //final List<MethodSignature> startMethod = Collections.singletonList(methodWithReflection.getSignature());
        //final List<MethodSignature> mainMethodEntryPoints = getMainMethodEntryPoints();
        final List<MethodSignature> cgEntryPoints = view.getClasses()
                                                        .flatMap(sootClass -> sootClass.getMethods().stream())
                                                        .filter(method -> method.isPublic())
                                                        .filter(method -> method.hasBody())
                                                        .map(SootMethod::getSignature)
                                                        .collect(Collectors.toList());

        JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG(
                (new ClassHierarchyAnalysisAlgorithm(view)).initialize(cgEntryPoints)
                , view, false, true);     //new JimpleBasedInterproceduralCFG(view, entryPoints, false, true);
        BackwardsInterproceduralCFG backwardICFG = new BackwardsInterproceduralCFG(icfg, view);

        IDEValueAnalysisProblem problem = new IDEValueAnalysisProblem(backwardICFG, null, startStmt, (JavaView) view);
        JimpleIDESolver<Value, String, InterproceduralCFG<Stmt, SootMethod>> solver = new JimpleIDESolver<>(problem);
        solver.solve();
        Map<Value, String> result = solver.resultsAt(backwardICFG.getEndPointsOf(problem.getMethodConsistingResult())
                                                                 .stream().findFirst().get());*/
        //assertEquals(result.size(), 1);
        //assertEquals("execute", result.get(JavaJimple.newLocal("hardCoddedResult", view.getIdentifierFactory().getClassType("java.lang.String"))));

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeIfBranchTest2() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/multipleSources/ifBranch2"));
        /*ClassType classType = view.getIdentifierFactory().getClassType("Main");
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
                                                  System.out.println("before: " + argumentSourceAnalysis.getFlowAfter(stmt));
                                                  System.out.println("after: " + argumentSourceAnalysis.getFlowBefore(stmt));
                                                  System.out.println("====================");
                                              });
        assertEquals(argumentSourceAnalysis.getFlowBefore(stmtGraph.getStartingStmt()).size(), 2);
        List<ArgumentSource> sources = argumentSourceAnalysis.getFlowBefore(stmtGraph.getStartingStmt()).stream()
                                                             .map(result1 -> result1.getArgumentSource())
                                                             .collect(Collectors.toUnmodifiableList());
        assertTrue(CollectionUtils.isEqualCollection(sources, Arrays.asList(RETURN_FROM_METHOD, RETURN_FROM_METHOD)));
        assertTrue(stringConcatResult.isEmpty());*/

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeIfBranchTest3_ERROR_BRANCHING_AND_STRINGCONCAT() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/multipleSources/ifBranch3"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    //Test string new keyword
    @Test
    public void shouldAnalyzeStingNewKeyword() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/StringNewKeyword/basicNewKeyword"));
        /*ClassType classType = view.getIdentifierFactory().getClassType("StringConcat");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "test", "void", List.of("java.lang.String")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(6);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(LOCAL, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());*/

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeStingNewKeyword2() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/StringNewKeyword/basicNewKeyword2"));
        /*ClassType classType = view.getIdentifierFactory().getClassType("StringConcat");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "test", "void", List.of("java.lang.String")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(10);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(LOCAL, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());*/

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeStringNewKeywordWithMethodReturnSource() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/StringNewKeyword/basicNewKeyword-methodReturn"));
        /*ClassType classType = view.getIdentifierFactory().getClassType("StringConcat");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "test", "void", List.of("java.lang.String")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(7);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(RETURN_FROM_METHOD, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());*/

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    //TODO: logic not written
    /*@Test
    public void shouldAnalyzeStringNewKeywordWithDifferentConstructors() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/StringNewKeyword/DifferentConstructors"));
        *//*ClassType classType = view.getIdentifierFactory().getClassType("StringConcat");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "test", "void", List.of("java.lang.String")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(8);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(RETURN_FROM_METHOD, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());*//*

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }*/

    @Test
    public void shouldAnalyzeStringNewKeywordForEmptyString() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/StringNewKeyword/stringEmpty"));
        ClassType classType = view.getIdentifierFactory().getClassType("StringConcat");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "test", "void", List.of("java.lang.String")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(8);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(UNKNOWN, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

   /* @Test
    public void shouldAnalyzeStringNewKeywordWithStringConcat() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/StringNewKeyword/stringConcat"));
        ClassType classType = view.getIdentifierFactory().getClassType("StringConcat");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "test", "void", List.of("java.lang.String")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(5);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertEquals(UNKNOWN, result.getArgumentSource());
        assertEquals(false, stringConcatResult.isEmpty());
        assertTrue(CollectionUtils.isEqualCollection(stringConcatResult.getArgumentSources(), Arrays.asList(LOCAL, RETURN_FROM_METHOD)));

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }*/

    /*@Test
    public void shouldAnalyzeStringBuffer() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/StringBufferAndBuilder"));
        ClassType classType = view.getIdentifierFactory().getClassType("StringBufferBuilder");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "test", "void", List.of("java.lang.String")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(7);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertTrue(stringConcatResult.isEmpty());
        assertEquals(RETURN_FROM_METHOD, result.getArgumentSource());
        assertEquals(false, argumentSourceAnalysis.isBranching());

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }*/

    @Test
    public void testing() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/testJars/junit-4.13.2.jar"));

        /*ClassType classType = view.getIdentifierFactory().getClassType("org.apache.commons.lang3.ClassUtils");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "getPublicMethod", "java.lang.reflect.Method", List.of(
                                                           "java.lang.Class","java.lang.String", "java.lang.Class[]")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(27);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        stmtGraph.getStmts().stream().forEach(stmt->
                                              {
                                                  System.out.println("====================");
                                                  System.out.println("Stmt: [" + stmt + "]");
                                                  System.out.println("before: " + argumentSourceAnalysis.getFlowAfter(stmt));
                                                  System.out.println("after: " + argumentSourceAnalysis.getFlowBefore(stmt));
                                                  System.out.println("====================");
                                              });
        System.out.println("Testing");
        assertEquals(argumentSourceAnalysis.getFlowBefore(stmtGraph.getStartingStmt()).size(), 2);
        List<ArgumentSource> sources = argumentSourceAnalysis.getFlowBefore(stmtGraph.getStartingStmt()).stream()
                                                             .map(result1 -> result1.getArgumentSource())
                                                             .collect(Collectors.toUnmodifiableList());
        assertTrue(CollectionUtils.isEqualCollection(sources, Arrays.asList(RETURN_FROM_METHOD, LOCAL)));
        //assertEquals(false, stringConcatResult.isEveryStringFromSameSource());
        //assertEquals(2, stringConcatResult.getArgumentSources().size());
        assertTrue(stringConcatResult.isEmpty());
        //assertEquals(UNKOWN, result.getArgumentSource());*/

        //For manual testing
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }
}