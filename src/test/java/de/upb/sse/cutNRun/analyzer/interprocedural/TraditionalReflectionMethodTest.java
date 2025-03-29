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
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.views.JavaView;

import java.util.*;
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

    @Test
    public void shouldAnalyzeStaticFieldTestFile3() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/method/field/File3"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeStaticFieldFromDifferentClass() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/interprocedural/method/field/staticField"));
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
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/testJars/glide-4.16.0.jar"));

        /*ClassType classType = view.getIdentifierFactory().getClassType("com.google.common.collect.Serialization");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "getFieldSetter", "com.google.common.collect.Serialization$FieldSetter",
                                                                       List.of("java.lang.Class", "java.lang.String")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(2);

        final List<MethodSignature> cgEntryPoints = view.getClasses()
                                                        .flatMap(sootClass -> sootClass.getMethods().stream())
                                                        .filter(sootMethod1 -> sootMethod1.isPublic())
                                                        //.filter(sootMethod -> sootMethod.hasBody())
                                                        .map(SootMethod::getSignature)
                                                        .collect(Collectors.toList());
        JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG(
                (new ClassHierarchyAnalysisAlgorithm(view)).initialize(cgEntryPoints)
                , view, false, true);
        BackwardsInterproceduralCFG backwardICFG = new BackwardsInterproceduralCFG(icfg, view);
        List<JavaSootMethod> cgEntryPointMethods = (List<JavaSootMethod>) icfg.getCg().getEntryMethods().stream()
                                                                              .map(methodSignature -> view.getMethod(methodSignature).get())
                                                                              .collect(Collectors.toList());
        Map<DFF, String> staticFieldsValueMap = collectInitializedStaticField(view);
        IDEValueAnalysisProblemDFF problem = new IDEValueAnalysisProblemDFF(backwardICFG, cgEntryPointMethods, startStmt, (JavaView) view, icfg.getCg(), staticFieldsValueMap);
        JimpleIDESolver<DFF, Set<String>, InterproceduralCFG<Stmt, SootMethod>> solver = new JimpleIDESolver<>(problem);
        solver.solve();

        List<Stmt> stmtsToCheckResults = problem.getMethodsConsistingResult().stream()
                                                .flatMap(method -> backwardICFG.getEndPointsOf(method).stream())
                                                .collect(Collectors.toList());
        Set<String> filteredResultValues = new HashSet<>();
        Map<DFF, Set<String>> rawResult = new HashMap<>();
        for (Stmt stmt : stmtsToCheckResults) {
            Map<DFF, Set<String>> result = solver.resultsAt(stmt);
            if (!result.isEmpty()) {
                rawResult.putAll(result);
                for (DFF key : result.keySet()) {
                    Set<String> values = result.get(key);
                    System.out.println("RESULT: " + key + "=" + values);
                    if (CollectionUtils.isNotEmpty(values) && !values.equals(new HashSet<>(Arrays.asList("<<TOP>>")))) {
                        filteredResultValues.addAll(values);//stringBuffer.append(values.toString());
                    }
                }
            }
        }*/

        //For manual testing
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    @Test
    public void shouldAnalyzeAbstractClass() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/interprocedural/method/field/file6"));

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    //Scenario in Junit jar
    @Test
    public void shouldAnalyzeJunitExample() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/interprocedural/method/field/testFile-setFieldInSetter"));
        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }

    private Map<DFF, String> collectInitializedStaticField(View view) {
        List<SootMethod> clinitMethods = view.getClasses()
                                             .flatMap(sootClass -> sootClass.getMethods().stream())
                                             .filter(method -> method.getName().equals("<clinit>"))
                                             .collect(Collectors.toList());

        List<Stmt> abstractDefinitionStmts = clinitMethods.stream()
                                                          .flatMap(method -> method.getBody().getStmts().stream())
                                                          .filter(stmt -> stmt instanceof AbstractDefinitionStmt)
                                                          .collect(Collectors.toList());

        Map<DFF, String> staticFieldValueMap = new HashMap<>();

        for (Stmt definitionStmt : abstractDefinitionStmts) {
            AbstractDefinitionStmt stmt = (AbstractDefinitionStmt) definitionStmt;
            Value leftOp = stmt.getLeftOp();
            Value rightOp = stmt.getRightOp();
            if (leftOp instanceof JStaticFieldRef && rightOp instanceof Constant) {
                String value = null;
                if (rightOp instanceof StringConstant) {
                    value = ((StringConstant) rightOp).getValue();
                    staticFieldValueMap.put(DFF.asDFF(leftOp, view), value);
                } else if (rightOp instanceof ClassConstant) {
                    value = ((ClassConstant) rightOp).getValue();
                    staticFieldValueMap.put(DFF.asDFF(leftOp, view), value);
                }
            }
        }
        return staticFieldValueMap;
    }
}