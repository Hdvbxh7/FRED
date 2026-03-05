package configuration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import LibExplorateurs.*;

public class Scenario implements Runnable{

    /** date de fin de test */
    public static final GregorianCalendar endOfEvaluation = new GregorianCalendar(2026,GregorianCalendar.FEBRUARY,19,21,54);

    /** le chemin du projet (/<cheminDossProjet>) ou "" si il n'y a pas de sous dossier */
    public static final String projet = "/mini-projet";

    /** nombre de thread */
    public static int nbThread = 15;

    /** temps attendu avant le crash de la thread pool avec son unité de temps*/
    public static int waitingTimeBeforeCrash = 1;
    public static TimeUnit timeUnit = TimeUnit.HOURS; 

    /** temps attendu avant le crash d'un thread de scénario avec son unité de temps*/
    public static int waitingTimeBeforeCrashScenario = 1;
    public static TimeUnit timeUnitScenario = TimeUnit.HOURS; 

    /** Explorateur à utiliser 
     * @see ExplorateurGit ExplorateurGit
     * @see ExplorateurScenario ExplorateurScenario
     * @see ExplorateurSimple ExplorateurSimple
     * @see Explorateur Classe Abstraite Explorateur
    */
    public static final Explorateur explorateur = new ExplorateurGit("tom.gutierrez1040@gmail.com",new File("GitPaths.csv"),"/mini-projet");
    //public static final Explorateur explorateur = new ExplorateurSimple(new File("testing"));

    /** dossiers a tester */
    public File dossierATester;
    public File dossierResultat;

    /**
     * dossier sur lequel lancer le scénario
     * @param dossier dossier à tester
     */
    public Scenario(File dossier,File dossierResultatArg){
        dossierATester = dossier;
        dossierResultat = dossierResultatArg;
    }

    /**
     * Scenario à remplir pour gérer les test à effectuers
     * @param dossierEtudiant dossier ou fichier depuis lequel les tests sont lancés
     */
    public static void scenario(File dossierEtudiant,File dossierResultat){
        //Test Scenario
        try {
            File resultEvaluation = new File(dossierResultat.getPath()+"/evaluation_tgz8009.txt");
            configuration.Utiles.createTree(dossierResultat, true);
            resultEvaluation.createNewFile();
        } catch (IOException e) {
            System.out.println("boom");
        }

    }

    /**
     * lance le thread de scénario
     */
    public void run(){
        scenario(dossierATester,dossierResultat);
    }

    /** 
     * @see ExplorateurScenario 
     * si utilisation de Explorateur scenario 
     */

    /**
     * fonction qui renvoi la liste des dossiers à tester
     * @return une liste de dossier à tester
     */
    public static ArrayList<File> listeDossier(){
         //TODO : remplir et utiliser Explorateur Scénario pour customiser
        return null;
    }

    /**
     * fonction qui s'occupe de tout ce qu'il
     * y à a faire une fois les tests résolues
     */
    public static void postprocess(){
        //TODO : remplir et utiliser Explorateur Scénario pour customiser
    }
    
}
