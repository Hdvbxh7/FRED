import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;

public class Scenario {

    public static final GregorianCalendar endOfEvaluation = new GregorianCalendar(2026,GregorianCalendar.FEBRUARY,19,21,54);
    /** le chemin du projet (/<cheminDossProjet>) ou "" si il n'y a pas de sous dossier */
    public static final String projet = "/mini-projet";
    /** email du professeur pour identité de commit */
    public static final String mail = "tom.gutierrez1040@gmail.com";

    /**
     * Scenario à remplir pour gérer les test à effectuers
     * @param dossierEtudiant, 
     */
    public static void scenario(File dossierEtudiant){
        //Test Scenario
        try {
            File resultEvaluation = new File("resultats/tgz8009/evaluation_tgz8009.txt");
            resultEvaluation.createNewFile();
        } catch (IOException e) {
            System.out.println("boom");
        }

    }
    
}
