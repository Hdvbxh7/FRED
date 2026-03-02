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
     * Convertit les résultats de l'évaluation en format TAP.
     */
    @Override
    protected void ResultatVersTAP() {
        /* TODO: Conversion vers format TAP (JP c'est ta tête sur l'affiche) */
    }

    /**
     * Exécute tous les tests JUnit des fichiers fournis et stocke les résultats.
     * Les résultats sont capturés dans la variable resultat (String) et 
     * testsResultat (Boolean[]).
     */
    @Override
    public void evaluer() {
        StringBuilder resultBuilder = new StringBuilder();
        List<Boolean> testsResults = new ArrayList<>();
        
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
            
            // Capture de la sortie système pour les logs des tests
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            
            // Redirection temporaire de System.out et System.err
            System.setOut(printStream);
            System.setErr(printStream);
            
            // Exécution des tests pour chaque fichier
            for (File fichier : fichiersTests) {
                String className = obtenirNomClasse(fichier);
                
                try {
                    // Chargement de la classe de test
                    Class<?> testClass = classLoader.loadClass(className);
                    
                    resultBuilder.append("=== Exécution des tests de ")
                                .append(className)
                                .append(" ===\n");
                    
                    // Exécution des tests avec JUnitCore
                    JUnitCore junit = new JUnitCore();
                    Result result = junit.run(testClass);
                    
                    // Collecte des résultats
                    resultBuilder.append("Tests exécutés: ")
                                .append(result.getRunCount())
                                .append("\n");
                    resultBuilder.append("Tests réussis: ")
                                .append(result.getRunCount() 
                                       - result.getFailureCount())
                                .append("\n");
                    resultBuilder.append("Tests échoués: ")
                                .append(result.getFailureCount())
                                .append("\n");
                    resultBuilder.append("Temps d'exécution: ")
                                .append(result.getRunTime())
                                .append(" ms\n");
                    
                    // Ajout du statut global du fichier
                    testsResults.add(result.wasSuccessful());
                    
                    // Détail des échecs
                    if (!result.wasSuccessful()) {
                        resultBuilder.append("\n--- Échecs ---\n");
                        for (Failure failure : result.getFailures()) {
                            resultBuilder.append("Test: ")
                                        .append(failure.getTestHeader())
                                        .append("\n");
                            resultBuilder.append("Message: ")
                                        .append(failure.getMessage())
                                        .append("\n");
                            resultBuilder.append("Trace: ")
                                        .append(failure.getTrace())
                                        .append("\n\n");
                        }
                    }
                    
                    resultBuilder.append("\n");
                    
                } catch (ClassNotFoundException e) {
                    resultBuilder.append("ERREUR: Classe non trouvée - ")
                                .append(className)
                                .append("\n");
                    resultBuilder.append("Message: ")
                                .append(e.getMessage())
                                .append("\n\n");
                    testsResults.add(false);
                } catch (Exception e) {
                    resultBuilder.append("ERREUR lors de l'exécution de ")
                                .append(className)
                                .append(": ")
                                .append(e.getMessage())
                                .append("\n\n");
                    testsResults.add(false);
                }
            }
            
            // Restauration de System.out et System.err
            System.setOut(originalOut);
            System.setErr(originalErr);
            
            // Ajout de la sortie capturée aux résultats
            String capturedOutput = outputStream.toString();
            if (!capturedOutput.isEmpty()) {
                resultBuilder.append("\n=== Sortie des tests ===\n");
                resultBuilder.append(capturedOutput);
            }
            
            // Fermeture du ClassLoader
            classLoader.close();
            
        } catch (Exception e) {
            resultBuilder.append("ERREUR CRITIQUE: ")
                        .append(e.getMessage())
                        .append("\n");
            e.printStackTrace();
        }
        
        // Stockage des résultats dans les attributs hérités
        this.resultat = resultBuilder.toString();
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
