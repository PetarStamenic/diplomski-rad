package rs.raf.service.clas;

import org.springframework.stereotype.Service;
import rs.raf.controler.dto.CreateDatabaseDTO;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;

@Service
public class ClasGeneratorImplementation implements ClasGenerator {
    FileWriter fileWriter = null;
    String newRow = "\n";
    @Override
    public boolean generateClass(String name, List<CreateDatabaseDTO> fields) {
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
            fileWriter.write("import rs.raf.annotations.*;");
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
            fields.forEach((filed)->{
                try {
                    if(filed.getProperty()!= null) {
                        String value = null;
                        if(filed.getProperty().toLowerCase().startsWith("default")&&filed.getProperty().contains("=")){
                            value = filed.getProperty().split("=")[1];
                            filed.setProperty("default");
                        }
                        switch (filed.getProperty().toLowerCase()) {
                            case "id": {
                                fileWriter.write("\t@Id");
                                fileWriter.write(newRow);
                                break;
                            }
                            case "unique": {
                                fileWriter.write("\t@Unique");
                                fileWriter.write(newRow);
                                break;
                            }
                            case "notnull": {
                                fileWriter.write("\t@NotNull");
                                fileWriter.write(newRow);
                                break;
                            }
                            case "default":{
                                if (value != null)
                                    fileWriter.write("\t@Default(value = \""+value+"\")");
                                fileWriter.write(newRow);
                                break;
                            }
                        }
                    }
                    fileWriter.write("\tprivate "+filed.getType()+" "+filed.getName()+";");
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

    private boolean compile(File file) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.err.println("No Java compiler found. Ensure that you're using a JDK and not just a JRE.");
            return false;
        }

        int compilationResult = compiler.run(null, null, null, file.getPath());
        if (compilationResult != 0) {
            System.err.println("Compilation failed with result code: " + compilationResult);
            return false;
        }

        // Ensure the file is a .java file
        if (!file.getName().endsWith(".java")) {
            System.err.println("The provided file is not a .java file.");
            return false;
        }

        // Replace ".java" with ".class" for the compiled file
        File fileSrc = new File(file.getPath().replace(".java", ".class"));

        // Replace src/main/java with target/classes in a cross-platform way
        String srcPath = fileSrc.getPath().replace("src" + File.separator + "main" + File.separator + "java", "target" + File.separator + "classes");

        // Create new target file
        File newFile = new File(srcPath);
        newFile.getParentFile().mkdirs();  // Ensure that the directories are created

        try {
            Files.move(Paths.get(fileSrc.getPath()), Paths.get(newFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to move file: " + e.getMessage());
            return false;
        }

        return true;
    }
}
