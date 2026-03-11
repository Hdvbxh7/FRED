package LibEvaluateur.EvaluationsBoiteNoire;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.tap4j.model.Comment;
import org.tap4j.model.Directive;
import org.tap4j.model.Plan;
import org.tap4j.model.TestResult;
import org.tap4j.model.TestSet;
import org.tap4j.util.DirectiveValues;
import org.tap4j.util.StatusValues;

/**
 * Evaluateur de tests en boîte noire pour des programmes Java.
 *
 * <p>
 * Les classes doivent être compilées et fournies sous forme de fichiers
 * {@code .class}. Le premier fichier correspond au programme testé et
 * le second au programme de référence.
 * </p>
 */
public class EvaluateurBoiteNoireJavaSimple extends EvaluateurBoiteNoire {

    /**
     * Construit un évaluateur avec une liste de fichiers.
     * @param fichiersList liste des fichiers {@code .class} nécessaires
     */
    public EvaluateurBoiteNoireJavaSimple(List<File> fichiersList) {
        super();
        fichiers.addAll(fichiersList);
        this.timedOut = new boolean[1];
    }


    /**
     * @param fichiersList liste des fichiers {@code .class}
     * @param arguments liste d'arguments passés aux programmes lors de l'exécution
     */
    public EvaluateurBoiteNoireJavaSimple(List<File> fichiersList, List<String> arguments) {
        super();
        fichiers.addAll(fichiersList);
        this.arguments = arguments;
        this.timedOut = new boolean[arguments.size()];
    }

    /**
     * Convertit les résultats d'exécution en format TAP.
     *
     * @param SortieTest sortie brute produite lors de l'exécution des tests
     */
    protected void resultatVersTAP(String sortieTest) {
    	this.resultat = sortieTest;
    }

    /**
     * Lance l'évaluation en boîte noire.
     * @throws Exception si le nombre de fichiers fournis est insuffisant
     */
    public void evaluer() throws Exception {
    
        if (fichiers.size() < 2) {
            throw new Exception("At least two files required: test file and reference file");
        }
    
        File testFile = fichiers.get(0);
        File referenceFile = fichiers.get(1);
    
        // ---- Build FULLY QUALIFIED class names ----
        String testClassName = testFile.getPath()
                .replace(File.separator, ".")
                .replace(".class", "");
    
        String referenceClassName = referenceFile.getPath()
                .replace(File.separator, ".")
                .replace(".class", "");
    
        // Remove leading "./" or similar
        if (testClassName.startsWith(".")) {
            testClassName = testClassName.substring(1);
        }
        if (referenceClassName.startsWith(".")) {
            referenceClassName = referenceClassName.substring(1);
        }
    
        // ---- Build classpath ----
        StringBuilder classpath = new StringBuilder();
    
        // Root of project
        classpath.append(new File(".").getAbsolutePath());
    
        // Add additional classpath entries
        for (int i = 2; i < fichiers.size(); i++) {
            classpath.append(File.pathSeparator);
            classpath.append(fichiers.get(i).getAbsolutePath());
        }
    
        int numberOfTests = (arguments == null || arguments.isEmpty())
                ? 1
                : arguments.size();
    
        testsResultat = new Boolean[numberOfTests];
        TestSet testBoiteNoire = new TestSet();
		testBoiteNoire.setPlan(new Plan(numberOfTests));
    
        for (int i = 0; i < numberOfTests; i++) {
        
            String arg = (arguments == null || arguments.isEmpty())
                    ? null
                    : arguments.get(i);
        
            // Run test class
            List<String> testCommand = new ArrayList<>();
            testCommand.add("java");
            testCommand.add("-cp");
            testCommand.add(classpath.toString());
            testCommand.add(testClassName);
        
            if (arg != null) {
                testCommand.add(arg);
            }
        
            Process processTest = new ProcessBuilder(testCommand).start();

            boolean finishedTest = processTest.waitFor(5, TimeUnit.SECONDS);

            String outputTest = "";
            String errorTest = "";
            int exitTest = -1;

            if (finishedTest) {
                outputTest = streamToString(processTest.getInputStream());
                errorTest = streamToString(processTest.getErrorStream());
                exitTest = processTest.exitValue();
            } else {
                processTest.destroyForcibly();
                timedOut[i] = true;
                System.out.println("Test executable timed out");
            }
        
            // Run reference class
            List<String> refCommand = new ArrayList<>();
            refCommand.add("java");
            refCommand.add("-cp");
            refCommand.add(classpath.toString());
            refCommand.add(referenceClassName);
        
            if (arg != null) {
                refCommand.add(arg);
            }
			testBoiteNoire.addComment(new Comment("Commande de test : " + testClassName + " " + arg));
            Process processRef = new ProcessBuilder(refCommand).start();

            boolean finishedRef = processRef.waitFor(5, TimeUnit.SECONDS);

            String outputRef = "";
            String errorRef = "";
            int exitRef = -1;

            if (finishedRef) {
                outputRef = streamToString(processRef.getInputStream());
                errorRef = streamToString(processRef.getErrorStream());
                exitRef = processRef.exitValue();
            } else {
                processRef.destroyForcibly();
                timedOut[i] = true;
            }
        
            TestResult resComp;
            // ---- COMPARE RESULTS ----
            if (timedOut[i]) {
                testsResultat[i] = false;
                resComp =  new TestResult(StatusValues.NOT_OK, 1);
        		Directive dirTO = new Directive(DirectiveValues.SKIP, "Temps d'execution dépassé.");
        		resComp.setDirective(dirTO);
            } else {
                boolean sameOutput = outputTest.trim().equals(outputRef.trim());
                boolean sameError = errorTest.trim().equals(errorRef.trim());
                boolean sameExitCode = exitTest == exitRef;
                testsResultat[i] = sameOutput && sameError && sameExitCode;
    			if (testsResultat[i]) {
    				resComp =  new TestResult(StatusValues.OK, i+1);
    			} else {
    				resComp =  new TestResult(StatusValues.NOT_OK, i+1);
    				if (!sameOutput) {
    					resComp.setDescription("Différences avec le retour attendu trouvées :");
    					List<String> rapportDiff = lignesDiffs(outputTest, outputRef);
    					TestSet ensDiff = new TestSet();
    					ensDiff.setPlan(new Plan(rapportDiff.size()));
    					for (int j = 0;j <= rapportDiff.size();j++) {
    						TestResult diffLigne = new TestResult(StatusValues.NOT_OK, j+1);
    						diffLigne.setDescription(rapportDiff.get(j));
    						ensDiff.addTestResult(diffLigne);
    					}
    					resComp.setSubtest(ensDiff);
    				} else if (!sameError) {
    					resComp.setDescription("Différences avec l'erreur attendu trouvées :");
    					List<String> rapportDiff = lignesDiffs(errorTest, errorRef);
    					TestSet ensDiff = new TestSet();
    					ensDiff.setPlan(new Plan(rapportDiff.size()));
    					for (int j = 0;j <= rapportDiff.size();j++) {
    						TestResult diffLigne = new TestResult(StatusValues.NOT_OK, j+1);
    						diffLigne.setDescription(rapportDiff.get(j));
    						ensDiff.addTestResult(diffLigne);
    					}
    					resComp.setSubtest(ensDiff);
    				} else {
    					resComp.setDescription("Mauvais code retour : trouvé " + exitTest + " attendu " + exitRef);
    				}
    			}
            }
			testBoiteNoire.addTestResult(resComp);
        }
        this.resultatVersTAP(producteur.dump(testBoiteNoire));
    }

    /**
     * Convertit un flux d'entrée en chaîne de caractères.
     *
     * @param in flux d'entrée
     * @return contenu du flux sous forme de chaîne
     * @throws IOException si une erreur de lecture survient
     */
    private String streamToString(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) { 
        ArrayList<File> liste = new ArrayList<>(); 
        liste.add(new File("BacATest/TestV.class")); 
        liste.add(new File("BacATest/TestF.class")); 
        ArrayList<String> in = new ArrayList<>(); 
        in.add("2"); 
        in.add("-3"); 
        EvaluateurBoiteNoireJavaSimple a = new EvaluateurBoiteNoireJavaSimple(liste, in); 
        try { 
            a.evaluer(); 
            Boolean[] results = a.getTestsResultat(); 
            for (int i = 0; i < results.length; i++) { 
                System.out.println("Test " + i + ": " + results[i]); 
            } 
            boolean[] timeout = a.getTimedOut(); 
            for (int i = 0; i < timeout.length; i++) { 
                System.out.println("timeout " + i + ": " + timeout[i]); 
            } 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
    }
}