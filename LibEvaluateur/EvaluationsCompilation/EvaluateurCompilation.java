package LibEvaluateur.EvaluationsCompilation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import LibEvaluateur.Evaluateur;

public abstract class EvaluateurCompilation extends Evaluateur {


    protected List<File> fichiers = new ArrayList<File>();

    public EvaluateurCompilation() {
        super();
    }
    
}
