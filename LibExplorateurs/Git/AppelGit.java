package LibExplorateurs.Git;
import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import configuration.Scenario;
import configuration.Utiles;

/**
 * Bibliothéque de fonctions 
 * qui permettent de gérer le git
 */
public class AppelGit {

    //attributs

    /** Repo Local qui contient les dossiers */
    File studentRepo;

    public File getStudentRepo() {
        return studentRepo;
    }

    /** Git qui contient les dossiers */
    Git studentGit;
    /** identifiant de l'eleve */
    String identifiant;
    
    public String getIdentifiant() {
        return identifiant;
    }

    /** Statut du dernier pull */
    public pullStatut statut;
    /** nom d'utilisateur */
    private String utilisateur;
    /** mots de passe */
    private String motDePasse;
    /** il y a besoin de s'authentifier */
    private boolean authentification = false;

    /** Statut du pull */
    public static enum pullStatut {
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
    public AppelGit(String gitUrl,File doss,String identifiantEleve){
        identifiant = identifiantEleve;
        try {
            // vérifie si le dossier local existe et le supprime sinon
            File usermdp = new File("mdpGit.txt");
            if(usermdp.exists()){
                String stringUsermdp = Utiles.content(usermdp);
                String[] info = stringUsermdp.split(":");
                utilisateur = info[0];
                motDePasse = info[1];
                authentification = true;
            }
            if(!(new File(doss.getCanonicalPath()+Scenario.projet)).exists()){
                try {    
                    //clonage du repository
                    clonage(doss, gitUrl);

                
                    //récupération du repository local dans la classe
                    studentRepo = doss; 

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
                studentRepo = doss;  
                
                //mise à jour du dernier statut de pull
                statut = maj();

                //Il y a des conflits
                if(statut == pullStatut.CONFLIT){
                    //suppression du dossier de l'éléve
                    Utiles.destroyFile(doss);

                    //clonage du Git
                    clonage(doss, gitUrl);

                    //récupération du repository local dans la classe
                    studentRepo = doss; 
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
    void clonage(File repository,String url) throws GitAPIException{
        if(authentification){
            studentGit = Git.cloneRepository()
            .setURI(url).setCredentialsProvider(new UsernamePasswordCredentialsProvider(utilisateur, motDePasse))
            .setDirectory(repository)
            .call();
        } else {
             studentGit = Git.cloneRepository()
            .setURI(url)
            .setDirectory(repository)
            .call();
        }
    }
    
   /**
    * Vérifie si il y a eu un commit depuis le dernier test
    * @param dateDernierTest date du dernier test
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
            PullResult pullRes = null;
            //appelle la commande Pull
            if(authentification){
                pullRes = studentGit.pull().setCredentialsProvider(new UsernamePasswordCredentialsProvider(utilisateur, motDePasse)).call();
            } else {
                pullRes = studentGit.pull().call();
            }


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
     * @param aPush dossier à push dans le dossier du projet du repository
     * @param mailCommit mail du commiteur
     */
    public void push(File aPush,String mailCommit){
        boolean conflit = false;
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
            conflit = true;
        }
        try {
            //création de l'identité de l'évaluateur
            PersonIdent evaluateurIdentité = new PersonIdent("évaluateur",mailCommit);
            //construction de la commande commit
            CommitCommand commit = studentGit.commit().setAuthor(evaluateurIdentité).setCommitter(evaluateurIdentité).setMessage("evaluation");
            //appel de la commande commit
            commit.call();
        } catch (GitAPIException e) {
            e.printStackTrace();
            System.out.println("erreur lors de l'appel de la commande commit");
            conflit = true;
        }
        try {
            PushCommand push = null;
            //Construction de la commande push
            if(authentification){
                push = studentGit.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(utilisateur, motDePasse));
            } else{
                push = studentGit.push();
            }
            //appel de la commande push
            push.call();
        } catch (GitAPIException e) {
            e.printStackTrace();
            System.out.println("erreur lors de l'appel de la commande push");
            conflit = true;
        }
        if(!conflit){

        }

    }
}
