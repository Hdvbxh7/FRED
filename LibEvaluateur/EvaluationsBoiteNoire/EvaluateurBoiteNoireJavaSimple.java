package LibEvaluateur.EvaluationsBoiteNoire;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    }


    /**
     * @param fichiersList liste des fichiers {@code .class}
     * @param arguments liste d'arguments passés aux programmes lors de l'exécution
     */
    public EvaluateurBoiteNoireJavaSimple(List<File> fichiersList, List<String> arguments) {
        super();
        fichiers.addAll(fichiersList);
        this.arguments = arguments;
    }

    /**
     * Convertit les résultats d'exécution en format TAP.
     *
     * @param SortieTest sortie brute produite lors de l'exécution des tests
     */
    protected void resultatVersTAP(String SortieTest) {

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
        
            String outputTest = streamToString(processTest.getInputStream());
            String errorTest = streamToString(processTest.getErrorStream());
        
            int exitTest = processTest.waitFor();
        
            // Run reference class
            List<String> refCommand = new ArrayList<>();
            refCommand.add("java");
            refCommand.add("-cp");
            refCommand.add(classpath.toString());
            refCommand.add(referenceClassName);
        
            if (arg != null) {
                refCommand.add(arg);
            }
        
            Process processRef = new ProcessBuilder(refCommand).start();
        
            String outputRef = streamToString(processRef.getInputStream());
            String errorRef = streamToString(processRef.getErrorStream());
        
            int exitRef = processRef.waitFor();
        
            // Compare results
            boolean sameOutput =
                    outputTest.trim().equals(outputRef.trim());
        
            boolean sameError =
                    errorTest.trim().equals(errorRef.trim());
        
            boolean sameExitCode =
                    exitTest == exitRef;
        
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