package de.upb.sse.cutNRun.slicer;

import com.ibm.wala.classLoader.Language;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.core.util.config.AnalysisScopeReader;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.util.CallGraphSearchUtil;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.SlicerUtil;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.shrike.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Component
@AllArgsConstructor
public class Slicer {

    private Printer printer;

    public void slice(String appJar,
                      String mainClass,
                      String methodOfInterest,
                      String methodOfInterestCaller) throws CancelException, IOException, ClassHierarchyException {

        com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions dataDependenceOptions = com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions.FULL;
        com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions controlDependenceOptions = com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions.FULL;
        AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(
                appJar, null); //Exclusion file can added to speedup analysis eg: new FileProvider().getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS)
        /*AnalysisScope scope = AnalysisScopeReader.instance.readJavaScope(
                appJar, null);
        IClassHierarchy cha = findOrCreateCHA(scope);*/
        ClassHierarchy cha = ClassHierarchyFactory.make(scope);
        Iterable<Entrypoint> entrypoints =
                com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(cha);
        /*Iterable<Entrypoint> entrypoints =
                com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(cha, TestConstants.SLICE1_MAIN);*/
        AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);

        CallGraphBuilder<InstanceKey> builder =
                Util.makeVanillaZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha);
        CallGraph cg = builder.makeCallGraph(options, null);
        SDG<InstanceKey> sdg = new SDG<>(cg, builder.getPointerAnalysis(), dataDependenceOptions, controlDependenceOptions);

        /*CGNode main = CallGraphSearchUtil.findMainMethod(cg);

        Statement s = SlicerUtil.findCallTo(main, "println");*/
        CGNode callerNode = CallGraphSearchUtil.findMethod(cg, methodOfInterestCaller);
        Statement s = SlicerUtil.findCallTo(callerNode, methodOfInterest);
        System.err.println("Statement: " + s);
        // compute a data slice
        final PointerAnalysis<InstanceKey> pointerAnalysis = builder.getPointerAnalysis();
        Collection<Statement> computeBackwardSlice =
                com.ibm.wala.ipa.slicer.Slicer.computeBackwardSlice(
                        s, cg, pointerAnalysis, dataDependenceOptions, controlDependenceOptions);
        Collection<Statement> slice = computeBackwardSlice;
        System.out.println("----------------------- Final Slice --------------------");
        SlicerUtil.dumpSlice(slice);
        Graph<Statement> g = GraphSlicer.prune(sdg, slice::contains);

        /*System.out.println("----------------------- Final SDG --------------------");
        System.out.println(g);*/
        //TODO: convert slice statements to readable java source code
        /*try {
            printer.printPath(slice.stream().toList());
        } catch (InvalidClassFileException e) {
            throw new RuntimeException(e);
        }*/

        //sanityCheck(slice, g);
    }
}
