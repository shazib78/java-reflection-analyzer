package de.upb.sse.cutNRun.analyzer.RQ3Evaluation;

import de.upb.sse.cutNRun.dataRecorder.ExcelWriterAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

import java.io.IOException;
import java.util.*;

public class EvaluationTest {

    @Test
    public void RQ3Evaluation() throws IOException {
        ExcelWriterAdapter excelWriter = new ExcelWriterAdapter("RQ3_combinedResults-Duplicate", false);
        excelWriter.setHeaders("Source.Name", "statementId", "project", "statement", "Value", "Error", "Error message", "testing values");
        List<Object[]> excelData = excelWriter.readExcelDataAsIs();
        excelData.remove(0);

        ExcelWriterAdapter excelWriterOutput = new ExcelWriterAdapter("RQ3_isReflectionTargetExistsInApplication", false);
        excelWriterOutput.setHeaders("statementId", "Source.Name", "project", "statement", "Value", "isExists", "Error", "Error message", "testing values");
        Map<String, Object[]> excelDataToWrite = new LinkedHashMap<>();

        for (Object[] rowValues : excelData) {
            try {
                String reflectionTarget = (String) rowValues[4];
                if (!StringUtils.isEmpty(reflectionTarget) && !reflectionTarget.equals("[]")) {
                    checkIfReflectionTargetExistsInApplication(reflectionTarget, rowValues, excelDataToWrite);
                } else {
                    excelDataToWrite.put((String) rowValues[1], new Object[]{rowValues[1], rowValues[0], rowValues[2], rowValues[3], rowValues[4], "NA", "", "", rowValues[7]});
                }
            } catch (Exception e) {
                String stacktrace = ExceptionUtils.getStackTrace(e);
                excelDataToWrite.put((String) rowValues[1], new Object[]{rowValues[1], rowValues[0], rowValues[2], rowValues[3], rowValues[4], "ERROR", "ERROR", stacktrace, rowValues[7]});
            } finally {
                excelWriterOutput.saveData(excelDataToWrite);
            }
        }
    }

    private void checkIfReflectionTargetExistsInApplication(String reflectionTarget, Object[] rowValues,
                                                            Map<String, Object[]> excelDataToWrite) {
        String jarName = (String) rowValues[2];
        String pathToBinary = "src/main/resources" + jarName.replaceFirst("./jars", "/jars");
        View view = new JavaView(new JavaClassPathAnalysisInputLocation(pathToBinary));
        List<String> targets = convertStringToList(reflectionTarget);
        if (isMethodExists(targets, view)) {
            setIsTargetExists("true", excelDataToWrite, rowValues);
        } else if (isFieldExists(targets, view)) {
            setIsTargetExists("true", excelDataToWrite, rowValues);
        } else if (isClassExists(targets, view)) {
            setIsTargetExists("true", excelDataToWrite, rowValues);
        } else {
            setIsTargetExists("false", excelDataToWrite, rowValues);
        }
    }

    private List<String> convertStringToList(String reflectionTarget) {
        reflectionTarget = reflectionTarget.substring(1, reflectionTarget.length() - 1); // removes [ and ]
        String[] items = reflectionTarget.split(",");
        //Remove white spaces
        /*for (int i = 0; i < items.length; i++) {
            items[i] = items[i].trim();
        }*/

        // Convert array to list
        List<String> targets = new LinkedList<>(Arrays.asList(items));
        targets.removeIf(s -> s == null || s.trim().isEmpty());
        return targets;
    }

    private void setIsTargetExists(String isExists, Map<String, Object[]> excelDataToWrite, Object[] rowValues) {
        excelDataToWrite.put((String) rowValues[1], new Object[]{rowValues[1], rowValues[0], rowValues[2], rowValues[3],
                rowValues[4], isExists, "", "", rowValues[7]});
    }

    private boolean isMethodExists(List<String> reflectionTargets, View view) {
        if (reflectionTargets.size() == 1) {
            for (SootClass sootClass : view.getClasses().toList()) {
                for (SootMethod sootMethod : sootClass.getMethods()) {
                    if (sootMethod.getName().equals(reflectionTargets.get(0))) {
                        return true;
                    }
                }
            }
        } else {
            List<Boolean> temp = new ArrayList<>();
            for (String reflectionTarget : reflectionTargets) {
                for (SootClass sootClass : view.getClasses().toList()) {
                    for (SootMethod sootMethod : sootClass.getMethods()) {
                        if (sootMethod.getName().equals(reflectionTarget)) {
                            return true;//temp.add(Boolean.TRUE);
                        }
                    }
                }
            }

            /*if(reflectionTargets.size() == temp.size()){
                return true;
            } else {
                return false;
            }*/
        }
        return false;
    }

    private boolean isFieldExists(List<String> reflectionTargets, View view) {
        if (reflectionTargets.size() == 1) {
            for (SootClass sootClass : view.getClasses().toList()) {
                for (SootField sootField : sootClass.getFields()) {
                    if (sootField.getName().equals(reflectionTargets.get(0))) {
                        return true;
                    }
                }
            }
        } else {
            List<Boolean> temp = new ArrayList<>();
            for (String reflectionTarget : reflectionTargets) {
                for (SootClass sootClass : view.getClasses().toList()) {
                    for (SootField sootField : sootClass.getFields()) {
                        if (sootField.getName().equals(reflectionTarget)) {
                            return true;//temp.add(Boolean.TRUE);
                        }
                    }
                }
            }

            /*if(reflectionTargets.size() == temp.size()){
                return true;
            } else {
                return false;
            }*/
        }
        return false;
    }

    private boolean isClassExists(List<String> reflectionTargets, View view) {
        if (reflectionTargets.size() == 1) {
            String className = reflectionTargets.get(0);

            if (className.contains("/")) {
                className = className.replaceAll("/", ".");
                className = className.replaceAll(";", "");
                if (className.indexOf("L") == 0) {
                    className = className.replaceFirst("L", "");
                }
            }

            ClassType classType = view.getIdentifierFactory().getClassType(className);
            Optional<? extends SootClass> sootClass = view.getClass(classType);
            return sootClass.isPresent();
        } else {
            List<Boolean> temp = new ArrayList<>();
            for (String reflectionTarget : reflectionTargets) {
                String className = reflectionTarget;

                if (className.contains("/")) {
                    className = className.replaceAll("/", ".");
                    className = className.replaceAll(";", "");
                    if (className.indexOf("L") == 0) {
                        className = className.replaceFirst("L", "");
                    }
                }

                ClassType classType = view.getIdentifierFactory().getClassType(className);
                Optional<? extends SootClass> sootClass = view.getClass(classType);
                /*if(sootClass.isPresent()){
                    temp.add(Boolean.TRUE);
                }*/
                return sootClass.isPresent();
            }

            /*if(reflectionTargets.size() == temp.size()){
                return true;
            } else {
                return false;
            }*/
            return false;
        }
    }
}
