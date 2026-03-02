package LibExplorateurs;

import java.io.File;
import java.util.ArrayList;

public class ExplorateurSimple extends Explorateur{

    File dossierSource;

    public ExplorateurSimple(File source){
        dossierSource = source;
    }
    //methode
    public ArrayList<File> listeDossier(){
        File[] dossierEleves = dossierSource.listFiles(new DossFilter());
        ArrayList<File> dossiersATester = new ArrayList<>();
        for(File dossier : dossierEleves){
            dossiersATester.add(dossier);
        }
        return dossiersATester;
    }

    public void postprocess(){

    }
}
