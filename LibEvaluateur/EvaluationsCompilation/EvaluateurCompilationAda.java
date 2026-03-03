package LibEvaluateur.EvaluationsCompilation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class EvaluateurCompilationAda extends EvaluateurCompilation {

    protected List<File> fichiers = new ArrayList<File>();

    public EvaluateurCompilationAda(File binaire) { 
        super();
        fichiers.add(binaire);
    }

    public EvaluateurCompilationAda(File binaire, ArrayList<File> dependences) { 
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
            resultat = "Test réussi";
        } else {
            testsResultat = new Boolean[]{false};
            resultat = "Test raté";
        }


    }

    public void evaluer() throws Exception {
        StringWriter sw = new StringWriter();
        BufferedWriter out = new BufferedWriter(sw);

        compileAda(out, fichiers.get(0).getAbsolutePath());
        out.close();
        
        String resultatsBruts = sw.toString();
        resultatVersTAP(resultatsBruts);
    }

    private static void compileAda(BufferedWriter out, String file) {
        try {
            File outputDir = new File("LibEvaluateur/EvaluationsCompilation/Compiled_Code");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            ProcessBuilder pb = new ProcessBuilder(
                "gnatmake",
                "-D", "LibEvaluateur/EvaluationsCompilation/Compiled_Code",
                "-gnat2012",
                "-Wall",
                file
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                out.write(output.toString());
            }

        } catch (Exception e) {
            try {
                out.write("Erreur de compilation Ada: " + e.getMessage());
            } catch (Exception ex) {
                System.out.println("erreur" + ex);
            }
        }
    }

    public static void main(String[] args) {
        try {
            File aTester = new File("LibEvaluateur/EvaluationsCompilation/CompilationTest.ada");
            EvaluateurCompilationAda eval = new EvaluateurCompilationAda(aTester);

            eval.evaluer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
