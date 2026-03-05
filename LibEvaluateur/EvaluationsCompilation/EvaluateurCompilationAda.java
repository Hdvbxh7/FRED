package LibEvaluateur.EvaluationsCompilation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.tap4j.model.Directive;
import org.tap4j.model.TestResult;
import org.tap4j.util.StatusValues;

public class EvaluateurCompilationAda extends EvaluateurCompilation {
	
    private TestSet ensembleTest;


    protected List<File> fichiers = new ArrayList<File>();

    public EvaluateurCompilationAda(File binaire) { 
        super();
        fichiers.add(binaire);
    }

    public EvaluateurCompilationAda(File binaire, ArrayList<File> dependences) { 
        super();
        fichiers.add(binaire);
        fichiers.addAll(dependences);
    }

    public String getResultat() {
        return this.resultat;
    }
    
    public Boolean[] getTestsResultat() {
        return testsResultat;
    }

    /**
     * Convertit la sortie brute de gnatmake en format TAP.
     *
     * @param SortieTest la sortie brute produite par gnatmake
     */
    protected void resultatVersTAP(String SortieTest) {
        testsResultat = new Boolean[1];
		ensembleTest.setPlan( new Plan(2) );
		TestResult presErr;
		TestResult presAvrt;
		if (SortieTest.isEmpty()){
			presErr = new TestResult( StatusValues.OK, 1 );
			presAvrt = new TestResult( StatusValues.OK, 2);
	        testsResultat[0] = true;
	        testsResultat[1] = true;
			}
		else if (derniereLigne(SortieTest).contains("\" compilation error")) {
			// On pourrait rendre ça plus robuste avec des regexp..
			presErr = new TestResult( StatusValues.NOT_OK, 1 );
            Directive dirAvrt = new Directive(DirectivesValues.SKIP, "Erreur de compilation.");
            presAvrt = new TestResult( StatusValues.NOT_OK, 2);
            presAvrt.setDirective(dirAvrt);
            testsResultat[0] = false;
            testsResultat[1] = false;
		} else {
			presErr = new TestResult( StatusValues.OK, 1 );
	        presAvrt = new TestResult( StatusValues.NOT_OK, 2);
	        testsResultat[0] = true;
	        testsResultat[1] = false;
		}
		ensembleTest.addTestResult( presErr );
		ensembleTest.addTestResult( presAvrt );
		this.resultat = producteur.dump( ensembleTest );
	}

    public void evaluer() throws Exception {
        StringWriter sw = new StringWriter();
        BufferedWriter out = new BufferedWriter(sw);

        compileAda(out, fichiers.get(0).getAbsolutePath());
        out.close();
        
        String resultatsBruts = sw.toString();
        resultatVersTAP(resultatsBruts);
    }

    private static void compileAda(BufferedWriter out, String file) {
        try {
            File outputDir = new File("LibEvaluateur/EvaluationsCompilation/Compiled_Code");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            ProcessBuilder pb = new ProcessBuilder(
                "gnatmake",
                "-D", "LibEvaluateur/EvaluationsCompilation/Compiled_Code",
                "-gnat2012",
                "-Wall",
                file
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                out.write(output.toString());
            }

        } catch (Exception e) {
            try {
                out.write("Erreur de compilation Ada: " + e.getMessage());
            } catch (Exception ex) {
                System.out.println("erreur" + ex);
            }
        }
    }

    public static void main(String[] args) {
        try {
            File aTester = new File("LibEvaluateur/EvaluationsCompilation/CompilationTest.ada");
            EvaluateurCompilationAda eval = new EvaluateurCompilationAda(aTester);

            eval.evaluer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String derniereLigne(String text) {
    	return text.substring(text.lastIndexOf('\n'));
    }
}
