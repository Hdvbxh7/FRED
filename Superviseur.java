import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import configuration.Scenario;

public class Superviseur {

    static ArrayList<File> dossiersATester;
    static ExecutorService threadPool= Executors.newFixedThreadPool(Scenario.nbThread);
    
    public static void main(String[] args) {

        //récupére les dossiers
        dossiersATester = Scenario.explorateur.listeDossier();

        //lance le scénario sur chacun en multi threading
        for(File doss : dossiersATester){
            threadPool.execute(new Scenario(doss));
        }

        try {
            threadPool.shutdown();
            threadPool.awaitTermination(Scenario.waitingTimeBeforeCrash, Scenario.timeUnit);
            threadPool.shutdownNow();
        } catch (InterruptedException e) {
            System.out.println("thread de Dossier git a Tester interrompu");
        }

        //On fait le post processing(la fonction peut ne rien faire)
        Scenario.explorateur.postprocess();

    }
}
