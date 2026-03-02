import java.io.File;
import java.util.ArrayList;

import configuration.Scenario;

public class Superviseur {

    static ArrayList<File> dossiersATester;
    static ArrayList<Thread> tList;
    
    public static void main(String[] args) {
        tList = new ArrayList<>();

        //récupére les dossiers
        dossiersATester = Scenario.explorateur.listeDossier();

        //lance le scénario sur chacun en multi threading
        for(File doss : dossiersATester){
            Thread t = Thread.startVirtualThread(new Scenario(doss));
            tList.add(t);
        }

        try {
            //Attend la fin de tout les threads
            for(Thread t : tList){
                t.join();
            }
        } catch (InterruptedException e) {
            System.out.println("thread de Dossier git a Tester interrompu");
        }

        //On fait le post processing(la fonction peut ne rien faire)
        Scenario.explorateur.postprocess();

    }
}
