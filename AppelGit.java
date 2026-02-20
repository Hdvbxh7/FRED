import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;


public class AppelGit {

    //attributs
    /** Repo Local qui contient les dossiers */
    Repository studentRepos;
    /** Git qui contient les dossiers */
    Git studentGit;
    /** Dossier Local qui contiient les repos */
    final File localDir = new File("studentRepo");

    private String content(File file){
        try {
            InputStream is = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader buffer = new BufferedReader(isr);

            String line = buffer.readLine();
            StringBuilder builder = new StringBuilder();
        
            while(line != null){
                builder.append(line).append("\n");
                line = buffer.readLine();
            }
            buffer.close();
            isr.close();
            is.close();
            return builder.toString();
        } catch (IOException e) {
            System.out.println("error during the reading of log in resultat");
            return null;
        }
    }

    private void destroyFile(File file){
        if(file.isDirectory()){
            File[] fichierASupprimer = file.listFiles();
            for(File fichier:fichierASupprimer){
                destroyFile(fichier);
            }
            file.delete();
        } else{
            file.delete();
        }
    }
    
    //constructeur
    /**
     * Clone le répértoire Git spécifié par GitUrl
     * dans un dossier studentRepos
     * @param gitUrl Url du git a cloné
     */
    AppelGit(String gitUrl){
        try {
            // vérifie si le dossier local existe et le supprime sinon
            if (localDir.exists()) {
                destroyFile(localDir);
                localDir.mkdirs();
            } else{
                localDir.mkdirs();
            }
            String usermdp = content(new File("mdpGitlab.txt"));
            String[] info = usermdp.split(":");          

            //clonage du repository
            File studentDir = new File(localDir.getCanonicalPath()+"/student1");
            studentDir.mkdirs();
            studentGit = Git.cloneRepository()
                    .setURI(gitUrl).setCredentialsProvider(new UsernamePasswordCredentialsProvider(info[0], info[1]))
                    .setDirectory(studentDir)
                    .call();
            
            //récupération du repository local dans la classe
            studentRepos = studentGit.getRepository();
        //Erreur de la part de l'APIGit JGit
        } catch (GitAPIException e) {
            System.out.println("erreur lié au clonage du git");
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Détruit le dossier local contenant le repository et les git
     */
    public void destroy(){
        studentRepos.close();
        localDir.delete();
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
