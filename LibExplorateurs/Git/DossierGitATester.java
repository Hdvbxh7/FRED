package LibExplorateurs.Git;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.GregorianCalendar;
import java.util.Objects;

import LibExplorateurs.ExplorateurGit;
import configuration.Scenario;

public class DossierGitATester implements Runnable{
    
    File dossierEleve;
    String urlEleve;
    String identifiantEleve;
    ExplorateurGit explo;

    /**
     * 
     * @param url
     * @param identifiant
     * @param explorateur
     */
    public DossierGitATester(String url,String identifiant,ExplorateurGit explorateur){
        urlEleve = url;
        identifiantEleve = identifiant;
        explo =explorateur;
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

    public void run(){
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
        long dateDernierTestMillis = 0;

        try {
            //Récupération de la date du dernier test et mise à jour
            dateDernierTestMillis = dateDernierTest();
            majDateDernierTest();

        } catch (Exception e) {
            System.out.println("erreur lors de la mise à jour de la date de test");
            e.printStackTrace();
        }
        
        //récupération du Git
        AppelGit gitEleve = new AppelGit(urlEleve,dossierGit,identifiantEleve);

        //vérifie si le dossier à besoin d'être tester
        if(gitEleve.needTesting(dateDernierTestMillis)){
            try {
                //création du dossier à tester
                File projetATester = new File(dossierGit.getCanonicalPath()+Scenario.projet);

                //ajout à l'explorateur
                explo.ArrayList.lock();
                explo.gitsEleve.add(gitEleve);
                explo.dossiersATester.add(projetATester);
                explo.ArrayList.unlock();
            //erreur de dossiers
            } catch (IOException e) {
                System.out.println("erreur sur la lecture du dossier"+dossierGit.getName());
                e.printStackTrace();
            }
        }
    }
}
