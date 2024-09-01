import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CutNRunTestJars {
  private Demo privateField;
  public Demo publicField;

  public CutNRunTestJars(){
    System.out.println("constructors");
  }

  public CutNRunTestJars(String str){
    System.out.println(str);
  }

  public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException, NoSuchFieldException {
    int i = 0;
    if (someBool()) {
      i = 3;
    } else {
      i = 4;
    }
    doNothing(i);

    CutNRunTestJars.class.getDeclaredMethod("reflectionPrint").invoke(null);

    System.out.println("lineBreak");
    CutNRunTestJars.class.getDeclaredMethod("reflectionPrint", int.class).invoke(null, 10);

    System.out.println("lineBreak");
    CutNRunTestJars.class.getMethod("reflectionPrint").invoke(null);

    System.out.println("lineBreak");
    Class c = Class.forName( "CutNRunTestJars" );
    Method m = c.getMethod("reflectionPrint", null);
    m.invoke(null, null);

    System.out.println("lineBreak");
    Demo demo = new Demo();
    demo.caller();

    System.out.println("new instance");
    CutNRunTestJars.class.newInstance();

    Object object = CutNRunTestJars.class.newInstance();

    CutNRunTestJars.class.getConstructor(String.class).newInstance("one parameter constructor");

    System.out.println("field access");
    CutNRunTestJars cutNRunTestJars = new CutNRunTestJars();
    cutNRunTestJars.privateField = new Demo();
    Field field = CutNRunTestJars.class.getDeclaredField("privateField");
    Demo demo1 = (Demo) field.get(cutNRunTestJars);
    demo1.reflectionPrintNonStatic();

    System.out.println("field access");
    cutNRunTestJars.publicField = new Demo();
    Field field1 = CutNRunTestJars.class.getField("publicField");
    Demo demo2 = (Demo) field1.get(cutNRunTestJars);
    demo2.reflectionPrintNonStatic();
  }

  public static void doNothing(int i) {}

  public static boolean someBool() {
    return false;
  }
  public static void reflectionPrint() {
    System.out.println("abc");
  }

  public static void reflectionPrint(int number) {
    System.out.println(number);
  }
}