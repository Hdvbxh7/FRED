package configuration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * un fichier avec des fonctions
 * statiques qui peuvent être utiles
 */
public class Utiles {

      /**
     * Copie le fichier origine à l'emplacement
     * du fichier destination
     * @param origine fichier d'origine à copier
     * @param destination fichier de destination sur lequel copier les données
     * @throws IOException
     */
    public static void copyPaste(File origine,File destination) throws IOException{
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
     * Renvoi le contenu d'un fichier sous forme de String
     * @param file fichier à transformer en string
     * @return le fichier sous forme de String, null si erreur
     */
    public static String content(File file){
        try {
            //ouverture des canaux d'entrée
            InputStream is = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader buffer = new BufferedReader(isr);

            //lis la premiére ligne du fichier
            String line = buffer.readLine();

            //Construit la string final
            StringBuilder builder = new StringBuilder();
        
            //rajoute les lignes à la string 
            while(line != null){
                builder.append(line).append("\n");
                line = buffer.readLine();
            }

            //fermeture des canaux
            buffer.close();
            isr.close();
            is.close();

            //renvoi de la string final
            return builder.toString();

        //erreur de lecture du fichier
        } catch (IOException e) {
            System.out.println("erreur de lecture du fichier");
            return null;
        }
    }

    /**
     * Détruit le fichier/dossier et 
     * tout ce qu'il contient
     * @param file fichier/dossier à détruire
     */
    public static void destroyFile(File file){
        if(file.isDirectory()){
            //suppression du dossier

            //liste les sous fichiers du dossier
            File[] fichierASupprimer = file.listFiles();

            //parcours les fichiers et dossiers et les supprimes
            for(File fichier:fichierASupprimer){
                destroyFile(fichier);
            }

            //supprime le dossier maintenant qu'il est vide
            file.delete();
        } else{
            //suppression du fichier
            file.delete();
        }
    }
}
