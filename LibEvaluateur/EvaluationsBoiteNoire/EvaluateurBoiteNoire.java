package LibEvaluateur.EvaluationsBoiteNoire;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import LibEvaluateur.Evaluateur;

public abstract class EvaluateurBoiteNoire extends Evaluateur {

    protected List<File> fichiers = new ArrayList<File>();

    public EvaluateurBoiteNoire() {
        super();
    }

    protected List<String> arguments = new ArrayList<String>();

    public List<String> getArguments() {
        return this.arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }



    protected abstract void resultatVersTAP(String SortieTest);

    public abstract void evaluer() throws Exception;
    
}
