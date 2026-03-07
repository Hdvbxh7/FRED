package LibEvaluateur.EvaluationsCompilation;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.tools.*;

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
        testsResultat = new Boolean[1];
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
	        presAvrt = new TestResult( StatusValues.NOT_OK, 2);
	        testsResultat[0] = true;
	        testsResultat[1] = false;
		} else {
			presErr = new TestResult( StatusValues.OK, 1 );
			presAvrt = new TestResult( StatusValues.OK, 2);
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
    public void evaluer() throws Exception {

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

    }


    /**
     * Exécute le compilateur Java {@code javac} de manière programmatique.
     *
     * @param out flux dans lequel écrire les résultats du compilateur
     * @param file fichier Java à compiler
     * @param cp classpath à utiliser pour la compilation (peut être {@code null})
     */
    private static void ourJCompiler(BufferedWriter out,String file,String cp){
        ByteArrayOutputStream cout = new ByteArrayOutputStream();
        ByteArrayOutputStream cerr = new ByteArrayOutputStream();

        // Create Tests directory if it doesn't exist
        File testsDir = new File("Tests");
        if (!testsDir.exists()) {
            testsDir.mkdir();
        }

        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        if(cp!=null){
            System.out.println(cp.toString());
            javac.run(null, cout, cerr,"-d","LibEvaluateur/EvaluationsCompilation/Compiled_Code","-Xlint","-cp",cp, file);
        }else{
            javac.run(null, cout, cerr,"-d","LibEvaluateur/EvaluationsCompilation/Compiled_Code","-Xlint", file);
        }

        try{
            out.write(cout.toString());
            out.write(cerr.toString());
        }catch(Exception e){
            System.out.println("erreur"+e);
        }
    }

    
    /**
     * Retourne la dernière ligne d'un texte.
     *
     * @param text texte d'entrée
     * @return dernière ligne du texte
     */
    public static String derniereLigne(String text) {
    	return text.substring(text.lastIndexOf('\n'));
    }
    
}