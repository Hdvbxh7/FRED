package configuration;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import LibExplorateurs.*;
import LibEvaluateur.*;
import LibEvaluateur.EvaluationsBoiteNoire.*;
import LibEvaluateur.EvaluationsCompilation.*;
import LibEvaluateur.EvaluationsMemoire.*;
import LibEvaluateur.EvaluationsStyle.*;
import LibEvaluateur.EvaluationsUnitaire.*;


public class Configuration implements Runnable{

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
     * @see ExplorateurConfiguration ExplorateurScenario
     * @see ExplorateurSimple ExplorateurSimple
     * @see Explorateur Classe Abstraite Explorateur
    */
    //public static final Explorateur explorateur = new ExplorateurGit("tom.gutierrez1040@gmail.com",new File("GitPaths.csv"),"/mini-projet","GitProfesseur");
    public static final Explorateur explorateur = new ExplorateurSimple(new File("BacATest"));

    /** dossiers a tester */
    public File dossierATester;
    public File dossierResultat;

    /**
     * dossier sur lequel lancer le scénario
     * @param dossier dossier à tester
     */
    public Configuration(File dossier,File dossierResultatArg){
        dossierATester = dossier;
        dossierResultat = dossierResultatArg;
    }

    /**
     * Scenario à remplir pour gérer les test à effectuers
     * @param dossierEtudiant dossier ou fichier depuis lequel les tests sont lancés
     */
    public static void scenario(File dossierEtudiant,File dossierResultat){
        try {
            // Création de l'agrégateur pour collecter les résultats
            Aggregateur aggregateur = new Aggregateur(dossierResultat);
            
            // Préparation des références de fichiers
            File pointJava = new File(dossierEtudiant, "Point.java");
            File pointTestClass = new File(dossierEtudiant, "PointTest.class");
            
            // Test 1 : Compilation
            EvaluateurCompilationJava evalCompilation = new EvaluateurCompilationJava(pointJava);
            aggregateur.add(evalCompilation.setNomEvaluateur("Compilation").evaluer());
            
            // Si la compilation réussit (pas d'erreurs), on lance les autres tests
            if (evalCompilation.getTestsResultat()[0]) {

                //Test 3 : Tests Boite Noire
                
                ArrayList<String> arguments = new ArrayList<String>(Arrays.asList("5 3", "8 19"));
                File pointJuste = new File("BacATest/TP02_Correction/PointV.class");
                System.out.println(evalCompilation.getDossiercompilé());
                ArrayList<File> binaires = new ArrayList<File>(Arrays.asList(new File("BacATest/TP02/Point.class"), pointJuste));
                EvaluateurBoiteNoire evalBoiteNoire = new EvaluateurBoiteNoireJavaSimple(binaires, arguments);
                aggregateur.add(evalBoiteNoire.setNomEvaluateur("Evaluation Boite Noire").evaluer());
                
                //Test 2 : Checkstyle
                java.util.ArrayList<File> fichiers = new java.util.ArrayList<>();
                fichiers.add(pointJava);
                EvaluateurCheckStyle evalStyle = new EvaluateurCheckStyle(fichiers, new File("BacATest/checkstyle_pas_cringe.xml"));
                aggregateur.add(evalStyle.setNomEvaluateur("Style (Checkstyle)").evaluer());
                
                // Test 3 : Tests unitaires JUnit
                java.util.ArrayList<File> testsFiles = new java.util.ArrayList<>();
                testsFiles.add(pointTestClass);
                EvaluateurJUnit evalJUnit = new EvaluateurJUnit(testsFiles);
                aggregateur.add(evalJUnit.setNomEvaluateur("Tests unitaires (JUnit)").evaluer());
            }

            // Agrégation des résultats
            aggregateur.aggreger();
            
        } catch (Exception e) {
            System.out.println("Erreur lors de l'évaluation: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        File TP02 = new File("BacATest/TP02");
        File TP02Resultat = new File("BacATest/TP02Res");
        scenario(TP02,TP02Resultat);
    }

    /**
     * lance le thread de scénario
     */
    public void run(){
        scenario(dossierATester,dossierResultat);
    }

    /** 
     * Fonctions à remplir pour l'explorateur Configuration
     * @see ExplorateurConfiguration 
     * si utilisation de Explorateur scenario 
     */

    /**
     * fonction qui renvoi la liste des dossiers à tester
     * @return une liste de dossier à tester
     */
    public static ArrayList<File> listeDossier(ExplorateurConfiguration explorateurConfiguration){
         //TODO : remplir et utiliser Explorateur Configuration pour customiser
        return null;
    }

    /**
     * fonction qui s'occupe de tout ce qu'il
     * y à a faire une fois les tests résolues
     */
    public static void postprocess(ExplorateurConfiguration explorateurConfiguration){
        //TODO : remplir et utiliser Explorateur Configuration pour customiser
    }
    
}
