package LibEvaluateur.EvaluationsBoiteNoire;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
     * @param fichiersList liste des exécutables Ada
     */
    public EvaluateurBoiteNoireAdaSimple(List<File> fichiersList) {
        super();
        fichiers.addAll(fichiersList);
    }

    /**
     * @param fichiersList liste des exécutables Ada
     * @param arguments arguments à passer lors de l'exécution
     */
    public EvaluateurBoiteNoireAdaSimple(List<File> fichiersList, List<String> arguments) {
        super();
        fichiers.addAll(fichiersList);
        this.arguments = arguments;
    }

    /**
     * Convertit la sortie brute d'un test en format TAP.
     *
     *
     * @param SortieTest sortie brute produite lors de l'exécution
     */
    protected void resultatVersTAP(String SortieTest) {
        // Optional TAP formatting
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

            Process processTest = new ProcessBuilder(testCommand).start();

            String outputTest = streamToString(processTest.getInputStream());
            String errorTest = streamToString(processTest.getErrorStream());
            int exitTest = processTest.waitFor();

            // ---- RUN REFERENCE EXECUTABLE ----
            List<String> refCommand = new ArrayList<>();
            refCommand.add(referenceExecutable.getAbsolutePath());

            if (arg != null) {
                refCommand.add(arg);
            }

            Process processRef = new ProcessBuilder(refCommand).start();

            String outputRef = streamToString(processRef.getInputStream());
            String errorRef = streamToString(processRef.getErrorStream());
            int exitRef = processRef.waitFor();

            // ---- COMPARE RESULTS ----
            boolean sameOutput = outputTest.trim().equals(outputRef.trim());
            boolean sameError = errorTest.trim().equals(errorRef.trim());
            boolean sameExitCode = exitTest == exitRef;

            testsResultat[i] = sameOutput && sameError && sameExitCode;
        }
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