import javax.tools.JavaCompiler.CompilationTask;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class compiler {
     public static void main(String... arguments) {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        javac.run(null, null, null,"-d","truc", "Segment.java");
        try{
            //aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
            String[] cmd = {"checkstyle", "-c", "checkstyle-sans-javadoc.xml", "compiler.java"};
            Process proc=Runtime.getRuntime().exec(cmd);
            InputStream in = proc.getInputStream();
            BufferedWriter out=new BufferedWriter(new FileWriter("log"));
             
            int c;
            while ((c = in.read()) != -1) {
                out.write((char)c);
            }
             
            in.close();
            out.flush();
            out.close();
         } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
