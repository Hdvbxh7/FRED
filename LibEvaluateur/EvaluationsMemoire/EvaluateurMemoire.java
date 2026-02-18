package LibEvaluateur.EvaluationsMemoire;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import LibEvaluateur.Evaluateur;

public abstract class EvaluateurMemoire extends Evaluateur {

    protected List<File> fichiers = new ArrayList<File>();

    public EvaluateurMemoire() {
        super();
    }

    public String getResultat() {
        return this.resultat;
    }
    
    public Boolean[] getTestsResultat() {
        return testsResultat;
    }

    protected abstract void resultatVersTAP(String SortieTest);

    public abstract void evaluer() throws Exception;

}

    

