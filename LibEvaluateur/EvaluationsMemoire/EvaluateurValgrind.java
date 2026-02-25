package LibEvaluateur.EvaluationsMemoire;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.tap4j.model.Plan;
import org.tap4j.model.TestResult;
import org.tap4j.model.TestSet;
import org.tap4j.util.StatusValues;


/**
 * Classe permettant d'évaluer un programme binaire à l'aide de l'outil Valgrind.
 *
 * <p>
 * Cette classe exécute Valgrind sur un fichier binaire donné afin de détecter
 * des erreurs liées à la gestion de la mémoire.
 *
 * <p>
 * Le résultat produit par Valgrind est capturé via le flux d'erreur standard
 * et converti dans un format TAP (Test Anything Protocol) via la méthode
 * {@link #resultatVersTAP(String)}.
 * </p>
 *
 *
 * @author Louis-Clément Olivier
*/

public class EvaluateurValgrind extends EvaluateurMemoire {

    private TestSet ensembleTest;

    public EvaluateurValgrind(File binaire) {
        super();
        this.ensembleTest = new TestSet();
        this.fichiers.add(binaire);
    }

    /**
     * Retourne le résultat brut de l'évaluation mémoire.
     *
     * @return une chaîne contenant le résultat de l'évaluation
     */
    public String getResultat() {
        return this.resultat;
    }
    
    /**
     * Retourne les résultats des tests sous forme de tableau de booléens.
     *
     * <p>
     *  Ici il n'y aura qu'un résultat (il y a t'il des erreurs mémoire ou non) 
     *  mais on peut changer si on veut checker des erreurs mémoire spécifique (double free, utilisation de mémoire non initialisée...)
     * </p>
     *
     * @return tableau des résultats des tests
     */
    public Boolean[] getTestsResultat() {
        return testsResultat;
    }

    /**
     * Convertit la sortie brute de Valgrind en format TAP.
     *
     *
     * @param SortieTest la sortie brute produite par Valgrind
     */
    protected void resultatVersTAP(String SortieTest) {
        System.out.println(SortieTest);
        testsResultat = new Boolean[1];
		ensembleTest.setPlan( new Plan(1) );
		TestResult presenceFuite;
		if (!(SortieTest.contains(" All heap blocks were freed -- no leaks are possible")) ){
			presenceFuite = new TestResult( StatusValues.NOT_OK, 1 );
            testsResultat[0] = false;
			}else {
			presenceFuite = new TestResult( StatusValues.OK, 1 );
            testsResultat[0] = true;
		}
		String resumeFuite = "- " + resume(SortieTest);
		presenceFuite.setDescription( resumeFuite);
		ensembleTest.addTestResult( presenceFuite );
		this.resultat = producteur.dump( ensembleTest );
    }

    /**
     * Exécute Valgrind sur le fichier binaire spécifié et capture sa sortie.
     */
    public void evaluer() throws Exception {

        Process process = new ProcessBuilder(
                "valgrind",
                fichiers.get(0).getAbsolutePath()
        ).start();

        BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream())
        );

        StringBuilder outputBuilder = new StringBuilder();
        String line;

        while ((line = errorReader.readLine()) != null) {
            outputBuilder.append(line).append(System.lineSeparator());
        }

        process.waitFor();

        String OutputSortie = outputBuilder.toString();

        resultatVersTAP(OutputSortie);
    }

    /**
     * Une fonction pour recuperer uniquement le resumé de l'etat de la mémoire.
     */
    private static String resume(String input) {
        int start = input.indexOf(" total heap usage: ");
        int end = input.indexOf("\n", start);
        return input.substring(start, end);
    }
    
}
