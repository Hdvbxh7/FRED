package LibExplorateurs;

import java.io.File;
import java.util.ArrayList;

import configuration.Scenario;

/**
 * Explorateur utilisant les fonctions défini dans scénario
 */
public class ExplorateurScenario extends Explorateur{
    
    //methode
    public ArrayList<File> listeDossier(){
        return Scenario.listeDossier();
    }

    public void postprocess(){
        Scenario.postprocess();
    }
}
