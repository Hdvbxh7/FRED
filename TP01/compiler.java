import javax.tools.JavaCompiler.CompilationTask;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.InputStream;

import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.launcher.*;
import org.junit.platform.launcher.core.*;
import org.junit.platform.launcher.listeners.*;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class compiler {

    private static void ourJCompiler(BufferedWriter out,String file,String cp){
        ByteArrayOutputStream cout = new ByteArrayOutputStream();
        ByteArrayOutputStream cerr = new ByteArrayOutputStream();
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        if(cp!=null){
            javac.run(null, cout, cerr,"-d","Tests","-Xlint","-classpath",cp, file);
        }else{
            javac.run(null, cout, cerr,"-d","Tests","-Xlint", file);
        }
        try{
            out.write(cout.toString());
            out.write(cerr.toString());
            out.write("--------------------------------------------------------------------------------------------------\n");
        }catch(Exception e){
            System.out.println("erreur"+e);
        }

    }

    private static void commande(String[] cmd,BufferedWriter out){
        try{
            Process proc=Runtime.getRuntime().exec(cmd);
            InputStream in = proc.getInputStream();  
            
            int c;
            while ((c = in.read()) != -1) {
                out.write((char)c);
            }
            out.write("--------------------------------------------------------------------------------------------------\n");
            in.close();
        }catch(Exception e){
            System.out.println("erreur"+e);
        }
    }
     public static void main(String... arguments) {
        try{
            //aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
            String[] RunCheckStyle = {"checkstyle", "-c", "checkstyle-sans-javadoc.xml", "compiler.java"};
            String[] runJunitTest = {"java","-classpath","junit4.jar:.", "PointTest.java"};
            String warning = "Warning.java";
            String noCompile = "NoCompile.java";
            String pointTest = "PointTest.java";
            BufferedWriter out=new BufferedWriter(new FileWriter("log"));   
            ourJCompiler(out,warning,null);   
            ourJCompiler(out,noCompile,null);  
            ourJCompiler(out,pointTest,"junit4.jar:."); 
            FileSelector File = null;
            try {
                File = DiscoverySelectors.selectFile("/home/gutierrez/FRED/FRED/TP01/Tests/PointTest.class");
            } catch (PreconditionViolationException e) {
                e.printStackTrace();
            }
            LauncherDiscoveryRequest request =
            LauncherDiscoveryRequestBuilder.request().selectors(File).build();

            Launcher launcher = LauncherFactory.create();

            TestExecutionListener listener = new SummaryGeneratingListener();
            launcher.registerTestExecutionListeners(listener);

            launcher.execute(request);

            TestExecutionSummary summary =
                ((SummaryGeneratingListener) listener).getSummary();
     
            commande(RunCheckStyle,out);
            out.write("Tests trouvés : " + summary.getTestsFoundCount());
            out.write("Tests réussis : " + summary.getTestsSucceededCount());
            out.write("Tests échoués : " + summary.getTestsFailedCount());
            out.flush();
            out.close();
         } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
