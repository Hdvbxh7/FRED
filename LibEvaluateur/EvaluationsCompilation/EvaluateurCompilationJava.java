package LibEvaluateur.EvaluationsCompilation;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
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
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        if(cp!=null){
            javac.run(null, cout, cerr,"-d","Tests","-Xlint","-classpath",cp, file);
        }else{
            javac.run(null, cout, cerr,"-d","Tests","-Xlint", file);
        }
        try{
            out.write(cout.toString());
            out.write(cerr.toString());
            out.write("--------------------------------------------------------------------------------------------------\n");
        }catch(Exception e){
            System.out.println("erreur"+e);
        }

    }
    
}
