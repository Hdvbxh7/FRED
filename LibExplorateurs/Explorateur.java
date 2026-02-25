package LibExplorateurs;
import java.io.File;
import java.util.ArrayList;

/**
 * Classe utiliser pour savoir quel superviseur
 * sera utilisé
 * @see ExplorateurGit ExplorateurGit
 * @see ExplorateurScenario ExplorateurScenario
 * @see ExplorateurSimple ExplorateurSimple
 */
public abstract class Explorateur {

    //methode
    public abstract ArrayList<File> listeDossier();

    public abstract void postprocess();
}
