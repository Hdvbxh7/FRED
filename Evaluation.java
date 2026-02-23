import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.GregorianCalendar;
import java.util.Objects;

/**
 * Classe d'évaluation des projets, cette classe 
 * s'ocuppe de parcourrir le dossier et de lancer
 * les tests
 */
public class Evaluation implements Runnable{

    //attributs

    /* url du git de l'éléve */
    String urlEleve;
    /* identifiant de l'éléve */
    String identifiantEleve;
    /* nom du projet à tester sur le git de l'éléve */
    String projetATester;
    /* Si le projet est sous git (true : evaluation doit récupérer le dossier, 
    false : les dossier son déjà au bon endroit) */
    boolean avecGit;

    //constructeurs

    /**
     * constructeur de base d'évaluation
     * @param identifiant, identifiant de l'éléve à tester
     * @param url, url du git de l'éléve à tester
     * @param projet, nom du projet à tester
     * @param Git, true : le projet utilise Git, false : il ne l'utilise pas
     */
    Evaluation(String identifiant,String url,boolean Git){
        urlEleve = url;
        identifiantEleve = identifiant;
        projetATester = Scenario.projet;
        avecGit = Git;
    }

    //méthodes 

    /**
     * Copie le fichier origine à l'emplacement
     * du fichier destination
     * @param origine, fichier d'origine à copier
     * @param destination, fichier de destination sur lequel copier les données
     * @throws IOException
     */
    private void copyPaste(File origine,File destination) throws IOException{
        //création des canaux de donnée
        InputStream origineStream = new FileInputStream(origine);
        PrintStream destinationPrintStream = new PrintStream(destination);

        //verification de l'existence et création sinon
        if(!destination.exists()){
            destination.createNewFile();
        }

        //récupération des données du fichier origine
        byte[] buff = origineStream.readAllBytes();

        //écriture dans le fichier destination
        destinationPrintStream.write(buff);

        //fermeture des canaux de données
        origineStream.close();
        destinationPrintStream.close();
    }

    /**
     * ajoute un test dans le dossier Git avant qu'il soit push
     * @param dossier, dossier de l'éléve
     */
    private File ajoutTest(File dossierGit){
        try {
            //dossier dans lequel les évaluations seront copiés
            File dossierResultat = new File(dossierGit.getCanonicalPath()+projetATester+"/evaluation");

            //fichier qui contient les informations d'évaluations
            File resultatCalcule = new File("resultats/"+identifiantEleve+"/evaluation_"+identifiantEleve+".txt");

            //fichier de destination des tests
            File fichierResultat = new File(dossierResultat.getCanonicalPath()+"/evaluation.txt");

            //Si le dossier de resultat existe déjà dans le dossier Git le détruit
            if(dossierResultat.exists()){
                AppelGit.destroyFile(dossierResultat);
            }
            //(re)créer le dossier resultat dans le Repo local
            dossierResultat.mkdirs();

            //copie le fichier de test contenu dans dossier dans resultat
            copyPaste(resultatCalcule, fichierResultat);

            return dossierResultat;

        //récupére les différentes erreur d'écriture de fichier
        } catch (IOException e) {
            System.out.println("erreur d'écriture en ajoutant les résultats");
            return null;
        }
    }

    /**
     * Mets à jour la date du dernier test
     * @throws IOException, renvoi une erreur si le fichier n'arrive pas à écrire dans dateDernierTest.txt
     */
    private void majDateDernierTest() throws IOException{
        //fichier qui contient la date du dernier test
        File dateDernierTest = new File("resultats/"+identifiantEleve+"/dateDernierTest.txt");

        //créer le fichier s'il n'existe pas
        if (!dateDernierTest.exists()) {
            dateDernierTest.createNewFile();
        } else{
            dateDernierTest.delete();
            dateDernierTest.createNewFile();
        }

        //flux d'écriture du fichier
        FileWriter dateDernierTestWriter = new FileWriter(dateDernierTest);

        //écriture de la date à la milliseconde près dans dateDernierTest.txt
        dateDernierTestWriter.write(Long.toString(new GregorianCalendar().getTimeInMillis()));

        //fermeture du canal
        dateDernierTestWriter.close();

    }

    /**
     * Récupére la date du dernier Test Réalisé
     * @return la date du dernier Test réalisé
     * @throws IOException erreur dans la lecture du fichier
     */
    private long dateDernierTest() throws IOException{
        try {
            //fichier qui contient la date de dernier commit
            File fichierDateDernierTest = new File("resultats/"+identifiantEleve+"/dateDernierTest.txt");

            //ouverture des canaux de données
            InputStream is = new FileInputStream(fichierDateDernierTest);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader buffer = new BufferedReader(isr);
            
            //Lecture de la premiére ligne du fichier
            String line = buffer.readLine();

            //fermeture des canaux
            buffer.close();
            isr.close();
            is.close();

            //Vérification de la valeur retournée
            if(Objects.isNull(Long.getLong(line))){
                //pas de test précédent
                return 0;
            }else{
                //date du dernier test en millisecondes
                return Long.getLong(line);
            }

        } catch (FileNotFoundException e) {
            //pas de test précédent
            return 0;
        } 
    }

    /**
     * lance le programme d'évaluation sur un 
     * éléve
     */
    public void run(){
        if(avecGit){
            try {
                //Utilise les appels git, pour récupérer les fichiers à tester et réecrit le résultat des tests
                //le dossier git de l'éléve
                File dossierGit = new File("studentRepo/"+identifiantEleve);
                //le dossier de résultat de l'éléve
                File resultDoss = new File("resultats/"+identifiantEleve);

                //créer les dossiers s'ils n'existent pas (surtout valable pour le test après clonage)
                if(!dossierGit.exists()){
                    dossierGit.mkdirs();
                }
                if(!resultDoss.exists()){
                    resultDoss.mkdirs();
                }

                //Récupération de la date du dernier test et mise à jour
                long dateDernierTestMillis = dateDernierTest();
                majDateDernierTest();

                //récupération du Git
                AppelGit gitEleve = new AppelGit(urlEleve,dossierGit);

                if(gitEleve.needTesting(dateDernierTestMillis)){
                    //Lancement des tests sur le dossier de l'éléve
                    Scenario.scenario(dossierGit);
                    //pull le dossier de l'éléve
                    gitEleve.maj();
                    if(gitEleve.statut == AppelGit.pullStatut.UP_TO_DATE){
                        //ajoute le test au dossier Git
                        File aPush = ajoutTest(dossierGit);
                        //envoi le résultat du test sur le git
                        gitEleve.push(aPush);
                    }
                    //TODO : relancer les tests si conflit ou maj
                }

            //Annule le test car pas réussi à écrire la date de dernier commit
            } catch (IOException e) {
                System.out.println("erreur dans la création de dateDernierTest");
                e.printStackTrace();
            }
        } else {
            //établit les évaluations sans s'occuper du dossier Git
            System.out.println("TODO : sans Git");
        }
    }
}
