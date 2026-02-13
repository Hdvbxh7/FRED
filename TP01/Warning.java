public class Warning {

    @Deprecated
    private static int bloup(){
        try {
            return 1;
        } finally{
            return 2;
        }
    }

    private static void affiche(Boolean a){ 
        Boolean b = false;
        if (b) {
            System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        }
        return;

    }

    public static void main(String[] args) {
        bloup();
    }
}
