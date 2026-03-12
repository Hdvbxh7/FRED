package LibEvaluateur.EvaluationsCompilation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import LibEvaluateur.Evaluateur;

public abstract class EvaluateurCompilation extends Evaluateur {


    protected List<File> fichiers = new ArrayList<File>();

    protected String dossiercompilé;

    public String getDossiercompilé() {
        return this.dossiercompilé;
    }

    public void setDossiercompilé(String dossiercompilé) {
        this.dossiercompilé = dossiercompilé;
    }



    public EvaluateurCompilation() {
        super();
    }
    
}
