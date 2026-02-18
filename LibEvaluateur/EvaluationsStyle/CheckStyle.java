package LibEvaluateur.EvaluationsStyle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
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
public class CheckStyle extends EvaluateurStyle {

    /** result of all tests */
    private String resultat;

    /** array of results for each test type (true=passed, false=failed) */
    private Boolean[] testsResults;

    // parameters to set up
    /** List of files to analyze */
    private ArrayList<File> FilesToCheck;
    /** The XML file that contains the checks to run */
    private File CheckstyleToUse;
    /** The properties file used by the Checkstyle configuration */
    private Properties PropertiesToUse;

    //constructors

    /**
     * Default constructor, uses the default checkstyle file
     * @param Files List of files to test
     */
    public CheckStyle(ArrayList<File> Files){
        FilesToCheck = Files;
        CheckstyleToUse = new File("LibEvaluateur/EvaluationsStyle/checkstyle-sans-javadoc.xml");
    }

    /**
     * Custom constructor: allows providing a custom checkstyle XML file
     * to be used for the checks
     * @param Files List of files to test
     * @param checkstyle XML configuration file
     */
    public CheckStyle(ArrayList<File> Files,File checkstyle){
        FilesToCheck = Files;
        CheckstyleToUse = checkstyle;
    }

    /**
     * Custom constructor: allows providing a custom checkstyle XML file
     * and the properties file used by that configuration
     * @param Files List of files to test
     * @param checkstyle XML configuration file
     * @param properties File containing the properties used in the XML configuration
     */
    public CheckStyle(ArrayList<File> Files,File checkstyle,File properties){
        PropertiesToUse = new Properties();
        FilesToCheck = Files;
        CheckstyleToUse = checkstyle;
        try {
            PropertiesToUse.load(new FileInputStream(properties));
        } catch (FileNotFoundException e){
            System.out.println("fichier de propriété non trouvé");
        } catch (IOException e) {
            System.out.println("le fichier de propriété données n'est pas conforme");
        } 
    }

    /**
     * Getter for Resultat
     * @return the resultat attribute
     */
    public String getResultat() {
        return resultat;
    }

    /**
     * Getter for testsResults
     * @return the results Table
     */
    public Boolean[] getTestsResults() {
        return testsResults;
    }

    protected void ResultsToTAP(){
    }

    /**
     * Launch the test and add the results to testResults
     * and resultat
     */
    public void run(){

        //creating checker
        Checker checker = new Checker();
        checker.setModuleClassLoader(Checker.class.getClassLoader());

        //creating configuration file
        try {
            Configuration config = ConfigurationLoader.loadConfiguration(
                CheckstyleToUse.getAbsolutePath(),
                new PropertiesExpander(PropertiesToUse));
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

        //launching test
        try {
            checker.process(FilesToCheck);
        } catch (CheckstyleException e) {
            System.out.print("error during the Tests");
            e.printStackTrace();
        }

        //destroying the checker so that the listener won't remain
        checker.destroy();
    }

}
