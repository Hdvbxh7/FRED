package LibEvaluateur;

public abstract class Evaluateur {
    
 private String resultat;

 public String getResultat() {
    return resultat;
    }

 private Boolean[] testsResultat;

 public Boolean[] getTestsResultat() {
    return testsResults;
    }

 protected abstract void ResutltatVersTAP();

 public abstract void evaluer();

    
}
