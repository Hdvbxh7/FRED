package LibEvaluateur.EvaluationsBoiteNoire;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import LibEvaluateur.Evaluateur;

public abstract class EvaluateurBoiteNoire extends Evaluateur {

    
    protected List<File> fichiers = new ArrayList<File>();

    public EvaluateurBoiteNoire() {
        super();
    }

    protected List<String> arguments = new ArrayList<String>();

    public List<String> getArguments() {
        return this.arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    /** Cette liste de booléens permet de savoir si une execution a timeout ou pas */
    protected boolean[] timedOut;

    public boolean[] getTimedOut() {
        return this.timedOut;
    }

    public void setTimedOut(boolean[] timedOut) {
        this.timedOut = timedOut;
    }

    
	protected static List<String> lignesDiffs(String stringTest, String stringTemoin) {
		String[] lignesTest = stringTest.split("\n");
		String[] lignesTemoin = stringTemoin.split("\n");
		List<String> rapportDiff = new ArrayList<String>() ;
		for (int i = 0;i<Integer.min(lignesTest.length,lignesTemoin.length); i++) {
			if (!lignesTest[i].equalsIgnoreCase(lignesTemoin[i])) {
				rapportDiff.add("l " + String.valueOf(i) + " : [resultat] " 
						+ lignesTest[i] + " || [attendu] " + lignesTemoin[i]);
			}
		}
		return rapportDiff;
		
	}
}
