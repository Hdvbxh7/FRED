package configuration;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import LibExplorateurs.*;
import LibEvaluateur.*;

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
            File pointJava = new File(dossierEtudiant, "src/Point.java");
            File pointTestClass = new File(dossierEtudiant, "bin/PointTest.class");
            
            // Test 1 : Compilation
            LibEvaluateur.EvaluationsCompilation.EvaluateurCompilationJava evalCompilation = 
                new LibEvaluateur.EvaluationsCompilation.EvaluateurCompilationJava(pointJava);
            evalCompilation.setNomEvaluateur("Compilation");
            aggregateur.add(evalCompilation);
            evalCompilation.evaluer();
            
            // Si la compilation réussit (pas d'erreurs), on lance les autres tests
            Boolean[] resultatComp = evalCompilation.getTestsResultat();
            if (resultatComp != null && resultatComp.length > 0 && resultatComp[0]) {
                
                // Test 2 : Checkstyle
                // java.util.ArrayList<File> fichiers = new java.util.ArrayList<>();
                // fichiers.add(pointJava);
                // LibEvaluateur.EvaluationsStyle.CheckStyle evalStyle = 
                //     new LibEvaluateur.EvaluationsStyle.CheckStyle(fichiers);
                // evalStyle.setNomEvaluateur("Style (Checkstyle)");
                // aggregateur.add(evalStyle);
                // evalStyle.evaluer();
                
                // Test 3 : Tests unitaires JUnit
                java.util.ArrayList<File> testsFiles = new java.util.ArrayList<>();
                testsFiles.add(pointTestClass);
                LibEvaluateur.EvaluationsUnitaire.EvaluateurJUnit evalJUnit = 
                    new LibEvaluateur.EvaluationsUnitaire.EvaluateurJUnit(testsFiles);
                evalJUnit.setNomEvaluateur("Tests unitaires (JUnit)");
                aggregateur.add(evalJUnit);
                evalJUnit.evaluer();
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
