import java.io.File;

/**
 * Fichier java a lancer lorsque l'on veut faire 
 * des tests sur des étudiants en utilisant Git
 */
public class SuperviseurGit {

    /** Dossier Local qui contiient les repos */
    final static File localDir = new File("studentRepo");

    public static void main(String[] args) {
        //création du dossier qui contient le repo
        if (localDir.exists()) {
            AppelGit.destroyFile(localDir);
            localDir.mkdirs();
        } else{
            localDir.mkdirs();
        }

        //récupération des login et url
        File GitsPaths = new File("GitPaths.csv");
        String content = AppelGit.content(GitsPaths);
        String[] lines = content.split("\n");

        //lancement des tests et vérification des mises à jour par dossier
        //depuis les informations du csv
        for(int ind=1;ind<lines.length;ind++){
            //séparation des informations (1: login, 2: url)
            String[] values = lines[ind].split(",");
            
            //Thread t = Thread.startVirtualThread(new Evaluation(values[0], values[1], true));
            Runnable task = new Evaluation(values[0], values[1], true);
            task.run();
        }    
    }
}
