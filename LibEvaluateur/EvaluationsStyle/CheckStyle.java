package LibEvaluateur.EvaluationsStyle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import java.util.Scanner;

import org.tap4j.model.Comment;
import org.tap4j.model.Directive;
import org.tap4j.model.Plan;
import org.tap4j.model.TestResult;
import org.tap4j.model.TestSet;
import org.tap4j.util.DirectiveValues;
import org.tap4j.util.StatusValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

/**
 * This class is the Checkstyle module
 * TODO
 */
/**
 * 
 */
public class CheckStyle extends EvaluateurStyle {



    // parameters to set up
    /** List of files to analyze */
    private ArrayList<File> filesToCheck;
    /** The XML file that contains the checks to run */
    private File checkstyleToUse;
    /** The properties file used by the Checkstyle configuration */
    private Properties propertiesToUse;

    //constructors

    /**
     * Default constructor, uses the default checkstyle file
     * @param Files List of files to test
     */
    public CheckStyle(ArrayList<File> Files){
        filesToCheck = Files;
        checkstyleToUse = new File("LibEvaluateur/EvaluationsStyle/checkstyle-sans-javadoc.xml");
    }

    /**
     * Custom constructor: allows providing a custom checkstyle XML file
     * to be used for the checks
     * @param Files List of files to test
     * @param checkstyle XML configuration file
     */
    public CheckStyle(ArrayList<File> Files,File checkstyle){
        filesToCheck = Files;
        checkstyleToUse = checkstyle;
    }

    /**
     * Custom constructor: allows providing a custom checkstyle XML file
     * and the properties file used by that configuration
     * @param Files List of files to test
     * @param checkstyle XML configuration file
     * @param properties File containing the properties used in the XML configuration
     */
    public CheckStyle(ArrayList<File> Files,File checkstyle,File properties){
        propertiesToUse = new Properties();
        filesToCheck = Files;
        checkstyleToUse = checkstyle;
        try {
            propertiesToUse.load(new FileInputStream(properties));
        } catch (FileNotFoundException e){
            System.out.println("fichier de propriété non trouvé");
        } catch (IOException e) {
            System.out.println("le fichier de propriété données n'est pas conforme");
        } 
    }

    /**
     * Convertit la sortie brute de CheckStyle en format TAP.
     *
     * @param sortieTest la sortie brute produite par Valgrind
     */
	@Override
	protected void resultatVersTAP(String sortieTest) {
		TestSet ensembleTestGlobal = new TestSet();
		List<String> nomsVerifs;
		try {
			nomsVerifs = parserNomsVerifications(checkstyleToUse);
		} catch (FileNotFoundException e){
			ensembleTestGlobal.setPlan( new Plan(1) );
        	TestResult resultat = new TestResult(StatusValues.NOT_OK, 1);
        	Directive exFileNotFound = new Directive(DirectiveValues.SKIP,
        			"Erreur, fichier de configuration de checkstyle introuvable");
        	resultat.setDirective(exFileNotFound);
            this.testsResultat = new Boolean[1];
            testsResultat[0] = false;
            ensembleTestGlobal.addTestResult(resultat);
			this.resultat = producteur.dump(ensembleTestGlobal);
			return;
		}
		Map<String,List<String>> erreursMap = new HashMap<String,List<String>>(nomsVerifs.size());
		for (String verif : nomsVerifs) {
			erreursMap.put(verif, new ArrayList<String>());
		}
		for (String line : sortieTest.split("\n")) {
			line = line.strip();
			for (String verif : nomsVerifs) {
				if (line.endsWith("["+verif+"]")) {
					erreursMap.get(verif).add(nettoyageLigneErreur(line.strip()));
				}
			}
		}
		this.testsResultat = new Boolean[nomsVerifs.size()];
		ensembleTestGlobal.setPlan( new Plan(nomsVerifs.size()) );
		for (String verif : nomsVerifs) {
			testsResultat[nomsVerifs.indexOf(verif)] = erreursMap.get(verif).isEmpty();
			TestResult resultatVerif = new TestResult(StatusValues.OK, nomsVerifs.indexOf(verif)+1);
			resultatVerif.setDescription(verif);
			if (!erreursMap.get(verif).isEmpty()) {
				resultatVerif.setStatus(StatusValues.NOT_OK);
				TestSet erreursVerif = new TestSet();
				erreursVerif.setPlan(new Plan(erreursMap.get(verif).size()));
				for (String ligneErr : erreursMap.get(verif)) {
					TestResult errResult = new TestResult(StatusValues.NOT_OK, erreursMap.get(verif).indexOf(ligneErr)+1);
					errResult.setDescription(ligneErr);
					erreursVerif.addTestResult(errResult);
				}
				resultatVerif.setSubtest(erreursVerif);
			}
			ensembleTestGlobal.addTestResult(resultatVerif);
		}
		this.resultat = producteur.dump(ensembleTestGlobal);
	}
	
	
    

	/**
     * Launch the test and add the results to testResults
     * and resultat
     */
    public void evaluer(){

        //creating checker
        Checker checker = new Checker();
        checker.setModuleClassLoader(Checker.class.getClassLoader());

        //creating configuration file
        try {
            Configuration config = null;
            if (propertiesToUse==null) {
                config = ConfigurationLoader.loadConfiguration(
                checkstyleToUse.getAbsolutePath(),
                new PropertiesExpander(new Properties()));
            } else {
                config = ConfigurationLoader.loadConfiguration(
                checkstyleToUse.getAbsolutePath(),
                new PropertiesExpander(propertiesToUse));
            }
            checker.configure(config);
        } catch (CheckstyleException e) {
                System.out.println("error during the creation of configuration");
                e.printStackTrace();
        }

        //disable the halt on exception
        checker.setHaltOnException(false);

        // logger vers le fichier logCheckstyle.txt
        File checkstyleLog = new File("logCheckstyle.txt");
        try {
            PrintStream resultPrintStream = new PrintStream(checkstyleLog);
            DefaultLogger logger =
                new DefaultLogger(resultPrintStream, com.puppycrawl.tools.checkstyle.api.AutomaticBean.OutputStreamOptions.CLOSE);
            checker.addListener(logger);
        } catch (FileNotFoundException e) {
           System.out.println("error during log creation");
        }

        //launching test
        try {
            checker.process(filesToCheck);
        } catch (CheckstyleException e) {
            System.out.print("error during the Tests");
            e.printStackTrace();
        }

        //écriture de logCheckstyle.txt
        try {
            InputStream is = new FileInputStream(checkstyleLog);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader buffer = new BufferedReader(isr);

            String line = buffer.readLine();
            StringBuilder builder = new StringBuilder();
        
            while(line != null){
                builder.append(line).append("\n");
                line = buffer.readLine();
            }
            resultat = builder.toString();
            buffer.close();
            isr.close();
            is.close();
        } catch (IOException e) {
            System.out.println("error during the writing of log in resultat");
        }

        //destroying the checker so that the listener won't remain
        checker.destroy();

        resultatVersTAP(resultat);
    }

    /**
     * Fonction utilitaire pour ne conserver que la partie la plus utile d'une ligne d'erreur du retour de checkstyle.
     * 
     * @param String La ligne concernée.
     * @return la ligne raccourcies un maximum.
     */
    private String nettoyageLigneErreur(String line) {
    	for (File fichier : filesToCheck) {
    		if (line.contains(fichier.getName())) {
    			return line.substring(line.indexOf(fichier.getName()), line.lastIndexOf("["));
    		}
    	}
		return line.substring(line.indexOf("]"), line.lastIndexOf("["));
	}
    
    /**
     * Fonction utilitaire lire le fichier xml de configuration et trouver le nom de toutes les différentes verifications.
     * 
     * @param File Le fichier xml de configuration.
     * @return Une liste de string composée du nom de chaques verifications.
     */
    private static List<String> parserNomsVerifications(File xmlConfiguration) throws FileNotFoundException{
    	Scanner myReader = new Scanner(xmlConfiguration);
    	List<String> nomsVerifs = new ArrayList<String>();
    	while (myReader.hasNextLine()) {
    		String data = myReader.nextLine();
    		if (data.strip().startsWith("<module name=\"")) {
    			String nomModule = parseNomModule(data);
    			if (!(nomModule.isBlank()
    					|| nomModule.equalsIgnoreCase("TreeWalker")
    					|| nomModule.equalsIgnoreCase("Checker"))) {
    				nomsVerifs.add(nomModule);
    			}
    		}
    	}
    	myReader.close();
    	return(nomsVerifs);
    }
    
    /**
     * fonction utilitaire pour raccourcir le code de parserNomsVerifications.
     * 
     * @param String une ligne d'un xml de configuration de checkstyle de la forme "<module name=\"[nom]\">.
     * @return la string [nom]
     */
    private static String parseNomModule(String ligne) {
    	return ligne.substring(ligne.indexOf("\"")+1, ligne.lastIndexOf("\""));
    }

}
