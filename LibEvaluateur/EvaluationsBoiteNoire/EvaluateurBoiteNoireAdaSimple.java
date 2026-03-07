package LibEvaluateur.EvaluationsBoiteNoire;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EvaluateurBoiteNoireAdaSimple extends EvaluateurBoiteNoire {

    public EvaluateurBoiteNoireAdaSimple(List<File> fichiersList) {
        super();
        fichiers.addAll(fichiersList);
    }

    public EvaluateurBoiteNoireAdaSimple(List<File> fichiersList, List<String> arguments) {
        super();
        fichiers.addAll(fichiersList);
        this.arguments = arguments;
    }

    protected void resultatVersTAP(String SortieTest) {
        // Optional TAP formatting
    }

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
        
        // These must be compiled Ada executables (not .adb files)
        liste.add(new File("BacATest/square_int"));       // compiled test
        liste.add(new File("BacATest/double_int"));  // compiled reference

        ArrayList<String> in = new ArrayList<>();
        in.add("2");
        in.add("3");

        EvaluateurBoiteNoireAdaSimple a =
                new EvaluateurBoiteNoireAdaSimple(liste, in);

        try {
            a.evaluer();
            Boolean[] results = a.getTestsResultat();

            for (int i = 0; i < results.length; i++) {
                System.out.println("Test " + i + ": " + results[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}