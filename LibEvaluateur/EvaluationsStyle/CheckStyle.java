package LibEvaluateur.EvaluationsStyle;

import LibEvaluateur.EvaluateurStyle;

public class CheckStyle extends EvaluateurStyle {
    private String resultat;

    public String getResultat() {
        return resultat;
    }

    private Boolean[] testsResults;

    public Boolean[] getTestsResults() {
        return testsResults;
    }

    protected void ResultsToTAP(){
    }

    public abstract void run(String[] args);
}
