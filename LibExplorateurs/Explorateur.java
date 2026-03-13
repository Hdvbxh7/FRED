package LibExplorateurs;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import configuration.Configuration;

/**
 * Classe utiliser pour savoir quel superviseur
 * sera utilisé
 * @see ExplorateurGit ExplorateurGit
 * @see ExplorateurConfiguration ExplorateurScenario
 * @see ExplorateurSimple ExplorateurSimple
 */
public abstract class Explorateur {

    ExecutorService threadPool;
    protected ArrayList<File> nomResultat;
    protected ArrayList<File> dossiersATester;

    public void addNomResultat(File dossier){
        nomResultat.add(dossier);
    }

    public void addDossierATester(File dossier){
        dossiersATester.add(dossier);
    }

    //methode
    public abstract ArrayList<File> preTraitement();

    public File getResultatDepuisDossierTeste(File dossier){
        int indice = dossiersATester.indexOf(dossier);
        return nomResultat.get(indice);
    }

    public abstract void postTraitement();

    /**
     * On créer l'ensemble de thread
     */
    protected void initiationThreadPool(){
        threadPool = Executors.newFixedThreadPool(Configuration.nbThread);
    }

    /**
     * attend la fin des thread et n'en accepte pas de nouveau
     */
    protected void shutdownAndWaitForTermination(){
        try {
            threadPool.shutdown();
            threadPool.awaitTermination(Configuration.waitingTimeBeforeCrash, Configuration.timeUnit);
            threadPool.shutdownNow();
        } catch (InterruptedException e) {
            System.out.println("thread de l'explorateurS interrompu");
        }
    }
}
