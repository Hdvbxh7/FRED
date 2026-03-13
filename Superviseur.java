import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import configuration.Configuration;

public class Superviseur {

    static ArrayList<File> dossierATester;
    static ExecutorService threadPool= Executors.newFixedThreadPool(Configuration.nbThread);
    static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {

        //récupére les dossiers
        dossierATester = Configuration.explorateur.preTraitement();

        //lance le scénario sur chacun en multi threading
        for(File doss : dossierATester){
            threadPool.submit(new Configuration(doss,Configuration.explorateur.getResultatDepuisDossierTeste(doss)));
        }

        try {
            threadPool.shutdown();
            threadPool.awaitTermination(Configuration.waitingTimeBeforeCrash, Configuration.timeUnit);
            threadPool.shutdownNow();
        } catch (InterruptedException e) {
            System.out.println("thread de Dossier git a Tester interrompu");
        }

        //On fait le post processing(la fonction peut ne rien faire)
        Configuration.explorateur.postTraitement();

    }
}
