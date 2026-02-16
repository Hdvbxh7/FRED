package LibEvaluateur.EvaluationsStyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

import LibEvaluateur.EvaluateurStyle;

public class CheckStyle extends EvaluateurStyle {

    private String resultat;
    private Boolean[] testsResults;

    //parameters to setup
    private ArrayList<File> FilesToCheck;
    private File CheckstyleToUse;
    private Properties PropertiesToUse;

    //constructors
    CheckStyle(ArrayList<File> Files){
        FilesToCheck = Files;
        CheckstyleToUse = new File("LibEvaluateur/EvaluationsStyle/checkstyle-sans-javadoc.xml");
    }

    CheckStyle(ArrayList<File> Files,File checkstyle){
        FilesToCheck = Files;
        CheckstyleToUse = checkstyle;
    }

    CheckStyle(ArrayList<File> Files,File checkstyle,File properties){
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

    public String getResultat() {
        return resultat;
    }

    public Boolean[] getTestsResults() {
        return testsResults;
    }

    protected void ResultsToTAP(){
    }

    public void run(){
    Checker checker = new Checker();
    checker.setModuleClassLoader(Checker.class.getClassLoader());
    try {
        Configuration config = ConfigurationLoader.loadConfiguration(
            CheckstyleToUse.getAbsolutePath(),
            new PropertiesExpander(PropertiesToUse));
        checker.configure(config);
    } catch (CheckstyleException e) {
            System.out.println("erreur durant la configuration");
            e.printStackTrace();
    }
    int errors =0;
    checker.setHaltOnException(false);
    // logger vers la console
    DefaultLogger logger =
        new DefaultLogger(System.out, com.puppycrawl.tools.checkstyle.api.AutomaticBean.OutputStreamOptions.NONE);

    checker.addListener(logger);
    try {
        errors = checker.process(FilesToCheck);
    } catch (CheckstyleException e) {
        System.out.print("erreur durant le process");
        e.printStackTrace();
    }
    checker.destroy();
    }
}
