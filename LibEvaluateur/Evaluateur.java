package LibEvaluateur;

public abstract class Evaluateur {
    
 protected String resultat;

 public String getResultat() {
    return resultat;
    }

 protected Boolean[] testsResultat;

 public Boolean[] getTestsResultat() {
    return testsResultat;
    }

 protected abstract void ResutltatVersTAP();

 public abstract void evaluer();

    
}
