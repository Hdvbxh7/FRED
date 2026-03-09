package LibEvaluateur;

import java.io.File;
import java.util.ArrayList;

import LibEvaluateur.EvaluationsCompilation.*;
import LibEvaluateur.EvaluationsMemoire.*;


public class Aggregateur {

    private String outputString = "";

    private float outputScore = 0;

    private float totalScore = 0;

    private ArrayList<Evaluateur> listeEvaluateur = new ArrayList<Evaluateur>();

    private File resultats;

    public Aggregateur(File resultats) {

        this.resultats = resultats;
    }


    /**
     * Permet d'ajouter un evaluateur a l'aggrégateur
     * @param eval
     */
    public void add(Evaluateur eval) {
        listeEvaluateur.add(eval);
    }

    /**
     * Aggrège les résultats de tout les évaluateurs pour produire deux fichiers
     * Resultat_eleve.txt : Résultat pour l'élève, un détail des tests sans score
     * Resultat_professeur.txt : Resultats pour le professeur, juste le score sans détailler les tests ratés et réussi.
     */
    public void aggreger() {
        String outputStringProfesseurs = "";
        String outputStringEleve = "";

        for (int i = 0; i < listeEvaluateur.size(); i++) {
            Evaluateur evaltemp = listeEvaluateur.get(i);
            Boolean[] temp = evaltemp.getTestsResultat();
            
            // Count passed and failed tests
            int passed = 0;
            int failed = 0;
            for (int j = 0; j < temp.length; j++) {
                if (temp[j]) {
                    passed++;
                } else {
                    failed++;
                }
            }
            
            // Add to professeurs overview
            outputStringProfesseurs += evaltemp.getNomEvaluateur() + " : " + passed + " réussis, " + failed + " échoués\n";
            
            // Add to élève details
            outputStringEleve += evaltemp.getNomEvaluateur() + " : \n" + evaltemp.getResultat() + "\n";
            
            // Update totals
            outputScore += passed;
            totalScore += temp.length;
        }

        // Add total score to professeurs file
        outputStringProfesseurs += "\nScore total : " + outputScore + " / " + totalScore;
        
        // Write both files
        File professeurFile = new File(resultats.getAbsolutePath(), "Resultats_professeur.txt");
        File eleveFile = new File(resultats.getAbsolutePath(), "Resultat_eleve.txt");
        
        try {
            java.nio.file.Files.write(professeurFile.toPath(), outputStringProfesseurs.getBytes());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        
        try {
            java.nio.file.Files.write(eleveFile.toPath(), outputStringEleve.getBytes());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        try {
            File aTester = new File("Tests/Test.java");
            File aTester2 = new File("BacATest/chiffre_significatif.adb");

            Aggregateur aggreg = new Aggregateur(new File("Tests/"));

            EvaluateurMemoire eval1 = new EvaluateurValgrindBinaire(aTester);

            EvaluateurCompilation eval2 = new EvaluateurCompilationAda(aTester2);

            eval1.setNomEvaluateur("FeurTest");

            eval2.setNomEvaluateur("Feur2Test");

            aggreg.add(eval1);

            aggreg.add(eval2);

            eval1.evaluer();

            eval2.evaluer();

            aggreg.aggreger();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
