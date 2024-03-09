package rs.raf.service.clas;

import org.springframework.stereotype.Service;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
@Service
public class ClasGeneratorImplementation implements ClasGenerator {
    FileWriter fileWriter = null;
    String newRow = "\n";
    @Override
    public boolean generateClass(String name, HashMap<String, String> fields) {
        File file = new File("src/main/java/rs/raf/entities/"+name+".java");
        if(file.exists())
            return false;
        try {
            fileWriter = new FileWriter(file.getPath());
            fileWriter.write("package rs.raf.entities;");
            fileWriter.write(newRow);
            fileWriter.write(newRow);
            fileWriter.write("import lombok.Getter;");
            fileWriter.write(newRow);
            fileWriter.write("import lombok.NoArgsConstructor;");
            fileWriter.write(newRow);
            fileWriter.write("import lombok.Setter;");
            fileWriter.write(newRow);
            fileWriter.write(newRow);
            fileWriter.write("@Getter");
            fileWriter.write(newRow);
            fileWriter.write("@Setter");
            fileWriter.write(newRow);
            fileWriter.write("@NoArgsConstructor");
            fileWriter.write(newRow);
            fileWriter.write("public class "+name.toLowerCase()+" {");
            fileWriter.write(newRow);
            fields.keySet().forEach((key)->{
                try {
                    fileWriter.write("private "+fields.get(key)+" "+key+";");
                    fileWriter.write(newRow);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            fileWriter.write("}");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }catch (Exception ignore){}
        }
        return compile(file);
    }

    private boolean compile(File file){
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null,null,null,file.getPath());
        File fileSrc = new File(file.getPath().replace(".java",".class"));
        File newFile = new File(fileSrc.getPath().replace("src/main/java","target/classes").replace("src\\main\\java","target\\classes"));
        try {
            Files.move(Paths.get(fileSrc.getPath()),Paths.get(newFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
