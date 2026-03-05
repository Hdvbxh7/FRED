package Tests;

public class TestV {

    public static int doSomething(int a) {
        return a + a;
    }
    
    public static void main(String[] args) {

        int argument = Integer.parseInt(args[0]);
        argument = doSomething(argument);
        System.out.println(argument);
    }
}
