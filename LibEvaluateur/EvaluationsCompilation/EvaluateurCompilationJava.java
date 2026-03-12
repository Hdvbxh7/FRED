package LibEvaluateur.EvaluationsCompilation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.tools.*;
import LibEvaluateur.*;
import org.tap4j.model.TestResult;
import org.tap4j.model.TestSet;
import org.tap4j.util.DirectiveValues;
import org.tap4j.util.StatusValues;
import org.tap4j.model.Directive;
import org.tap4j.model.Plan;

/**
 * Evaluateur permettant de tester la compilation d'un fichier Java
 * à l'aide du compilateur {@code javac}.  
 *
 * <p>Deux tests TAP sont générés :</p>
 * <ul>
 * <li>Test 1 : absence d'erreurs de compilation</li>
 * <li>Test 2 : absence d'avertissements (warnings)</li>
 * </ul>
 */
public class EvaluateurCompilationJava extends EvaluateurCompilation {

    private TestSet ensembleTest;

    /** Liste des fichiers à compiler (le premier est la cible, les autres sont des dépendances). */
    protected List<File> fichiers = new ArrayList<File>();

    /**
     * @param binaire fichier Java à tester
     */
    public EvaluateurCompilationJava(File binaire) { 
        super();
        this.ensembleTest = new TestSet();
        fichiers.add(binaire);
    }

    /**
     * @param binaire fichier Java principal à compiler
     * @param dependences liste de fichiers ou bibliothèques nécessaires à la compilation
     */
    public EvaluateurCompilationJava(File binaire, ArrayList<File> dependences) { 
        super();
        fichiers.add(binaire);
        this.ensembleTest = new TestSet();
        fichiers.addAll(dependences);
    }


    /** Convertit la sortie brute de javac en format TAP. 
     * 
     * @param SortieTest la sortie brute produite par javac 
    */
    protected void resultatVersTAP(String SortieTest) {
        testsResultat = new Boolean[2];
		ensembleTest.setPlan( new Plan(2) );
		TestResult presErr;
		TestResult presAvrt;

		if (derniereLigne(SortieTest).contains("error")){
			presErr = new TestResult( StatusValues.NOT_OK, 1 );
            Directive dirAvrt = new Directive(DirectiveValues.SKIP, "Erreur de compilation.");
            presAvrt = new TestResult( StatusValues.NOT_OK, 2);
            presAvrt.setDirective(dirAvrt);
            testsResultat[0] = false;
            testsResultat[1] = false;
			}
		else if (derniereLigne(SortieTest).contains("warning")) {
			presErr = new TestResult( StatusValues.OK, 1 );
            presErr.setDescription("pas d'erreur de compilation");
	        presAvrt = new TestResult( StatusValues.NOT_OK, 2);
	        testsResultat[0] = true;
	        testsResultat[1] = false;
		} else {
			presErr = new TestResult( StatusValues.OK, 1 );
            presErr.setDescription("pas d'erreur de compilation");
			presAvrt = new TestResult( StatusValues.OK, 2);
            presAvrt.setDescription("pas d'avertissement");

	        testsResultat[0] = true;
	        testsResultat[1] = true;
		}

		ensembleTest.addTestResult( presErr );
		ensembleTest.addTestResult( presAvrt );
		this.resultat = producteur.dump( ensembleTest );
	} 


    /**
     * Lance l'évaluation de la compilation du fichier Java.
     *
     * @throws Exception si une erreur survient lors de l'évaluation
     */
    public Evaluateur evaluer() throws Exception {

        StringWriter sw = new StringWriter();
        BufferedWriter out = new BufferedWriter(sw);

        if (fichiers.size() > 1) {
            String classpath = "";
            for (int i = 1; i < fichiers.size(); i++) {
                if (i > 1) {
                    classpath += File.pathSeparator;
                }
                classpath += fichiers.get(i).getAbsolutePath();
            }
            ourJCompiler(out, fichiers.get(0).getAbsolutePath(), classpath);
        } else {
            ourJCompiler(out, fichiers.get(0).getAbsolutePath(), null);
        }

        out.close();
        
        String resultatsBruts = sw.toString();
        resultatVersTAP(resultatsBruts);
        return this;

    }


    /**
     * Exécute le compilateur Java {@code javac} de manière programmatique.
     *
     * @param out flux dans lequel écrire les résultats du compilateur
     * @param file fichier Java à compiler
     * @param cp classpath à utiliser pour la compilation (peut être {@code null})
     */
    private void ourJCompiler(BufferedWriter out, String file, String cp) {
        ByteArrayOutputStream cout = new ByteArrayOutputStream();
        ByteArrayOutputStream cerr = new ByteArrayOutputStream();
    
        // Create Compiled_Code directory if it doesn't exist
        File compiledDir = new File("Compiled_Code");
        if (!compiledDir.exists()) {
            compiledDir.mkdir();
        }
    
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    
        try {
            if (cp != null) {
                javac.run(null, cout, cerr, "-d", compiledDir.getAbsolutePath(), "-Xlint", "-cp", cp, file);
            } else {
                javac.run(null, cout, cerr, "-d", compiledDir.getAbsolutePath(), "-Xlint", file);
            }
        
            // Determine package structure from source file
            String packageName = "";
            try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("package ")) {
                        // e.g., package BacATest.TP02;
                        packageName = line.substring(8, line.indexOf(";")).trim();
                        break;
                    }
                }
            }
        
            // Extract class name from file
            String className = new File(file).getName().replace(".java", "");
        
            // Build the full path: Compiled_Code + package folders + class name
            File fullPath;
            if (packageName.isEmpty()) {
                fullPath = new File(compiledDir, className);
            } else {
                fullPath = new File(compiledDir, packageName.replace(".", File.separator) + File.separator + className);
            }
        
            // Save the path for execution
            this.dossiercompilé = fullPath.getAbsolutePath();
        
            // Write compilation output
            out.write(cout.toString());
            out.write(cerr.toString());
        
        } catch (IOException e) {
            System.out.println("Erreur de compilation : " + e);
        }
    }
    
    /**
     * Retourne la dernière ligne d'un texte.
     *
     * @param text texte d'entrée
     * @return dernière ligne du texte
     */
    public static String derniereLigne(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        int index = text.lastIndexOf('\n');

        if (index == -1) {
            return text;
        }

        return text.substring(index + 1);
    }


    
}