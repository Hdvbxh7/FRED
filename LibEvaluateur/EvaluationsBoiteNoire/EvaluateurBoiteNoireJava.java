package LibEvaluateur.EvaluationsBoiteNoire;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EvaluateurBoiteNoireJava extends EvaluateurBoiteNoire {

    

    public EvaluateurBoiteNoireJava(List<File> fichiersList) {
        super();
        fichiers.addAll(fichiersList);
    }

    public EvaluateurBoiteNoireJava(List<File> fichiersList, List<String> arguments) {
        super();
        fichiers.addAll(fichiersList);
        this.arguments = arguments;
    }

    protected void resultatVersTAP(String SortieTest) {

    }

    public void evaluer() throws Exception {
        if (fichiers.isEmpty()) {
            throw new Exception("No file provided for evaluation");
        }
        
        File fichier = fichiers.get(0);
        String className = fichier.getName().replace(".class", "");
        
        // Build classpath from main file's parent directory and all library files
        StringBuilder classpath = new StringBuilder();
        classpath.append(fichier.getParent());
        
        for (int i = 1; i < fichiers.size(); i++) {
            classpath.append(":");
            classpath.append(fichiers.get(i).getAbsolutePath());
        }
        
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-cp");
        command.add(classpath.toString());
        command.add(className);
        
        if (arguments != null && !arguments.isEmpty()) {
            command.addAll(arguments);
        }
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        Process process = pb.start();
        process.waitFor();




    }

    public static void main(String[] args) {

        ArrayList<File> liste = new ArrayList<File>();
        liste.add(new File("Tests/Test.class"));
        EvaluateurBoiteNoireJavaSimple a = new EvaluateurBoiteNoireJavaSimple(liste);

        System.out.println(a.fichiers.get(0).toString());
    }
    
}

