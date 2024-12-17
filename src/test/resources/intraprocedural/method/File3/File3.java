import java.lang.reflect.InvocationTargetException;

public class File3 {
    public void caller() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        /*Demo2.class.getDeclaredMethod("reflectionPrintNonStatic").invoke(this);
        Demo2.class.getMethod("reflectionPrintNonStatic").invoke(this);*/
        String str1 = "reflec";
        String str2 = "tion";
        String str3 = str2 + "Print";
        String str4 = str3 + "Non";
        String fullMethodName = str4 + "Static";
        /*String alias = fullMethodName;*/
        TestFile.class.getMethod(fullMethodName).invoke(this);

        /*StringBuilder stringBuilder = new StringBuilder("reflectionPrintNonStatic");
        Demo2.class.getMethod(stringBuilder.toString()).invoke(this);

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("reflectionPrintNonStatic");
        Demo2.class.getMethod(stringBuffer.toString()).invoke(this);*/
    }

    public void reflectionPrintNonStatic() {
        System.out.println("reflectionPrintNonStatic");
    }
}
