package LibEvaluateur.EvaluationsUnitaire;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.tap4j.model.Comment;
import org.tap4j.model.Directive;
import org.tap4j.model.Plan;
import org.tap4j.model.TestResult;
import org.tap4j.model.TestSet;
import org.tap4j.util.DirectiveValues;
import org.tap4j.util.StatusValues;

/* 
    NOTE:
        Références (merci Tom!)
        https://docs.junit.org/6.0.3/appendix.html#dependency-metadata-junit-bom
        https://github.com/junit-team/junit-examples/tree/r6.0.3/junit-jupiter-starter-maven
        https://docs.junit.org/6.0.3/overview.html
        https://docs.junit.org/6.0.3/api/org.junit.jupiter/module-summary.html
        https://docs.junit.org/6.0.3/api/

*/

/**
 * Évaluateur de tests unitaires JUnit.
 * Cette classe permet d'exécuter des tests JUnit et de collecter les résultats.
 * 
 * @author Laérian Bontinck
 * @version 1.0
 */
public class EvaluateurJUnit extends EvaluateurUnitaire {

    /** Liste des fichiers de tests à évaluer (.java ou .class) */
    private ArrayList<File> fichiersTests;
    
    /** Chemins des classes pour le chargement dynamique */
    private ArrayList<String> classpaths;

    /**
     * Constructeur de l'évaluateur JUnit.
     * 
     * @param fichiers Liste des fichiers de tests à exécuter 
     *                 (fichiers .class ou .java)
     */
    public EvaluateurJUnit(ArrayList<File> fichiers) {
        this.fichiersTests = fichiers;
        this.classpaths = new ArrayList<>();
    }

    /**
     * Constructeur de l'évaluateur JUnit avec chemins de classe additionnels.
     * 
     * @param fichiers Liste des fichiers de tests à exécuter
     * @param classpaths Chemins additionnels pour le chargement des classes
     */
    public EvaluateurJUnit(ArrayList<File> fichiers, ArrayList<String> classpaths) {
        this.fichiersTests = fichiers;
        this.classpaths = classpaths;
    }

    /**
     * Convertit les résultats de l'évaluation en format TAP. Dans cette classe, la sortie générée
     * est déja au format tap. Cette fonction est donc vide.
     */
    @Override
    protected void resultatVersTAP(String sortieTest) {
        this.resultat = sortieTest;
    }

    /**
     * Exécute tous les tests JUnit des fichiers fournis et stocke les résultats.
     * Les résultats sont capturés dans la variable resultat (String) et 
     * testsResultat (Boolean[]).
     */
    @Override
    public void evaluer() {
        List<Boolean> testsResults = new ArrayList<>();
        TestSet ensembleTestGlobal = new TestSet();

        try {
        	
            // Préparation du ClassLoader pour charger les classes de test
            List<URL> urls = new ArrayList<>();
            
            // Ajout des chemins des fichiers de test
            for (File fichier : fichiersTests) {
                if (fichier.getName().endsWith(".class")) {
                    // Ajout du répertoire parent pour les fichiers .class
                    urls.add(fichier.getParentFile().toURI().toURL()); // Pourquoi je dois passer par URI pour une URL c'est super verbose :(
                } else if (fichier.getName().endsWith(".java")) {
                    // Pour les fichiers .java, on suppose que les .class 
                    // sont dans le même répertoire ou dans ../bin
                    urls.add(fichier.getParentFile().toURI().toURL());
                    File binDir = new File(fichier.getParentFile(), "../bin");
                    if (binDir.exists()) {
                        urls.add(binDir.toURI().toURL());
                    }
                }
            }
            
            // Ajout des chemins additionnels fournis
            for (String classpath : classpaths) {
                urls.add(new File(classpath).toURI().toURL());
            }
            
            // Création du ClassLoader avec les URLs collectées
            URLClassLoader classLoader = new URLClassLoader(
                urls.toArray(new URL[0]),
                Thread.currentThread().getContextClassLoader()
            );
            
            // Exécution des tests pour chaque fichier
            ensembleTestGlobal.setPlan( new Plan(fichiersTests.size()) );
            ensembleTestGlobal.addComment(new Comment("Resultats par fichier de tests : "));

            for (File fichier : fichiersTests) {
            	TestResult resultatFichier ;
                String className = obtenirNomClasse(fichier);
                
                try {
                    // Chargement de la classe de test
                    Class<?> testClass = classLoader.loadClass(className);
                    
                    // Exécution des tests avec JUnitCore
                    JUnitCore junit = new JUnitCore();
                    Result result = junit.run(testClass);
                    
                    // Ajout du statut global du fichier
                    testsResults.add(result.wasSuccessful());
                    
                    // Détail des échecs (Uniquement des echecs, impossible de récuperer les resultats des tests réussis...)
                    if (!result.wasSuccessful()) {
                    	resultatFichier = new TestResult(StatusValues.NOT_OK, fichiersTests.indexOf(fichier));
                    	resultatFichier.setDescription(className.concat(" Echecs des tests suivants."));
                    	List<Failure> echecs = result.getFailures();
                    	TestSet echecsFichier = new TestSet();
                    	echecsFichier.setPlan(new Plan(echecs.size()));
                        for (int i=0;i>=echecs.size();i++) {
                        	TestResult echec = new TestResult(StatusValues.NOT_OK, i);
                        	echec.setDescription("Test: ".concat(echecs.get(i).getTestHeader()));
                        	Comment message = new Comment("Message: "+ echecs.get(i).getMessage() + ".", false);
                        	echec.addComment(message);
                        	Comment trace = new Comment("Trace: "+ echecs.get(i).getTrace() + ".", false);
                        	echec.addComment(trace);
                        	echecsFichier.addTestResult(echec);
                        }
                        resultatFichier.setSubtest(echecsFichier);
                    } else {
                    	resultatFichier = new TestResult(StatusValues.OK, fichiersTests.indexOf(fichier));
                    	resultatFichier.setDescription(" Tous les tests de " + className + " ont réussis.");
                    	Comment descrSucces = new Comment(
                    			"Tests exécutés: ".concat(String.valueOf(result.getRunCount()))
                    			.concat(". Temps d'exécution: ")
                    			.concat(String.valueOf(result.getRunTime()))
                    			.concat(" ms.")
                    			);
                    	resultatFichier.addComment(descrSucces);
                    	
                    }
                    resultatFichier.setDescription(className);
                    ensembleTestGlobal.addTestResult(resultatFichier);
                    
                } catch (ClassNotFoundException e) {
                	resultatFichier = new TestResult(StatusValues.NOT_OK, fichiersTests.indexOf(fichier));
                	Directive exClassAbs = new Directive(DirectiveValues.SKIP,
                			"Exception rencontrée :" + className + " non trouvée.");
                	resultatFichier.setDirective(exClassAbs);
                    testsResults.add(false);
                    ensembleTestGlobal.addTestResult(resultatFichier);
                } catch (Exception e) {
                	resultatFichier = new TestResult(StatusValues.NOT_OK, fichiersTests.indexOf(fichier));
                	Directive exAutre = new Directive(DirectiveValues.SKIP,
                			"Exception rencontrée lors de l'execution de :" + className);
                	resultatFichier.setDirective(exAutre);
                	Comment trace = new Comment ("Message d'erreur :" + e.getMessage());
                	resultatFichier.addComment(trace);
                    testsResults.add(false);
                    ensembleTestGlobal.addTestResult(resultatFichier);
                    
                }
            }
            

            
            // Fermeture du ClassLoader
            classLoader.close();
            
        } catch (Exception e) {
        	ensembleTestGlobal.setPlan( new Plan(1) );
            
        	TestResult resultat = new TestResult(StatusValues.NOT_OK, 1);
        	Directive exAutre = new Directive(DirectiveValues.SKIP,
        			"Erreur critique rencontrée lors de l'execution. pas de resultats");
        	resultat.setDirective(exAutre);
        	Comment trace = new Comment ("Message d'erreur :" + e.getMessage());
        	resultat.addComment(trace);
            testsResults.add(false);
            ensembleTestGlobal.addTestResult(resultat);
        }
        
        // Stockage des résultats dans les attributs hérités
        resultatVersTAP(producteur.dump(ensembleTestGlobal));
        this.testsResultat = testsResults.toArray(new Boolean[0]);
    }
    
    /**
     * Extrait le nom de la classe à partir d'un fichier .java ou .class.
     * 
     * @param fichier Le fichier dont on veut extraire le nom de classe
     * @return Le nom de la classe (sans extension)
     */
    private String obtenirNomClasse(File fichier) {
        String nomFichier = fichier.getName();
        
        // Suppression de l'extension .java ou .class
        if (nomFichier.endsWith(".java")) {
            return nomFichier.substring(0, nomFichier.length() - 5);
        } else if (nomFichier.endsWith(".class")) {
            return nomFichier.substring(0, nomFichier.length() - 6);
        }
        
        return nomFichier;
    }
    
}
