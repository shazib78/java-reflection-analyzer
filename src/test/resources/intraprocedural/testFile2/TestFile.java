import java.lang.reflect.InvocationTargetException;

public class TestFile {
    public void caller() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        /*Demo2.class.getDeclaredMethod("reflectionPrintNonStatic").invoke(this);
        Demo2.class.getMethod("reflectionPrintNonStatic").invoke(this);*/
        String str1 = "reflec";
        String str2 = "tion";
        String str3 = "Print";
        String str4 = "Non";
        String str5 = "Static";
        String fullMethodName = str1 + str2 + str3 + str4 + str5;
        String alias = fullMethodName;
        TestFile.class.getMethod(alias).invoke(this);

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
