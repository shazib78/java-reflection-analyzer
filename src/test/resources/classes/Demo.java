import java.lang.reflect.InvocationTargetException;

public class Demo {
    public void caller() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Demo.class.getDeclaredMethod("reflectionPrintNonStatic").invoke(this);
        Demo.class.getMethod("reflectionPrintNonStatic").invoke(this);
    }

    public void reflectionPrintNonStatic() {
        System.out.println("reflectionPrintNonStatic");
    }
}
