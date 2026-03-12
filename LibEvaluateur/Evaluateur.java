package LibEvaluateur;

import org.tap4j.model.Directive;
import org.tap4j.model.Plan;
import org.tap4j.model.TestResult;
import org.tap4j.model.TestSet;
import org.tap4j.producer.TapProducer;
import org.tap4j.producer.TapProducerFactory;
import org.tap4j.util.DirectiveValues;
import org.tap4j.util.StatusValues;

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

	public Evaluateur setNomEvaluateur(String nomEvaluateur) {
		this.nomEvaluateur = nomEvaluateur;
		return this;
	}
	
	protected void skipResultatVersTAP(String raison) {
		TestSet testIgnore = new TestSet();
		testIgnore.setPlan(new Plan(2));
		TestResult resIgnore =  new TestResult(StatusValues.NOT_OK, 1);
		Directive dirIgnore = new Directive(DirectiveValues.SKIP, raison);
		resIgnore.setDirective(dirIgnore);
		this.testsResultat = new Boolean[1];
		this.testsResultat[0] = false;
		this.resultat = producteur.dump(testIgnore);
	}

	protected abstract void resultatVersTAP(String SortieTest);

	public abstract Evaluateur evaluer() throws Exception;
}
