<<<<<<< HEAD
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

<<<<<<< HEAD
public class EvaluateurStyle extends Evaluateur {
    
>>>>>>> daa3d7c (Classes Abstraites de la lib bougée dans leur fichier respectifs)
=======
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

>>>>>>> 6b87d6c (fin du setup)
=======
package LibEvaluateur;

public class EvaluateurStyle extends Evaluateur {
    
>>>>>>> daa3d7c (Classes Abstraites de la lib bougée dans leur fichier respectifs)
}
