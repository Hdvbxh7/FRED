package LibEvaluateur.EvaluationsMemoire;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class EvaluateurValgrind extends EvaluateurMemoire {


    public EvaluateurValgrind(File binaire) {
        super();
        this.fichiers.add(binaire);
    }

    public String getResultat() {
        return this.resultat;
    }
    
    public Boolean[] getTestsResultat() {
        return testsResultat;
    }

    protected void resultatVersTAP(String SortieTest) {

        System.out.println(SortieTest);

    }

    public void evaluer() throws Exception {


        Process process = new ProcessBuilder("valgrind", fichiers.get(0).getAbsolutePath()).start();


        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        StringBuilder outputBuilder = new StringBuilder();
        String line;

        // Read all stderr content
        while ((line = errorReader.readLine()) != null) {
            outputBuilder.append(line).append(System.lineSeparator());
        }

        // Wait for process to finish
        process.waitFor();

        String OutputSortie = outputBuilder.toString();

        // Call your function
        resultatVersTAP(OutputSortie);
}



        
    }

    public static void main(String[] args) {

        File aTester = new File("BacATest/exemples_memoire_dynamique_fail");

        EvaluateurValgrind test = new EvaluateurValgrind(aTester);
        try {
            test.evaluer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
