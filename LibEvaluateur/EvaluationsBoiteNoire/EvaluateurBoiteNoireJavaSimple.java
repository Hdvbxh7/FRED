package LibEvaluateur.EvaluationsBoiteNoire;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EvaluateurBoiteNoireJavaSimple extends EvaluateurBoiteNoire {

    

    public EvaluateurBoiteNoireJavaSimple(List<File> fichiersList) {
        super();
        fichiers.addAll(fichiersList);
    }



    public EvaluateurBoiteNoireJavaSimple(List<File> fichiersList, List<String> arguments) {
        super();
        fichiers.addAll(fichiersList);
        this.arguments = arguments;
    }

    protected void resultatVersTAP(String SortieTest) {

    }

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
    
        // Root of project (VERY IMPORTANT for packaged classes)
        classpath.append(new File(".").getAbsolutePath());
    
        // Add additional classpath entries (index >= 2)
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
        
            
            //ON RUN LA CLASSE A TESTER
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
        
            //ON RUN LA CLASSE TEMOIN
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
        
           
            //ON COMPARE
            boolean sameOutput =
                    outputTest.trim().equals(outputRef.trim());
        
            boolean sameError =
                    errorTest.trim().equals(errorRef.trim());
        
            boolean sameExitCode =
                    exitTest == exitRef;
        
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

        ArrayList<File> liste = new ArrayList<File>();
        liste.add(new File("Tests/TestF.class"));
        liste.add(new File("Tests/TestV.class"));

        ArrayList<String> in = new ArrayList<>();
        in.add("2");
        in.add("3");

        EvaluateurBoiteNoireJavaSimple a = new EvaluateurBoiteNoireJavaSimple(liste, in);

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

