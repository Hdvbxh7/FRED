package LibEvaluateur;

import java.io.File;
import java.util.ArrayList;

import LibEvaluateur.EvaluationsCompilation.EvaluateurCompilationJava;
import LibEvaluateur.EvaluationsMemoire.EvaluateurValgrind;

public class Aggregateur {

    private String outputString = "";

    private float outputScore = 0;

    private float totalScore = 0;

    private ArrayList<Evaluateur> listeEvaluateur = new ArrayList<Evaluateur>();


    public void add(Evaluateur eval) {
        listeEvaluateur.add(eval);
    }

    public void aggreger() {
        for (int i = 0; i < listeEvaluateur.size(); i++) {
            Boolean[] temp = listeEvaluateur.get(i).getTestsResultat();
            for (int j = 0; j < temp.length; j++) {
                outputScore += (temp[j] ? 1 : 0);
            }
            outputString += "\n" + listeEvaluateur.get(i).getResultat();
            totalScore += temp.length;
        }
        System.out.println(outputString);

    }

    public static void main(String[] args) {

        try {
            File aTester = new File("Tests/CompilationTest.java");

            File aTesterFail = new File("Tests/CompilationTestFail.java");

            Aggregateur aggreg = new Aggregateur();

            EvaluateurValgrind eval1 = new EvaluateurValgrind(aTester);

            EvaluateurValgrind eval2 = new EvaluateurValgrind(aTesterFail);

            aggreg.add(eval2);

            eval2.evaluer();

            aggreg.aggreger();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
