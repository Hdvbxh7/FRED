package LibExplorateurs;

import java.io.File;
import java.util.ArrayList;

public class ExplorateurSimple extends Explorateur{

    File dossierSource;

    public ExplorateurSimple(File source){
        dossierSource = source;
        nomResultat = new ArrayList<File>();
    }
    
    //methode
    public ArrayList<File> listeDossier(){
        File[] dossierEleves = dossierSource.listFiles(new DossFilter());
        dossiersATester = new ArrayList<>();
        for(File dossier : dossierEleves){
            dossiersATester.add(dossier);
            addNomResultat(new File("resultat/"+dossier.getName()));
        }
        return dossiersATester;
    }

    public void postprocess(){

    }
}
