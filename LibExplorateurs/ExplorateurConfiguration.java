package LibExplorateurs;

import java.io.File;
import java.util.ArrayList;

import configuration.Configuration;

/**
 * Explorateur utilisant les fonctions défini dans scénario
 */
public class ExplorateurConfiguration extends Explorateur{
    
    //methode
    public ArrayList<File> preTraitement(){
        return Configuration.listeDossier(this);
    }

    public void postTraitement(){
        Configuration.postprocess(this);
    }
}
