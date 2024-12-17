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
import java.util.stream.Collectors;

import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.RETURN_FROM_METHOD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TraditionalReflectionNewInstanceTest {
    @Test
    public void shouldAnalyzeFile1() {
        View view = new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/intraprocedural/NewInstance/File1"));
        /*ClassType classType = view.getIdentifierFactory().getClassType("NewInstanceFile1");
        SootMethod sootMethod = view.getMethod(view.getIdentifierFactory()
                                                   .getMethodSignature(classType, "caller", "void", List.of("java.lang.String")))
                                    .get();
        Stmt startStmt = sootMethod.getBody().getStmts().get(9);
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();

        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();

        Result result = argumentSourceAnalysis.getResult();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        assertEquals(argumentSourceAnalysis.getFlowBefore(stmtGraph.getStartingStmt()).size(), 2);
        List<ArgumentSource> sources = argumentSourceAnalysis.getFlowBefore(stmtGraph.getStartingStmt()).stream()
                                                             .map(result1 -> result1.getArgumentSource())
                                                             .collect(Collectors.toUnmodifiableList());
        assertTrue(CollectionUtils.isEqualCollection(sources, Arrays.asList(RETURN_FROM_METHOD, RETURN_FROM_METHOD)));
        assertTrue(stringConcatResult.isEmpty());*/

        ProgramAnalyzerAdaptor programAnalyzerPort = new ProgramAnalyzerAdaptor(view, "");
        programAnalyzerPort.analyze();
    }
}
