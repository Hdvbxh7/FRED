package LibEvaluateur;

public abstract class Evaluateur {
    
 private String result;

 public String getResult() {
    return result;
    }

 private Boolean[] testsResults;

 public Boolean[] getTestsResults() {
    return testsResults;
    }

 protected abstract void ResultsToTAP();

 public abstract void run();

    
}
