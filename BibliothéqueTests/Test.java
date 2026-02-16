package BibliothéqueTests;

public abstract class Test {
    
 private String resultat;

 public String getResultat() {
    return resultat;
    }

 private Boolean[] testsResults;

 public Boolean[] getTestsResults() {
    return testsResults;
    }

 protected abstract void ResultsToTAP();

 public abstract void run(String[] args);

    
}
