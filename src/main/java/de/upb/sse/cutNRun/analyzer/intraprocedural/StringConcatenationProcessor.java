package de.upb.sse.cutNRun.analyzer.intraprocedural;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.LinePosition;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.jimple.basic.JavaLocal;

import java.util.*;

import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.LOCAL;

@Slf4j
@AllArgsConstructor
public class StringConcatenationProcessor {

    private View view;
    private Value leftOp;
    private Set<Result> out;
    private Stmt stmt;
    private StringConcatenationSource stringConcatenationSource;

    public void process(Value rightOp) {
        JDynamicInvokeExpr invokeDynamic = (JDynamicInvokeExpr) rightOp;
        MethodSignature bootstrapMethodSignature = invokeDynamic.getBootstrapMethodSignature();
        MethodSignature stringConcatenationMethod = getStringConcatenationMethodSignature();
        if (bootstrapMethodSignature.equals(stringConcatenationMethod)) {
            MethodSignature invokeDynamicCallSiteSignature = invokeDynamic.getMethodSignature();
            MethodSignature stringConcatSignature = getInvokeDynamicConcatSignature(invokeDynamic.getArgs().size());
            if (!invokeDynamicCallSiteSignature.equals(stringConcatSignature)) {
                throw new RuntimeException("Unexpected Dynamic call during String Concatenation: " + invokeDynamicCallSiteSignature);
            }
            List<Immediate> bootstrapArgs = invokeDynamic.getBootstrapArgs();
            List<Immediate> args = invokeDynamic.getArgs();
            if (isEveryArgumentStringConstant(bootstrapArgs, args) && stringConcatenationSource.isEmpty()) {
                LinePosition linePosition = (LinePosition) stmt.getPositionInfo().getStmtPosition();
                //result.setStatementLineNumber(linePosition.getFirstLine());
                //result.setArgumentSource(INTRAPROCEDURAL);
                stringConcatenationSource.setArgumentSources(Collections.nCopies(bootstrapArgs.size()+args.size(),
                                                                                 LOCAL));
                out.add(Result.builder()
                              .statementLineNumber(linePosition.getFirstLine())
                              .argumentSource(ArgumentSource.LOCAL)
                              .build());
            } else if (!isEveryArgumentStringConstant(bootstrapArgs, args)) {
                trackStringConcatParameters(bootstrapArgs, args);
            } else if (isEveryArgumentStringConstant(bootstrapArgs, args)) {
                List<ArgumentSource> argumentSources = getArgumentSources(bootstrapArgs, args);
                stringConcatenationSource.getArgumentSources().addAll(argumentSources);
                if (!CollectionUtils.isEmpty(stringConcatenationSource.getNextVariablesToTrack())) {
                    stringConcatenationSource.getNextVariablesToTrack().remove(leftOp/*result.getTrackVariable()*/);
                }
            } else {
                log.error("Unknown scenario when tracking string concatenation variables");
            }
        }
    }

    private void trackStringConcatParameters(List<Immediate> bootstrapArgs, List<Immediate> args) {
        if (stringConcatenationSource.isEmpty()) {
            List<JavaLocal> javaLocals = getJavaLocalArguments(bootstrapArgs, args);
            stringConcatenationSource.setNextVariablesToTrack(javaLocals);
            stringConcatenationSource.setArgumentSources(getArgumentSources(bootstrapArgs, args));
            if (!javaLocals.isEmpty()) {
                //result.setTrackVariable(javaLocals.get(0));
                stringConcatenationSource.getNextVariablesToTrack().remove(leftOp/*result.getTrackVariable()*/);
            }

        } else {
            List<JavaLocal> javaLocals = getJavaLocalArguments(bootstrapArgs, args);
            List<ArgumentSource> argumentSources = getArgumentSources(bootstrapArgs, args);
            stringConcatenationSource.getArgumentSources().addAll(argumentSources);
            stringConcatenationSource.getNextVariablesToTrack().addAll(javaLocals);
            if (!CollectionUtils.isEmpty(stringConcatenationSource.getNextVariablesToTrack())) {
                //List<JavaLocal> nextVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();
                //result.setTrackVariable(nextVariablesToTrack.get(0));
                stringConcatenationSource.getNextVariablesToTrack().remove(leftOp/*result.getTrackVariable()*/);
            }
        }
    }

    private List<ArgumentSource> getArgumentSources(List<Immediate> bootstrapArgs, List<Immediate> args) {
        List<ArgumentSource> argumentSources = new ArrayList<>();
        for (Immediate immediate : bootstrapArgs) {
            if (immediate instanceof StringConstant) {
                argumentSources.add(LOCAL);
            }
        }
        for (Immediate immediate : args) {
            if (immediate instanceof StringConstant) {
                argumentSources.add(LOCAL);
            }
        }
        return argumentSources;
    }

    private List<JavaLocal> getJavaLocalArguments(List<Immediate> bootstrapArgs, List<Immediate> args) {
        List<JavaLocal> javaLocals = new ArrayList<>();
        for (Immediate immediate : bootstrapArgs) {
            if (immediate instanceof JavaLocal) {
                javaLocals.add((JavaLocal) immediate);
            }
        }
        for (Immediate immediate : args) {
            if (immediate instanceof JavaLocal) {
                javaLocals.add((JavaLocal) immediate);
            }
        }
        return javaLocals;
    }

    private boolean isEveryArgumentStringConstant(List<Immediate> bootstrapArgs, List<Immediate> args) {
        boolean isIntraProcedural = true;
        for (Immediate argument : bootstrapArgs) {
            if (!(argument instanceof StringConstant)) {
                isIntraProcedural = false;
            }
        }
        for (Immediate argument : args) {
            if (!(argument instanceof StringConstant)) {
                isIntraProcedural = false;
            }
        }
        return isIntraProcedural;
    }

    private MethodSignature getInvokeDynamicConcatSignature(int noOfParameters) {
        List<String> parameterTypes = Collections.nCopies(noOfParameters, "java.lang.String");
        ClassType classType = view.getIdentifierFactory().getClassType("sootup.dummy.InvokeDynamic");
        return view.getIdentifierFactory()
                   .getMethodSignature(classType, "makeConcatWithConstants", "java.lang.String",
                                       parameterTypes);
    }

    private MethodSignature getStringConcatenationMethodSignature() {
        ClassType classType = view.getIdentifierFactory().getClassType("java.lang.invoke.StringConcatFactory");
        return view.getIdentifierFactory()
                   .getMethodSignature(classType, "makeConcatWithConstants", "java.lang.invoke.CallSite",
                                       Arrays.asList("java.lang.invoke.MethodHandles$Lookup", "java.lang.String",
                                                     "java.lang.invoke.MethodType", "java.lang.String", "java.lang.Object[]"));
    }
}
