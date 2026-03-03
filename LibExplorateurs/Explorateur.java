package LibExplorateurs;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import configuration.Scenario;

/**
 * Classe utiliser pour savoir quel superviseur
 * sera utilisé
 * @see ExplorateurGit ExplorateurGit
 * @see ExplorateurScenario ExplorateurScenario
 * @see ExplorateurSimple ExplorateurSimple
 */
public abstract class Explorateur {

    ExecutorService threadPool;

    //methode
    public abstract ArrayList<File> listeDossier();

    public abstract void postprocess();

    /**
     * On créer l'ensemble de thread
     */
    protected void initiationThreadPool(){
        threadPool = Executors.newFixedThreadPool(Scenario.nbThread);
    }

    /**
     * attend la fin des thread et n'en accepte pas de nouveau
     */
    protected void shutdownAndWaitForTermination(){
        try {
            threadPool.shutdown();
            threadPool.awaitTermination(Scenario.waitingTimeBeforeCrash, Scenario.timeUnit);
            threadPool.shutdownNow();
        } catch (InterruptedException e) {
            System.out.println("thread de l'explorateurS interrompu");
        }
    }
}
