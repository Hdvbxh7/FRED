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
 * Evaluateur de tests en boîte noire pour des programmes Ada compilés.
 * <p>
 * Les fichiers fournis doivent être des exécutables Ada déjà compilés
 * (par exemple générés par {@code gnatmake}).
 * </p>
 */
public class EvaluateurBoiteNoireAdaSimple extends EvaluateurBoiteNoire {


    /**
     * Construit un évaluateur avec une liste d'exécutables Ada.
     * @param fichiersList liste des exécutables Ada, le premier est le fichier a tester, le deuxième est la réference
     */
    public EvaluateurBoiteNoireAdaSimple(List<File> fichiersList) {
        super();
        fichiers.addAll(fichiersList);
        this.timedOut = new boolean[1];
    }

    /**
     * @param fichiersList liste des exécutables Ada, le premier est le fichier a tester, le deuxième est la réference
     * @param arguments arguments à passer lors de l'exécution
     */
    public EvaluateurBoiteNoireAdaSimple(List<File> fichiersList, List<String> arguments) {
        super();
        fichiers.addAll(fichiersList);
        this.arguments = arguments;
        this.timedOut = new boolean[arguments.size()];
    }

    /**
     * Convertit la sortie brute d'un test en format TAP.
     *
     *
     * @param SortieTest sortie brute produite lors de l'exécution
     */
    protected void resultatVersTAP(String sortieTest) {
        this.resultat = sortieTest;
    }

    /**
     * Lance l'évaluation en boîte noire des exécutables Ada.
     *
     * @throws Exception si les exécutables sont manquants ou invalides
     */
    public void evaluer() throws Exception {

        if (fichiers.size() < 2) {
            throw new Exception("At least two files required: test executable and reference executable");
        }

        File testExecutable = fichiers.get(0);
        File referenceExecutable = fichiers.get(1);

        if (!testExecutable.exists() || !referenceExecutable.exists()) {
            throw new Exception("Executable file not found.");
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

            // ---- RUN TEST EXECUTABLE ----
            List<String> testCommand = new ArrayList<>();
            testCommand.add(testExecutable.getAbsolutePath());

            if (arg != null) {
                testCommand.add(arg);
            }
			testBoiteNoire.addComment(new Comment("Commande de test : " + testExecutable.getName() + " " + arg));


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

            // ---- RUN REFERENCE EXECUTABLE ----
            List<String> refCommand = new ArrayList<>();
            refCommand.add(referenceExecutable.getAbsolutePath());

            if (arg != null) {
                refCommand.add(arg);
            }

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

            // ---- COMPARE RESULTS ----
            TestResult resComp;

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

}