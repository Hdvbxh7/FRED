package LibEvaluateur.EvaluationsCompilation;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.tools.*;

import org.tap4j.model.TestResult;

public class EvaluateurCompilationJava extends EvaluateurCompilation {


    protected List<File> fichiers = new ArrayList<File>();

    public EvaluateurCompilationJava(File binaire) { 
        super();
        fichiers.add(binaire);
    }

    public EvaluateurCompilationJava(File binaire, ArrayList<File> dependences) { 
        super();
        fichiers.add(binaire);
        fichiers.addAll(dependences);
    }

    public String getResultat() {
        return this.resultat;
    }
    
    public Boolean[] getTestsResultat() {
        return testsResultat;
    }

    protected void resultatVersTAP(String SortieTest) {

        System.out.println("Voici la sortie +++++ " + SortieTest + " +++++");

        if (SortieTest.isEmpty()) {
            testsResultat = new Boolean[]{true};
        } else {
            testsResultat = new Boolean[]{false};
        }

    }

    public void evaluer() throws Exception {

        StringWriter sw = new StringWriter();
        BufferedWriter out = new BufferedWriter(sw);

        if (fichiers.size() > 1) {
            String classpath = "";
            for (int i = 1; i < fichiers.size(); i++) {
                if (i > 1) {
                    classpath += File.pathSeparator;
                }
                classpath += fichiers.get(i).getAbsolutePath();
            }
            ourJCompiler(out, fichiers.get(0).getAbsolutePath(), classpath);
        } else {
            ourJCompiler(out, fichiers.get(0).getAbsolutePath(), null);
        }
        out.close();
        
        String resultatsBruts = sw.toString();

        resultatVersTAP(resultatsBruts);

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
            System.out.println(cp.toString());
            javac.run(null, cout, cerr,"-d","LibEvaluateur/EvaluationsCompilation/Compiled_Code","-Xlint","-cp",cp, file);
        }else{
            javac.run(null, cout, cerr,"-d","LibEvaluateur/EvaluationsCompilation/Compiled_Code","-Xlint", file);
        }
        try{
            out.write(cout.toString());
            out.write(cerr.toString());
        }catch(Exception e){
            System.out.println("erreur"+e);
        }
    }


    public static void main(String[] args) {


        try {
            File aTester = new File("LibEvaluateur/EvaluationsCompilation/CompilationTest.java");
            File junit = new File("libs/junit4.jar");
            File junit2 = new File("libs/junit-jupiter-api-5.6.0.jar");
            File junit3 = new File("libs/junit-platform-commons-1.6.0.jar");
            File junit4 = new File("libs/junit-platform-engine-1.6.0.jar");
            File junit5 = new File("libs/junit-platform-launcher-1.6.0.jar");
            File apiguardian = new File("libs/apiguardian-api-1.1.0.jar");
            ArrayList<File> libs = new ArrayList<File>();
            libs.add(junit);
            libs.add(junit2);
            libs.add(junit3);
            libs.add(junit4);
            libs.add(junit5);
            libs.add(apiguardian);
            EvaluateurCompilationJava eval = new EvaluateurCompilationJava(aTester, libs);

            eval.evaluer();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
}
