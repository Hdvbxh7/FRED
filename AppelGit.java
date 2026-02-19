import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;


public class AppelGit {

    //attributs
    /** Repo Local qui contient les dossiers */
    Repository studentRepos;
    /** Git qui contient les dossiers */
    Git studentGit;
    /** Dossier Local qui contiient les repos */
    final File localDir = new File("studentRepo");
    
    //constructeur
    /**
     * Clone le répértoire Git spécifié par GitUrl
     * dans un dossier studentRepos
     * @param gitUrl Url du git a cloné
     */
    AppelGit(String gitUrl){
        try {
            // vérifie si le dossier local existe et le supprime sinon
            if (!localDir.exists()) {
                localDir.mkdirs();
            }

            //clonage du repository
            studentGit = Git.cloneRepository()
                    .setURI(gitUrl)
                    .setDirectory(localDir)
                    .call();
            
            //récupération du repository local dans la classe
            studentRepos = studentGit.getRepository();
        //Erreur de la part de l'APIGit JGit
        } catch (GitAPIException e) {
            System.out.println("erreur lié au clonage du git");
            e.printStackTrace();
        } 
    }

    /**
     * Détruit le dossier local contenant le repository et les git
     */
    public void destroy(){
        studentRepos.close();
        localDir.mkdirs();
    }

    /**
     * mets à jour le repo git  local
     */
    public boolean maj(){
        try {
            PullResult pullRes = studentGit.pull().call();
            RebaseResult rebRes = pullRes.getRebaseResult();
            if(rebRes.getStatus()==org.eclipse.jgit.api.RebaseResult.Status.UP_TO_DATE){
                return false;
            } else if((rebRes.getStatus()==org.eclipse.jgit.api.RebaseResult.Status.OK)&&(pullRes.isSuccessful())){
                return true;
            } else{
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
