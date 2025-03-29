package de.upb.sse.cutNRun.analyzer.interprocedural;

import boomerang.BackwardQuery;
import boomerang.Boomerang;
//import boomerang.DefaultBoomerangOptions;
import boomerang.options.BoomerangOptions;
import boomerang.results.BackwardBoomerangResults;
/*import boomerang.scene.*;
import boomerang.scene.jimple.*;
import boomerang.scene.sparse.SparseCFGCache;*/
import boomerang.scope.ControlFlowGraph;
import boomerang.scope.DataFlowScope;
import boomerang.scope.Statement;
//import boomerang.scope.sootup.SootUpCallGraph;
import boomerang.scope.sootup.SootUpFrameworkScope;
import boomerang.scope.sootup.jimple.JimpleUpMethod;
import boomerang.scope.sootup.jimple.JimpleUpStatement;
import boomerang.scope.sootup.jimple.JimpleUpVal;
import boomerang.solver.Strategies;
import boomerang.util.AccessPath;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*import soot.Scene;
import soot.SootClass;
import soot.Type;*/
import sootup.callgraph.CallGraph;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.views.View;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;
import sparse.SparsificationStrategy;
import wpds.impl.Weight;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class SparseAliasManager {

    private static Logger log = LoggerFactory.getLogger(SparseAliasManager.class);

    private static SparseAliasManager INSTANCE;

    private LoadingCache<BackwardQuery, Set<AccessPath>> queryCache;

    private Boomerang boomerangSolver;

    private CallGraph sootCallGraph;
    private DataFlowScope dataFlowScope;

    private boolean disableAliasing = false;
    private SparsificationStrategy sparsificationStrategy;
    private boolean ignoreAfterQuery;
    private View view;
    private Collection<JavaSootMethod> entryPoints;


    /*static class CustomBoomerangOptions extends BoomerangOptions {

        private SparsificationStrategy sparsificationStrategy;
        private boolean ignoreAfterQuery;

        public CustomBoomerangOptions(SparsificationStrategy sparsificationStrategy, boolean ignoreAfterQuery){
            this.sparsificationStrategy = sparsificationStrategy;
            this.ignoreAfterQuery = ignoreAfterQuery;
        }

        @Override
        public SparsificationStrategy getSparsificationStrategy() {
            if(this.sparsificationStrategy==null){
                return SparsificationStrategy.NONE;
            }
            return this.sparsificationStrategy;
        }

        @Override
        public boolean ignoreSparsificationAfterQuery() {
            return this.ignoreAfterQuery;
        }

        @Override
        public int analysisTimeoutMS() {
            return 1000;
        }

        @Override
        public boolean onTheFlyCallGraph() {
            return false;
        }

        @Override
        public Strategies.StaticFieldStrategy getStaticFieldStrategy() {
            return Strategies.StaticFieldStrategy.SINGLETON;
        }

        @Override
        public boolean allowMultipleQueries() {
            return true;
        }

        @Override
        public boolean throwFlows() {
            return true;
        }

        @Override
        public boolean trackAnySubclassOfThrowable() {
            return true;
        }
    }*/

    private static Duration totalAliasingDuration;

    private SparseAliasManager(SparsificationStrategy sparsificationStrategy, boolean ignoreAfterQuery,
                               View view, CallGraph callGraph, Collection<JavaSootMethod> entryPoints) {
        this.sparsificationStrategy = sparsificationStrategy;
        this.ignoreAfterQuery = ignoreAfterQuery;
        totalAliasingDuration = Duration.ZERO;
        this.view = view;
        sootCallGraph = callGraph;
        this.entryPoints = entryPoints;
        dataFlowScope = DataFlowScope.EXCLUDE_PHANTOM_CLASSES;//SootDataFlowScope.make(Scene.v());
        setupQueryCache();
    }

    public static Duration getTotalDuration() {
        return totalAliasingDuration;
    }

    public static synchronized SparseAliasManager getInstance(SparsificationStrategy sparsificationStrategy, boolean ignoreAfterQuery,
                                                              View view, CallGraph callGraph, Collection<JavaSootMethod> entryPoints) {
        if (INSTANCE == null || INSTANCE.sparsificationStrategy!=sparsificationStrategy || INSTANCE.ignoreAfterQuery!=ignoreAfterQuery) {
            INSTANCE = new SparseAliasManager(sparsificationStrategy, ignoreAfterQuery, view, callGraph, entryPoints);
        }
        return INSTANCE;
    }

    private void setupQueryCache() {
        queryCache =
                CacheBuilder.newBuilder()
                        .build(
                                new CacheLoader<BackwardQuery, Set<AccessPath>>() {
                                    @Override
                                    public Set<AccessPath> load(BackwardQuery query) throws Exception {
                                        Set<AccessPath> aliases = queryCache.getIfPresent(query);
                                        if (aliases == null) {
                                            // TODO: stabilize null pointer exception that happens sometimes in boomerang
                                            BoomerangOptions boomerangOptions = BoomerangOptions.builder()
                                                                                                .withSparsificationStrategy(INSTANCE.sparsificationStrategy)
                                                                                                .enableIgnoreSparsificationAfterQuery(INSTANCE.ignoreAfterQuery)
                                                                                                .withAnalysisTimeout(1000)
                                                                                                .enableOnTheFlyCallGraph(false)
                                                                                                .withStaticFieldStrategy(Strategies.StaticFieldStrategy.SINGLETON)
                                                                                                .enableAllowMultipleQueries(true)
                                                                                                .build();
                                            boomerangSolver =
                                                    new Boomerang(new SootUpFrameworkScope((JavaView) view, sootCallGraph, entryPoints, dataFlowScope)/*sootCallGraph, dataFlowScope*/, boomerangOptions);
                                            BackwardBoomerangResults<Weight.NoWeight> results = boomerangSolver.solve(query);
                                            aliases = results.getAllAliases();
                                            boolean debug = false;
                                            if (debug) {
                                                System.out.println(query);
                                                System.out.println("alloc:" + results.getAllocationSites());
                                                System.out.println("aliases:" + aliases);
                                            }//boomerangSolver.unregisterAllListeners();
                                            //boomerangSolver.unregisterAllListeners();
                                            queryCache.put(query, aliases);
                                        }
                                        return aliases;
                                    }
                                });
    }


    /**
     * @param stmt   Statement that contains the value. E.g. Value can be the leftOp
     * @param method Method that contains the Stmt
     * @param value  We actually want to find this local's aliases
     * @return
     */
    public synchronized Set<AccessPath> getAliases(Stmt stmt, SootMethod method, Value value) {
        log.info(method.getBody().toString());
        log.info("getAliases call for: " + stmt + " in " + method);
        if (disableAliasing) {
            return Collections.emptySet();
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        BackwardQuery query = createQuery(stmt, method, value);
        Set<AccessPath> aliases = getAliases(query);
        Duration elapsed = stopwatch.elapsed();
        totalAliasingDuration = totalAliasingDuration.plus(elapsed);
        return aliases;
    }


//    public synchronized Set<AccessPath> getAliases(Stmt stmt, SootMethod method, Value value, Set<AccessPath> allAliases, JimpleField field) {
//        if (allAliases == null) {
//            allAliases = new HashSet<>();
//        }
//        log.info("getAliases call for: " + stmt + " in " + method);
//        if (disableAliasing) {
//            return Collections.emptySet();
//        }
//        Stopwatch stopwatch = Stopwatch.createStarted();
//        BackwardQuery query = createQuery(stmt, method, value);
//        Set<AccessPath> aliases = getAliases(query);
//        for (AccessPath alias : aliases) {
//            if(field!=null && !alias.getFields().contains(field)){
//                alias.getFields().add(field);
//            }
//            if (!allAliases.contains(alias)) {
//                Val baseVal = alias.getBase();
//                if (baseVal instanceof JimpleVal) {
//                    Local aliasBase = (Local) ((JimpleVal) baseVal).getDelegate();
//                    //((JimpleField) ((ArrayList) alias.fieldChain).get(0)).delegate
//                    if (!aliasBase.equals(value)) {
//
//                        allAliases.add(alias);
//                        JimpleField currentField = null;
//                        if(alias.getFields().size()>0){
//                            //TODO: what about deeper fields
//                            currentField = (JimpleField) alias.getFields().stream().findFirst().get();
//                        }
//                        getAliases(stmt, method, aliasBase, allAliases, currentField);
//                    }
//                }
//            }
//        }
//
//        Duration elapsed = stopwatch.elapsed();
//        totalAliasingDuration = totalAliasingDuration.plus(elapsed);
//        log.info("getAliases took: " + elapsed.getSeconds() + " - " + elapsed.getNano());
//
//        return aliases;
//    }


    private BackwardQuery createQuery(Stmt stmt, SootMethod method, Value value) {
        /*List<Type> sootParams = new ArrayList<>();
        for (sootup.core.types.Type paramType : method.getParameterTypes()) {
            sootParams.add(Scene.v().getType(paramType.toString()));
        }

        int sootModifiers  = 0;
        for (MethodModifier methodModifier: method.getModifiers()){
            sootModifiers = sootModifiers | methodModifier.getBytecode();
        }

        List<SootClass> thrownExceptions = new ArrayList<>();
        for (ClassType exceptionType : method.getExceptionSignatures()) {
            thrownExceptions.add(Scene.v().getSootClass(exceptionType.getFullyQualifiedName()));
        }*/
        JimpleUpMethod jimpleUpMethod = boomerang.scope.sootup.jimple.JimpleUpMethod.of((JavaSootMethod) method);
        boomerang.scope.Statement jimpleUpStatement = JimpleUpStatement.create(stmt, jimpleUpMethod);
        JimpleUpVal val = new JimpleUpVal(value, jimpleUpMethod);
            Optional<Statement> first = jimpleUpStatement.getMethod().getControlFlowGraph().getSuccsOf(jimpleUpStatement).stream().findFirst();
            if(first.isPresent()){
                return BackwardQuery.make(new ControlFlowGraph.Edge(jimpleUpStatement, first.get()), val);
            }
            throw new RuntimeException("No successors for: " + jimpleUpStatement);
    }

    private Set<AccessPath> getAliases(BackwardQuery query) {
        try {
            return queryCache.get(query);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (UncheckedExecutionException e) {
            e.printStackTrace();
        }
        return Collections.emptySet();
    }

   /* public static boolean isTargetDFFOrAlias(Stmt stmt, SootMethod method, Value base, DFF target) {
        if (target.toString().equals("<<zero>>")) {
            return false;
        }
        Set<AccessPath> aliases = SparseAliasManager.getInstance(SparseCFGCache.SparsificationStrategy.NONE, true).getAliases(stmt, method, base);
        for (AccessPath alias : aliases) {
            Val baseVal = alias.getBase();
            Value aliasBase = null;
            if (baseVal instanceof JimpleVal) {
                aliasBase = ((JimpleVal) baseVal).getDelegate();
            } else if (baseVal instanceof JimpleStaticFieldVal) {
                JimpleStaticFieldVal staticFieldVal = ((JimpleStaticFieldVal) baseVal);
                JimpleField field = (JimpleField) staticFieldVal.field();
                SootField sootField = field.getSootField();
                SootFieldRef sootFieldRef = sootField.makeRef();
                SherosStaticFieldRef staticFieldRef = new SherosStaticFieldRef(sootFieldRef);
                aliasBase = staticFieldRef;
            } else {
                return false;
            }
            Collection<Field> fields = alias.getFields();
            DFF aliasDFF;
            if (!fields.isEmpty()) {
                final List<SootField> accessPathFields = new ArrayList<>();
                for (final Field field : fields) {
                    if (field instanceof JimpleField) {
                        JimpleField jf = (JimpleField) field;
                        accessPathFields.add(jf.getSootField());
                    }
                }
                aliasDFF = new DFF(aliasBase, stmt, accessPathFields);
            } else {
                aliasDFF = new DFF(aliasBase, stmt);
            }
            if (aliasDFF.equals(target)) {
                return true;
            }
        }
        return false;
    }*/


}
