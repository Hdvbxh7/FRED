package LibEvaluateur.EvaluationsStyle;

import LibEvaluateur.Evaluateur;

public abstract class EvaluateurStyle extends Evaluateur {
     private String resultat;

 public String getResultat() {
    return resultat;
    }

 private Boolean[] testsResults;

 public Boolean[] getTestsResults() {
    return testsResults;
    }

 protected abstract void ResultsToTAP();

 public abstract void run();

}
