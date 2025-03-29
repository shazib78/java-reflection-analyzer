package de.upb.sse.cutNRun.analyzer;

import de.upb.sse.cutNRun.analyzer.interprocedural.DFF;
import de.upb.sse.cutNRun.analyzer.interprocedural.IDEValueAnalysisProblem;
import de.upb.sse.cutNRun.analyzer.interprocedural.IDEValueAnalysisProblemDFF;
import de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource;
import de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSourceAnalysis;
import de.upb.sse.cutNRun.analyzer.intraprocedural.Result;
import de.upb.sse.cutNRun.analyzer.intraprocedural.StringConcatenationSource;
import de.upb.sse.cutNRun.analyzer.methodSignature.ModernReflectionMethodSignature;
import de.upb.sse.cutNRun.analyzer.methodSignature.TraditionalReflectionMethodSignature;
import de.upb.sse.cutNRun.analyzer.methodSignature.UnsoundMethodSignatureCategory;
import de.upb.sse.cutNRun.analyzer.soot.BackwardsInterproceduralCFG;
import de.upb.sse.cutNRun.dataRecorder.ExcelWriterAdapter;
import de.upb.sse.cutNRun.dataRecorder.ExcelWriterPort;
import heros.InterproceduralCFG;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.analysis.interprocedural.ide.JimpleIDESolver;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

import java.util.*;
import java.util.stream.Collectors;

import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.ERROR_BRANCHING_AND_STRINGCONCAT;
import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.UNKNOWN;

@Slf4j
public class ProgramAnalyzerAdaptor implements ProgramAnalyzerPort {
    private View view;
    private List<UnsoundMethodSignatureCategory> unsoundMethodSignatureCategories;
    private String jarName;

    public ProgramAnalyzerAdaptor(View view, String jarName) {
        this.view = view;
        this.unsoundMethodSignatureCategories = List.of(new TraditionalReflectionMethodSignature(view),
                                                        new ModernReflectionMethodSignature(view));
        this.jarName = jarName;
    }

    @Override
    public void analyze() {
        ExcelWriterPort excelWriter = new ExcelWriterAdapter("SourcesOfUnsoundnessCount", false);
        excelWriter.setHeaders("ProjectName", "totalTraditionalReflection", "totalModernReflection",
                               "totalSourcesOfUnsoundness", "isError", "errorMessage",
                               "traditionalReflection-METHOD", "traditionalReflection-NEW-INSTANCE", "traditionalReflection-FIELD", "totalTraditionalReflection",
                               "modernReflection-METHOD", "modernReflection-NEW-INSTANCE", "modernReflection-FIELD", "totalModernReflection");
        Map<String, Object[]> excelData = new LinkedHashMap<>();
        try {
            //TODO: uncomment for RQ1
            //if (!excelWriter.isJarWritten(jarName)) {
            int totalSourcesOfUnsoundnessCount = 0;
            for (SootClass sootClass : view.getClasses().toList()) {
                for (SootMethod sootMethod : sootClass.getMethods()) {
                    System.out.println("method: " + sootMethod.getSignature());
                    List<Stmt> statements = sootMethod.hasBody() ? sootMethod.getBody().getStmts() : Collections.emptyList();
                    //System.out.println("method Statements:" + statements);
                    List<Stmt> unsoundStatements = statements.stream()
                                                             .filter(statement -> isSourceOfUnsoundness(statement))
                                                             .collect(Collectors.toList());
                    if (!unsoundStatements.isEmpty()) {
                        //TODO: uncomment
                        //getEntryPoints();
                        log.debug("------------START--------------");
                        log.debug("method: " + sootMethod.getSignature());
                        log.debug("Sources of Unsoundness - Statements:");
                        unsoundStatements.stream().forEach(stmt -> log.debug(stmt.toString()));
                        log.debug("Sources of Unsoundness - Count: " + unsoundStatements.size());
                        log.debug("------------END--------------");
                        totalSourcesOfUnsoundnessCount = totalSourcesOfUnsoundnessCount + unsoundStatements.size();
                        //TODO: uncomment for intraprocedural Analysis
                            /*unsoundStatements.stream()
                                             .forEach(stmt -> performIntraProceduralAnalysis(sootMethod, stmt));*/
                        //TODO: uncomment for interprocedural Analysis
                        unsoundStatements.stream()
                                         .forEach(stmt -> performInterProceduralAnalysis(sootMethod, stmt));
                    }
                }
                    /*for(MethodSignature  : methodSignaturesToSearch)
                    if (!sootClass.getMethod(methodSignature.getSubSignature()).isPresent()) {
                        System.out.println("Method not found!");
                        return;  // Exit if the method is not found
                    }*/
            }
            log.info("Sources of Unsoundness - Total Count: " + totalSourcesOfUnsoundnessCount);
            setRQ1ExcelData(excelData, totalSourcesOfUnsoundnessCount);
            //TODO: uncomment for RQ1
            //}
        } catch (Exception e) {
            e.printStackTrace();
            excelData.put(jarName, new Object[]{jarName, "", "", "", "ERROR", e.getMessage(), "", "", "", ""});
        } finally {
            //TODO: uncomment for RQ1
            //excelWriter.saveData(excelData);
        }
    }

    private void setRQ1ExcelData(Map<String, Object[]> excelData, int totalSourcesOfUnsoundnessCount) {
        int totalTraditionalReflectionCount = 0;
        int traditionalReflectionMethodCount = 0;
        int traditionalReflectionFieldCount = 0;
        int traditionalReflectionNewInstanceCount = 0;
        int totalModernReflectionCount = 0;
        int modernReflectionMethodCount = 0;
        int modernReflectionFieldCount = 0;
        int modernReflectionNewInstanceCount = 0;
        for (UnsoundMethodSignatureCategory unsoundMethodSignatureCategory : unsoundMethodSignatureCategories) {
            if (unsoundMethodSignatureCategory instanceof TraditionalReflectionMethodSignature) {
                TraditionalReflectionMethodSignature traditionalReflection = (TraditionalReflectionMethodSignature) unsoundMethodSignatureCategory;
                totalTraditionalReflectionCount = traditionalReflection.getTotalReflectionCount();
                traditionalReflectionMethodCount = traditionalReflection.getMethodReflectionCount();
                traditionalReflectionFieldCount = traditionalReflection.getFieldReflectionCount();
                traditionalReflectionNewInstanceCount = traditionalReflection.getNewInstanceReflectionCount();
            } else {
                ModernReflectionMethodSignature modernReflection = (ModernReflectionMethodSignature) unsoundMethodSignatureCategory;
                totalModernReflectionCount = unsoundMethodSignatureCategory.getTotalReflectionCount();
                modernReflectionMethodCount = modernReflection.getMethodReflectionCount();
                modernReflectionFieldCount = modernReflection.getFieldReflectionCount();
                modernReflectionNewInstanceCount = modernReflection.getNewInstanceReflectionCount();
            }
        }
        excelData.put(jarName, new Object[]{jarName, totalTraditionalReflectionCount, totalModernReflectionCount,
                String.valueOf(totalSourcesOfUnsoundnessCount), "", "",
                traditionalReflectionMethodCount, traditionalReflectionNewInstanceCount, traditionalReflectionFieldCount, totalTraditionalReflectionCount,
                modernReflectionMethodCount, modernReflectionNewInstanceCount, modernReflectionFieldCount, totalModernReflectionCount});
    }

    private void performInterProceduralAnalysis(SootMethod methodWithReflection, Stmt startStmt) {
        /*final ClassType classType = view.getIdentifierFactory().getClassType(targetTestClassName);
        final Optional<JavaSootClass> aClass = view.getClass(classType);
        if (!aClass.isPresent()) {
            throw new IllegalArgumentException("Entrypoint class is not in the View.");
        }*/
        //entryPointMethod = aClass.get().getMethods().stream().filter(SootMethod::hasBody).filter(ms -> ms.getName().equals("entryPoint")).findAny().get();
        log.info("----------------------------------------");
        log.info("Starting inter-procedural analysis");
        log.info("Entry point:" + methodWithReflection.toString());
        ExcelWriterPort excelWriter = new ExcelWriterAdapter("RQ3_Values-" + jarName.replace("./jars/","-"), false);
        excelWriter.setHeaders("statementId", "project", "statement", "Value", "Error", "Error message", "testing values");
        Map<String, Object[]> excelData = new LinkedHashMap<>();
        String statementId = jarName + "_" + methodWithReflection.getDeclaringClassType() + "_"
                + methodWithReflection.getSignature().getSubSignature().toString() + "_" + startStmt.getPositionInfo().getStmtPosition().getFirstLine();
        try {
            //if (!excelWriter.isJarWritten(jarName)) {
                final List<MethodSignature> startMethod = Collections.singletonList(methodWithReflection.getSignature());
                //final List<MethodSignature> mainMethodEntryPoints = getMainMethodEntryPoints();
                final List<MethodSignature> cgEntryPoints = view.getClasses()
                                                                .flatMap(sootClass -> sootClass.getMethods().stream())
                                                                .filter(sootMethod -> sootMethod.isPublic())
                                                                //.filter(sootMethod -> sootMethod.hasBody())
                                                                .map(SootMethod::getSignature)
                                                                .collect(Collectors.toList());

                JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG(
                        (new ClassHierarchyAnalysisAlgorithm(view)).initialize(cgEntryPoints)
                        , view, false, true);     //new JimpleBasedInterproceduralCFG(view, entryPoints, false, true);
                BackwardsInterproceduralCFG backwardICFG = new BackwardsInterproceduralCFG(icfg, view);
                //TODO: testing start
                log.info("testing statements start");
        /*List<Stmt> startPointStmt = (List<Stmt>) icfg.getStartPointsOf(view.getMethod(entryPoints.get(0)).orElse(null));
        List<Stmt> temp = icfg.getSuccsOf(startPointStmt.get(0));
        log.info(temp.toString());
        do{
            //if(!backwardICFG.isExitStmt(temp)) {
                temp = icfg.getSuccsOf(temp.get(0));
                log.info(temp.toString());
            //}else{
                //log.info(temp.toString());
                //break;
            //}
        }while (!temp.isEmpty());
        log.info("testing statements end");*/
                //TODO: testing end
                List<JavaSootMethod> cgEntryPointMethods = (List<JavaSootMethod>) icfg.getCg().getEntryMethods().stream()
                                                                                      .map(methodSignature -> view.getMethod(methodSignature).get())
                                                                                      .collect(Collectors.toList());
                Map<DFF, String> staticFieldsValueMap = collectInitializedStaticField();

                /*IDEValueAnalysisProblem problem = new IDEValueAnalysisProblem(backwardICFG, startMethod, startStmt, (JavaView) view);*/
                IDEValueAnalysisProblemDFF problem = new IDEValueAnalysisProblemDFF(backwardICFG, cgEntryPointMethods, startStmt,
                                                                                    (JavaView) view, icfg.getCg(), staticFieldsValueMap);
                JimpleIDESolver<DFF, Set<String>, InterproceduralCFG<Stmt, SootMethod>> solver = new JimpleIDESolver<>(problem);
                solver.solve();
                /*Map<Value, Set<String>> result = solver.resultsAt(backwardICFG.getEndPointsOf(problem.getMethodsConsistingResult())
                                                                         .stream().findFirst().get());*/
                /*Map<Value, String> result = solver.resultsAt(backwardICFG.getEndPointsOf(methodWithReflection).stream().findFirst().get());*/
                //Map<Local, String> result = solver.resultsAt(backwardICFG.getEndPointsOf(view.getMethod(mainMethodEntryPoints.get(0)).get())
                //                                                                 .stream().findFirst().get());

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
                            log.info("RESULT: {} = {}", key, values);
                            if (CollectionUtils.isNotEmpty(values) && !values.equals(new HashSet<>(Arrays.asList("<<TOP>>")))) {
                                filteredResultValues.addAll(values);//stringBuffer.append(values.toString());
                            }
                        }
                    }
                }
                excelData.put(statementId, new Object[]{statementId, jarName, startStmt, filteredResultValues, "", "", rawResult});
                log.info("FINAL RESULT: {}", filteredResultValues);
                //log.info("RESULT: {} = {}", result.keySet().stream().findFirst().get(), result.values().stream().findFirst().get());
                log.info("End of inter-procedural analysis");
            //}
        } catch (Exception e) {
            String stacktrace = ExceptionUtils.getStackTrace(e);
            e.printStackTrace();
            excelData.put(statementId, new Object[]{statementId, jarName, startStmt, "", "ERROR", stacktrace, ""});
        } finally {
            excelWriter.saveData(excelData);
        }
    }

    private Map<DFF, String> collectInitializedStaticField() {
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

    private List<MethodSignature> getMainMethodEntryPoints() {
        /*ExcelWriterPort excelWriter = new ExcelWriterAdapter("CallGraph_EntryPoints", false);
        excelWriter.setHeaders("Name", "No of entry points", "entryPoints");
        Map<String, Object[]> excelData = new LinkedHashMap<>();*/
        Collection<MethodSignature> entryPoints = view.getClasses()
                                                      .flatMap(sootClass -> sootClass.getMethods().stream())
                                                      .filter(sootMethod -> sootMethod.isMain(view.getIdentifierFactory()))//.getSubSignature().equals(buildMethodSignature("java.lang.String", "<init>", "void", Arrays.asList("char[]"), view);))
                                                      .map(SootMethod::getSignature)
                                                      .collect(Collectors.toSet());
        /*excelData.put(jarName,new Object[]{jarName, });
        excelWriter.saveData(excelData);*/
        if (entryPoints.size() != 1) {
            throw new RuntimeException("No single entry point. No. of entry points = " + entryPoints.size());
        }
        return entryPoints.stream().toList();
    }

    private void performIntraProceduralAnalysis(SootMethod sootMethod, Stmt startStmt) {
        log.info("Starting analysis for Method={} and StartStmt={}", sootMethod.getSignature().getSubSignature(), startStmt);
        ExcelWriterPort excelWriter = new ExcelWriterAdapter("RQ2_Factors", false);
        excelWriter.setHeaders("statementId", "project", "statement", "isSameFactor", "totalFactors", "isStringConcat", "Factors", "isBranching", "Error");
        Map<String, Object[]> excelData = new LinkedHashMap<>();
        String statementId = jarName + "_" + sootMethod.getDeclaringClassType() + "_"
                + sootMethod.getSignature().getSubSignature().toString() + "_" + startStmt.getPositionInfo().getStmtPosition().getFirstLine();
        try {
            if (!excelWriter.isJarWritten(jarName)) {
                StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();
                ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
                argumentSourceAnalysis.execute();
                Result result = argumentSourceAnalysis.getResult();
                ArgumentSource argumentSource = result.getArgumentSource();
                StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
                if (argumentSource == UNKNOWN && !stringConcatResult.isEmpty()) {
                    log.info("String Concatenation sources: {}", stringConcatResult.getArgumentSources());
                    log.info("Argument Source: {} for Statement: {}", stringConcatResult.getSource(), startStmt);
                    log.info("isSingleSource: {} allSourcesCount: {} allSources: {}", stringConcatResult.isEveryStringFromSameSource(),
                             stringConcatResult.getArgumentSources().size(), stringConcatResult.getArgumentSources());
                    excelData.put(statementId, new Object[]{statementId, jarName, startStmt, stringConcatResult.isEveryStringFromSameSource(),
                            stringConcatResult.getArgumentSources().size(), !stringConcatResult.isEmpty(),
                            stringConcatResult.getArgumentSources(), argumentSourceAnalysis.isBranching(), ""});

                } else if (stringConcatResult.isEmpty() && !argumentSourceAnalysis.isBranching()) {
                    log.info("Argument Source: {} for Statement: {}", argumentSource, startStmt);
                    log.info("isSingleSource: {} allSourcesCount: {} allSources: {}", true, 1, argumentSource);
                    if (argumentSource == UNKNOWN) {
                        log.error("Testing");
                    }
                    excelData.put(statementId, new Object[]{statementId, jarName, startStmt, true, 1, !stringConcatResult.isEmpty(),
                            Arrays.asList(argumentSource), argumentSourceAnalysis.isBranching(), ""});

                } else if (argumentSourceAnalysis.isBranching() && !stringConcatResult.isEmpty()) {
                    log.info("String Concatenation sources: {}", stringConcatResult.getArgumentSources());
                    log.info("Branching sources: {}", argumentSourceAnalysis.getFlowBefore(stmtGraph.getStartingStmt()).stream()
                                                                            .map(result1 -> result1.getArgumentSource())
                                                                            .collect(Collectors.toUnmodifiableList()));
                    log.info("Argument Source: {} for Statement: {}", ERROR_BRANCHING_AND_STRINGCONCAT, startStmt);
                    excelData.put(statementId, new Object[]{statementId, jarName, startStmt, "", "", "",
                            ERROR_BRANCHING_AND_STRINGCONCAT, "", "Logic not handled for existence of both branching and string concatenation"});

                } else if (argumentSourceAnalysis.isBranching() && stringConcatResult.isEmpty()) {
                    //log.info("String Concatenation sources: {}", stringConcatResult.getArgumentSources());
                    List<ArgumentSource> sources = argumentSourceAnalysis.getFlowBefore(stmtGraph.getStartingStmt()).stream()
                                                                         .map(result1 -> result1.getArgumentSource())
                                                                         .collect(Collectors.toUnmodifiableList());
                    log.info("Branching sources: {}", sources);
                    log.info("for Statement: {}", startStmt);
                    log.info("isSingleSource: {} allSourcesCount: {} allSources: {}", sources.stream().distinct().count() == 1, sources.size(),
                             sources);
                    if (sources.size() <= 0) {
                        log.error("Testing");
                    }
                    excelData.put(statementId, new Object[]{statementId, jarName, startStmt, sources.stream().distinct().count() == 1,
                            sources.size(), !stringConcatResult.isEmpty(),
                            sources, argumentSourceAnalysis.isBranching(), ""});
                } else {
                    log.error("DEFAULT else branch - UNKOWN source");
                    log.info("Argument Source: {} for Statement: {}", UNKNOWN, startStmt);
                    //log.info("isSingleSource: {} allSourcesCount: {} allSources: {}", true, 1, UNKOWN);
                    excelData.put(statementId, new Object[]{statementId, jarName, startStmt, "",
                            "", "", "UNKOWN", "", "UNKOWN"});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            excelData.put(statementId, new Object[]{statementId, jarName, startStmt, "", "", "", "ERROR", "", e.getMessage()});
        } finally {
            excelWriter.saveData(excelData);
        }
    }

    private boolean isSourceOfUnsoundness(Stmt statement) {
        if (statement instanceof JInvokeStmt) {
            JInvokeStmt jInvokeStmt = (JInvokeStmt) statement;
            AbstractInvokeExpr abstractInvokeExpr = jInvokeStmt.getInvokeExpr().orElse(null);
            if (abstractInvokeExpr instanceof JVirtualInvokeExpr) {
                /*JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr) statement.getInvokeExpr();*/
            /*boolean isUnsoundStatement = unsoundMethodSignatureTypes.stream()
                                       .map(methodSignatureType -> methodSignatureType.getSignatures())
                    .anyMatch(methodSignature -> methodSignature.contains(jVirtualInvokeExpr.getMethodSignature()));*/
                return isInvokeExpressionUnsound((JVirtualInvokeExpr) abstractInvokeExpr);
            }
        } else if (statement instanceof JAssignStmt) {
            JAssignStmt jAssignStmt = (JAssignStmt) statement;
            Value rightOp = jAssignStmt.getRightOp();
            if (rightOp instanceof JVirtualInvokeExpr) {
                return isInvokeExpressionUnsound((JVirtualInvokeExpr) rightOp);
            }
        }
        return false;
    }

    private boolean isInvokeExpressionUnsound(JVirtualInvokeExpr jVirtualInvokeExpr) {
        return unsoundMethodSignatureCategories
                .stream()
                .anyMatch(category -> category.isSourceOfUnsoundnessAndIncreaseCount(jVirtualInvokeExpr.getMethodSignature()));
    }
}
