import java.io.File;
import java.util.GregorianCalendar;

public class Superviseur {

    private static final String gitPath = "https://hudson.inp-toulouse.fr/projects_group/2023-1sn-tob/1sn-e/tgz8009/tp.git";
    private static final GregorianCalendar endOfEvaluation = new GregorianCalendar(2026,GregorianCalendar.FEBRUARY,19,21,54);
    private static void scenario(File dossierEtudiant){
        //Test Scenario
    }
    
    public static void main(String[] args) {
        //création du repo Git
        AppelGit Git = new AppelGit(gitPath);

        //récupération des dossiers étudiants
        File repoEleve = new File("studentRepo");
        File[] dossierEleves = repoEleve.listFiles(new DossFilter("TP01"));
        for(File dossier:dossierEleves){
                scenario(dossier);
        }
        /*
        //vérifie que la date d'évaluation n'est pas dépassé
        while (endOfEvaluation.getTimeInMillis()<(new GregorianCalendar().getTimeInMillis())) {
            try {
                //mets à jour le repo local
                Git.maj();
                Thread.sleep(waitTiming);   
            //renvoi une erreur si le thread ne s'est pas endormi
            } catch (InterruptedException e) {
                System.out.println("erreur system interrompu");
                e.printStackTrace();
            }
        }
        //supprime le dossier des éléves et le repository local
        Git.destroy();
        */
    }
    
}
