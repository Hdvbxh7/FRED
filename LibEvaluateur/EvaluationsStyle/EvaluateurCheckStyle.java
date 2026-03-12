package LibEvaluateur.EvaluationsStyle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Scanner;

import org.tap4j.model.Comment;
import org.tap4j.model.Directive;
import org.tap4j.model.Plan;
import org.tap4j.model.TestResult;
import org.tap4j.model.TestSet;
import org.tap4j.util.DirectiveValues;
import org.tap4j.util.StatusValues;
import LibEvaluateur.*;

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
 * Évaluateur de code basé sur Checkstyle.
 * <p>
 * Cette classe encapsule la configuration et l'exécution d'un
 * <a href="https://checkstyle.org/">checker Checkstyle</a> sur
 * une liste de fichiers Java. Le résultat est converti en format
 * <a href="https://testanything.org/tap-specification.html">TAP</a>
 * pour l'intégration dans la suite d'évaluation de FRED.
 * </p>
 *
 * <p>Quelques références :</p>
 * <ul>
 *   <li>https://puppycrawl.com/checkstyle/</li>
 *   <li>https://checkstyle.org/</li>
 *   <li>https://checkstyle.sourceforge.io/apidocs/com/puppycrawl/tools/checkstyle/package-summary.html</li>
 * </ul>
 *
 * <p><strong>Exemple d'utilisation :</strong></p>
 * <pre><code>
 * List&lt;File&gt; files = List.of(new File("src/Main.java"));
 * EvaluateurCheckStyle eval = new EvaluateurCheckStyle(new ArrayList<>(files));
 * eval.evaluer();
 * System.out.println(eval.getResultat());
 * </code></pre>
 */
public class EvaluateurCheckStyle extends EvaluateurStyle {

    // paramètres de checkstyle

    /** Liste des fichiers à analyser */
    private ArrayList<File> fichiersAVerifier;
    /** Fichier XML contenant les vérifications à exécuter */
    private File checkstyleAUtiliser;
    /** Fichier de propriétés utilisé par la configuration Checkstyle */
    private Properties proprietesAUtiliser;

    //constructeurs

    /**
     * Constructeur par défaut : utilise le fichier de configuration de checkstyle par défaut
     * @param listeFichiers Liste des fichiers à tester
     */
    public EvaluateurCheckStyle(ArrayList<File> listeFichiers){
        fichiersAVerifier = listeFichiers;
        checkstyleAUtiliser = new File("LibEvaluateur/EvaluationsStyle/checkstyle-sans-javadoc.xml");
    }

    /**
     * Constructeur personnalisé : permet de fournir un fichier XML
     * de configuration checkstyle à utiliser pour les vérifications
     * @param listeFichiers Liste des fichiers à tester
     * @param checkstyle Fichier XML de configuration
     */
    public EvaluateurCheckStyle(ArrayList<File> listeFichiers,File checkstyle){
        fichiersAVerifier = listeFichiers;
        checkstyleAUtiliser = checkstyle;
    }

    /**
     * Constructeur personnalisé : permet de fournir un fichier XML de configuration checkstyle
     * et le fichier de propriétés utilisé par cette configuration
     * @param listeFichiers Liste des fichiers à tester
     * @param checkstyle Fichier XML de configuration
     * @param proprietes Fichier contenant les propriétés utilisées dans la configuration XML
     */
    public EvaluateurCheckStyle(ArrayList<File> listeFichiers,File checkstyle,File proprietes){
        proprietesAUtiliser = new Properties();
        fichiersAVerifier = listeFichiers;
        checkstyleAUtiliser = checkstyle;
        try {
            proprietesAUtiliser.load(new FileInputStream(proprietes));
        } catch (FileNotFoundException e){
            System.out.println("fichier de propriété non trouvé");
        } catch (IOException e) {
            System.out.println("le fichier de propriété données n'est pas conforme");
        } 
    }

    //méthodes

    /**
     * Convertit la sortie brute de CheckStyle en format TAP.
     *
     * @param sortieTest la sortie brute produite par CheckStyle
     */
	@Override
	protected void resultatVersTAP(String sortieTest) {
		TestSet ensembleTestGlobal = new TestSet();
		List<String> nomsVerifs;
		try {
			nomsVerifs = parserNomsVerifications(checkstyleAUtiliser);
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
     * Exécute l'analyse Checkstyle sur les fichiers fournis et prépare le
     * résultat TAP.
     * <p>
     * Cette méthode ne lance pas d'exception à l'extérieur : en cas d'erreur
     * (configuration invalide, I/O, etc.) un message est écrit sur
     * {@code System.out} et l'état interne peut indiquer un échec.
     * </p>
     */
    public Evaluateur evaluer(){
        //création du checker
        Checker verifieur = new Checker();
        verifieur.setModuleClassLoader(Checker.class.getClassLoader());

        //création du fichier de configuration
        try {
            Configuration config = null;
            if (proprietesAUtiliser==null) {
                config = ConfigurationLoader.loadConfiguration(
                checkstyleAUtiliser.getAbsolutePath(),
                new PropertiesExpander(new Properties()));
            } else {
                config = ConfigurationLoader.loadConfiguration(
                checkstyleAUtiliser.getAbsolutePath(),
                new PropertiesExpander(proprietesAUtiliser));
            }
            verifieur.configure(config);
        } catch (CheckstyleException e) {
                System.out.println("error during the creation of configuration");
                e.printStackTrace();
        }

        //désactive l'arrêt si exception
        verifieur.setHaltOnException(false);

        // Création du logger qui récupérera les résultats
        ByteArrayOutputStream fluxResultat = new ByteArrayOutputStream();
        DefaultLogger logger =
            new DefaultLogger(fluxResultat, com.puppycrawl.tools.checkstyle.api.AutomaticBean.OutputStreamOptions.CLOSE);
        verifieur.addListener(logger);

        //lancement du test sur les fichiers
        try {
            verifieur.process(fichiersAVerifier);
        } catch (CheckstyleException e) {
            System.out.print("error during the Tests");
            e.printStackTrace();
        }

        //transformation du stream en String dans résultat
        resultat = fluxResultat.toString();

        //on supprime le checker pour que le destroyer ne tourne plus
        verifieur.destroy();

        //transformation du résultat en TAP
        resultatVersTAP(resultat);
        return this;
    }

    /**
     * Fonction utilitaire pour ne conserver que la partie la plus utile d'une ligne d'erreur du retour de checkstyle.
     * 
     * @param String La ligne concernée.
     * @return la ligne raccourcies un maximum.
     */
    private String nettoyageLigneErreur(String line) {
    	for (File fichier : fichiersAVerifier) {
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
