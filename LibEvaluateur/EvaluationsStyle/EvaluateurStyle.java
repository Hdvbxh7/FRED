<<<<<<< HEAD
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

=======
package LibEvaluateur;

public class EvaluateurStyle extends Evaluateur {
    
>>>>>>> daa3d7c (Classes Abstraites de la lib bougée dans leur fichier respectifs)
}
