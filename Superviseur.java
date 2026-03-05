import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import configuration.Scenario;

public class Superviseur {

    static ArrayList<File> dossierATester;
    static ExecutorService threadPool= Executors.newFixedThreadPool(Scenario.nbThread);
    static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {

        //récupére les dossiers
        dossierATester = Scenario.explorateur.listeDossier();

        //lance le scénario sur chacun en multi threading
        for(File doss : dossierATester){
            System.out.println(doss.getName());
            Future<?> future = threadPool.submit(new Scenario(doss,Scenario.explorateur.getResultatDepuisDossierTeste(doss)));
            /*scheduler.schedule(() -> {
            //termine le thread de test au temps prédéfini
            if (!future.isDone()) {
                future.cancel(true);
                try {
                    System.out.println("erreur lors de l'éxécution des tests sur " + doss.getCanonicalPath());
                } catch (IOException e) {
                   System.out.println("probléme sur le dossier de test");
                }
            }
        }, Scenario.waitingTimeBeforeCrashScenario, Scenario.timeUnitScenario);*/
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
