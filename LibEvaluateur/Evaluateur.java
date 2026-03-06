package LibEvaluateur;

import org.tap4j.producer.TapProducer;
import org.tap4j.producer.TapProducerFactory;

public abstract class Evaluateur {
	
	protected TapProducer producteur;

	public Evaluateur() {
		this.producteur = TapProducerFactory.makeTap13Producer();
	}
    
	protected String resultat;

	public String getResultat() {
		return resultat;
    }

	protected Boolean[] testsResultat;

	public Boolean[] getTestsResultat() {
		return testsResultat;
    }

	protected String nomEvaluateur;

	public String getNomEvaluateur() {
		return this.nomEvaluateur;
	}

	public void setNomEvaluateur(String nomEvaluateur) {
		this.nomEvaluateur = nomEvaluateur;
	}
	

 protected abstract void resultatVersTAP(String SortieTest);

	public String getNomEvaluateur() {
		return this.nomEvaluateur;
	}

	public void setNomEvaluateur(String nomEvaluateur) {
		this.nomEvaluateur = nomEvaluateur;
	}
	

	protected abstract void resultatVersTAP(String SortieTest);

	public abstract void evaluer() throws Exception;
}
