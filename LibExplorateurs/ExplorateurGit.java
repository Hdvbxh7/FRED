package LibExplorateurs;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import LibExplorateurs.Git.AppelGit;
import LibExplorateurs.Git.DossierGitATester;
import configuration.Configuration;
import configuration.Utiles;

/**
 * Systeme permettant d'utiliser le systeme Git
 * pour réaliser les tests
 */
public class ExplorateurGit extends Explorateur{

    /**Liste de Threads */
    public ArrayList<Thread> tList;
    /**liste des AppelsGits */
    public ArrayList<AppelGit> gitsEleve;
    /** Lock utilisé pour le multithreading */
    public Lock lockPartage;
    /** email du professeur pour identité de commit */
    private String mailCommit;
    /** CSV pour les gits à évaluer
     * format : 
     * login,url
     * ...,...
    */
    private File csvGits;
    String cheminProjetGit;

    public String getCheminProjetGit() {
        return cheminProjetGit;
    }

    public String getMailCommit() {
        return mailCommit;
    }

    /**
     * 
     * @param mail mail à lier aux commits de l'évaluateur 
     * @param csv fichier csv qui contient tout les noms
     * et l'url de chaque éléve selon le format
     * login,url
     * ...,...
     */
    public ExplorateurGit(String mail,File csv){
        mailCommit = mail;
        csvGits = csv;
        lockPartage  = new ReentrantLock();
        nomResultat = new ArrayList<File>();
        dossiersATester = new ArrayList<File>();
        gitsEleve = new ArrayList<AppelGit>();
        cheminProjetGit = null;
    }

        /**
     * 
     * @param mail mail à lier aux commits de l'évaluateur 
     * @param csv fichier csv qui contient tout les noms
     * et l'url de chaque éléve selon le format
     * login,url
     * ...,...
     */
    public ExplorateurGit(String mail,File csv,String cheminProjet){
        mailCommit = mail;
        csvGits = csv;
        lockPartage  = new ReentrantLock();
        nomResultat = new ArrayList<File>();
        dossiersATester = new ArrayList<File>();
        gitsEleve = new ArrayList<AppelGit>();
        cheminProjetGit = cheminProjet;
    }
    
    /** Dossier Local qui contient les repos */
    final static File localDir = new File("studentRepo");

     /** Dossier Local qui contient les résultats */
    final static File resultDir = new File("resultats");

     /**
     * ajoute un test dans le dossier Git avant qu'il soit push
     * @param dossierGit dossier de l'éléve évalué
     * @param identifiantEleve identifiant de l'éléve évalué
     */
    private File ajoutTest(File dossierGit,String identifiantEleve){
        try {
            //dossier dans lequel les évaluations seront copiés
            File dossierResultat = new File(dossierGit.getCanonicalPath()+cheminProjetGit+"/evaluation");

            //fichier qui contient les informations d'évaluations
            File resultatCalcule = new File("resultats/"+identifiantEleve+cheminProjetGit+"/Resultat_eleve.txt");

            //fichier de destination des tests
            File fichierResultat = new File(dossierResultat.getCanonicalPath()+"/evaluation.txt");

            //Si le dossier de resultat existe déjà dans le dossier Git le détruit
            if(dossierResultat.exists()){
                Utiles.destroyFile(dossierResultat);
            }
            //(re)créer le dossier resultat dans le Repo local
            dossierResultat.mkdirs();

            //copie le fichier de test contenu dans dossier dans resultat
            Utiles.copyPaste(resultatCalcule, fichierResultat);

            return dossierResultat;

        //récupére les différentes erreur d'écriture de fichier
        } catch (IOException e) {
            System.out.println("erreur d'écriture en ajoutant les résultats");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * renvoi la liste des dossiers à tester
     */
    public ArrayList<File> listeDossier(){
        //création du dossier qui contient le repo
        if (!localDir.exists()) {
            localDir.mkdirs();
        }

        //création du dossier qui contient les résultats
        if (!localDir.exists()) {
            localDir.mkdirs();
        }

        //récupération des login et url
        String content = Utiles.content(csvGits);
        String[] lines = content.split("\n");

        //initie la threadPool
        initiationThreadPool();

        //lancement des tests et vérification des mises à jour par dossier
        //depuis les informations du csv
        for(int ind=1;ind<lines.length;ind++){

            //séparation des informations (1: login, 2: url)
            String[] values = lines[ind].split(",");
            
            //création du Thread
            threadPool.execute(new DossierGitATester(values[1], values[0], this));
        }
        shutdownAndWaitForTermination();

        //renvoi les dossiers à tester
        return dossiersATester;
    }

    public void postprocess(){
        for(AppelGit gitEleve : gitsEleve){
            //ajoute le test au dossier Git
            File aPush = ajoutTest(gitEleve.getStudentRepo(),gitEleve.getIdentifiant());
            //envoi le résultat du test sur le git
            gitEleve.push(aPush,mailCommit);
        }
    }
}
