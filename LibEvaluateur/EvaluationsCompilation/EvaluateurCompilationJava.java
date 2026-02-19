package LibEvaluateur.EvaluationsCompilation;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.tools.*;

public class EvaluateurCompilationJava extends EvaluateurCompilation {


    protected List<File> fichiers = new ArrayList<File>();

    public EvaluateurCompilationJava() { 
        super();
    }

    public String getResultat() {
        return this.resultat;
    }
    
    public Boolean[] getTestsResultat() {
        return testsResultat;
    }

    protected void resultatVersTAP(String SortieTest) {

    }

    public void evaluer() throws Exception {

    }








    private static void ourJCompiler(BufferedWriter out,String file,String cp){
        ByteArrayOutputStream cout = new ByteArrayOutputStream();
        ByteArrayOutputStream cerr = new ByteArrayOutputStream();

        // Create Tests directory if it doesn't exist
        File testsDir = new File("Tests");
        if (!testsDir.exists()) {
            testsDir.mkdir();
        }

        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        if(cp!=null){
            javac.run(null, cout, cerr,"-d","LibEvaluateur/EvaluationsCompilation/Compiled_Code","-Xlint","-classpath",cp, file);
        }else{
            javac.run(null, cout, cerr,"-d","LibEvaluateur/EvaluationsCompilation/Compiled_Code","-Xlint", file);
        }
        try{
            out.write(cout.toString());
            out.write(cerr.toString());
            out.write("--------------------------------------------------------------------------------------------------\n");
        }catch(Exception e){
            System.out.println("erreur"+e);
        }
    }


    public static void main(String[] args) {

        try { 
            BufferedWriter out=new BufferedWriter(new FileWriter("log"));   
            ourJCompiler(out, "LibEvaluateur/EvaluationsCompilation/Test.java", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        


    }
    
}
