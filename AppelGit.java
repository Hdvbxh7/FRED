import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * Bibliothéque de fonctions 
 * qui permettent de gérer le git
 */
public class AppelGit {

    //attributs

    /** Repo Local qui contient les dossiers */
    Repository studentRepos;
    /** Git qui contient les dossiers */
    Git studentGit;
    /** Statut du dernier pull */
    pullStatut statut;
    /** nom d'utilisateur */
    String utilisateur;
    /** mots de passe */
    String motDePasse;

    /** Statut du pull */
    static enum pullStatut {
        UP_TO_DATE,
        MAJ,
        CONFLIT;
    }

    //constructeur
    
    /**
     * Clone le répértoire Git spécifié par GitUrl
     * dans un dossier studentRepos
     * @param gitUrl Url du git a cloné
     */
    AppelGit(String gitUrl,File doss){
        try {
            // vérifie si le dossier local existe et le supprime sinon
            String usermdp = content(new File("mdpGitlab.txt"));
            String[] info = usermdp.split(":");
            utilisateur = info[0];
            motDePasse = info[1];
            if(!(new File(doss.getCanonicalPath()+Scenario.projet)).exists()){
                try {    
                    //clonage du repository
                    studentGit = Git.cloneRepository()
                        .setURI(gitUrl).setCredentialsProvider(new UsernamePasswordCredentialsProvider(utilisateur, motDePasse))
                        .setDirectory(doss)
                        .call();
                
                    //récupération du repository local dans la classe
                    studentRepos = studentGit.getRepository(); 

                    //mise à jour du dernier statut de pull
                    statut = pullStatut.MAJ;

                //Erreur de la part de l'APIGit JGit
                } catch (GitAPIException e) {
                    System.out.println("erreur lié au clonage du git");
                    e.printStackTrace();
                }
            } else {
                //Récupération du Git
                studentGit = Git.open(doss);

                //récupération du repository local dans la classe
                studentRepos = studentGit.getRepository();  
                
                //mise à jour du dernier statut de pull
                statut = maj();

                //Il y a des conflits
                if(statut == pullStatut.CONFLIT){
                    //suppression du dossier de l'éléve
                    destroyFile(doss);

                    //clonage du Git
                    studentGit = Git.cloneRepository()
                        .setURI(gitUrl).setCredentialsProvider(new UsernamePasswordCredentialsProvider(utilisateur, motDePasse))
                        .setDirectory(doss)
                        .call();

                    //récupération du repository local dans la classe
                    studentRepos = studentGit.getRepository(); 
                }
            }
        
        //Erreur de lecture du fichier
        } catch (IOException e) {
            System.out.println("erreur lié au à l'existence du fichier "+doss.getName());
            e.printStackTrace();
        } catch (GitAPIException e) {
            System.out.println("erreur lié au clonage du git");
            e.printStackTrace();
        }
    }
    
    //methodes

    /**
     * Renvoi le contenu d'un fichier sous forme de String
     * @param file, fichier à transformer en string
     * @return le fichier sous forme de String, null si erreur
     */
    public static String content(File file){
        try {
            //ouverture des canaux d'entrée
            InputStream is = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader buffer = new BufferedReader(isr);

            //lis la premiére ligne du fichier
            String line = buffer.readLine();

            //Construit la string final
            StringBuilder builder = new StringBuilder();
        
            //rajoute les lignes à la string 
            while(line != null){
                builder.append(line).append("\n");
                line = buffer.readLine();
            }

            //fermeture des canaux
            buffer.close();
            isr.close();
            is.close();

            //renvoi de la string final
            return builder.toString();

        //erreur de lecture du fichier
        } catch (IOException e) {
            System.out.println("erreur de lecture du fichier");
            return null;
        }
    }

    /**
     * Détruit le fichier/dossier et 
     * tout ce qu'il contient
     * @param file, fichier/dossier à détruire
     */
    public static void destroyFile(File file){
        if(file.isDirectory()){
            //suppression du dossier

            //liste les sous fichiers du dossier
            File[] fichierASupprimer = file.listFiles();

            //parcours les fichiers et dossiers et les supprimes
            for(File fichier:fichierASupprimer){
                destroyFile(fichier);
            }

            //supprime le dossier maintenant qu'il est vide
            file.delete();
        } else{
            //suppression du fichier
            file.delete();
        }
    }
    
   /**
    * Vérifie si il y a eu un commit depuis le dernier test
    * @param LastPull, date du dernier test
    * @return true : il faut relancer le test, false : on ne relance pas
    */
    public boolean needTesting(long dateDernierTest){
        try {
            //récupére le chemin du projet
            String logPath = Scenario.projet;

            //enléve le slash d'ouverture de dossier
            if(logPath.charAt(0)=='/'){
                logPath = logPath.substring(1);
            }
            //a commande log de Git
            LogCommand logs = studentGit.log().addPath(logPath);
            //On appellle la commande log
            Iterable<RevCommit> lastCommits = logs.call();
            //On récupére le premier commit et on vérifie la date
            for (RevCommit revCommit : lastCommits) {
                System.out.println(revCommit.getCommitTime());
                System.out.println(revCommit.getAuthorIdent().getName());
                //vérifie que le dernier push n'est pas réalisé par l"évaluateur
                if(!revCommit.getAuthorIdent().getName().equals("évaluateur")){
                    //vérifie si le commit a eu lieu après le dernier test
                    return (((long)revCommit.getCommitTime())*1000)>dateDernierTest;
                }
            }
            //pas de commit précédent donc pas besoin de tester
            return false;

        //erreur sur la commande log
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * mets à jour le repo git  local
     */
    public pullStatut maj(){
        try {
            //appelle la commande Pull
            PullResult pullRes = studentGit.pull().setCredentialsProvider(new UsernamePasswordCredentialsProvider(utilisateur, motDePasse)).call();

            //vérifie les résultats de rebasage
            MergeResult mergeRes = pullRes.getMergeResult();

            //TODO : à supprimer
            System.out.println(mergeRes.getMergeStatus().toString());

            //statut du pull
            if(mergeRes.getMergeStatus()==org.eclipse.jgit.api.MergeResult.MergeStatus.ALREADY_UP_TO_DATE){
                //aucun changement sur le git
                return pullStatut.UP_TO_DATE;
            } else if(((mergeRes.getMergeStatus()==org.eclipse.jgit.api.MergeResult.MergeStatus.MERGED)||(mergeRes.getMergeStatus()==org.eclipse.jgit.api.MergeResult.MergeStatus.FAST_FORWARD))&&(pullRes.isSuccessful())){
                //Des changements et la fusion a réussi
                return pullStatut.MAJ;
            } else{
                //il y a eu un conflit
                return pullStatut.CONFLIT;
            }

        //erreur de commande JGit
        } catch (GitAPIException e) {
            System.out.println("erreur sur la commande pull");
            e.printStackTrace();
            return pullStatut.CONFLIT;
        }
    }

    /**
     * Push le dossier donner en paramétre dans le dossier du projet
     * @param aPush, dossier à push dans le dossier du projet du repository
     */
    public void push(File aPush){
        try {
            //récupére le chemin du projet
            String projetPath = Scenario.projet;

            //enléve le slash d'ouverture de dossier
            if(projetPath.charAt(0)=='/'){
                projetPath = projetPath.substring(1);
            }

            //Construction de la commande add
            AddCommand add = studentGit.add().addFilepattern(projetPath+"/"+aPush.getName()+"/");
            //appel de la commande add
            add.call();
        } catch (GitAPIException e) {
            e.printStackTrace();
            System.out.println("erreur lors de l'appel de la commande add");
        }
        try {
            //création de l'identité de l'évaluateur
            PersonIdent evaluateurIdentité = new PersonIdent("évaluateur",Scenario.mail);
            //construction de la commande commit
            CommitCommand commit = studentGit.commit().setAuthor(evaluateurIdentité).setMessage("evaluation");
            //appel de la commande commit
            commit.call();
        } catch (GitAPIException e) {
            e.printStackTrace();
            System.out.println("erreur lors de l'appel de la commande commit");
        }
        try {
            //Construction de la commande push
            PushCommand push = studentGit.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(utilisateur, motDePasse));
            //appel de la commande push
            push.call();
        } catch (GitAPIException e) {
            e.printStackTrace();
            System.out.println("erreur lors de l'appel de la commande push");
        }

    }
}
