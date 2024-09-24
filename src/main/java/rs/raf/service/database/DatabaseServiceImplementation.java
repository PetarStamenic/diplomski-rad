package rs.raf.service.database;

import org.springframework.stereotype.Service;
import rs.raf.annotations.Default;
import rs.raf.annotations.Id;
import rs.raf.annotations.NotNull;
import rs.raf.annotations.Unique;
import rs.raf.controler.dto.RequestDTO;
import rs.raf.controler.dto.UpdateDTO;
import rs.raf.service.clas.ClasFinder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class DatabaseServiceImplementation implements DatabaseService{

    ClasFinder clasFinder;
    String newRow = "\n";

    public DatabaseServiceImplementation(ClasFinder clasFinder) {
        this.clasFinder = clasFinder;
    }



    @Override
    public boolean saveObjectToDatabaseToTable(String database, String name, LinkedHashMap entity) throws Exception{
        String json = "";
        String pk = null;
        long max = 0;
        long curr;
        FileWriter fileWriter = null;
        File file;
        try {

            json += "{";
            json += newRow;
            Class table = clasFinder.findClas(name);
            Object o = table.cast(clasFinder.getClassObjectFromNameAndLinkedHashMap(name,entity));
            for(Field field: clasFinder.findAllFieldsFromClass(name)){
                field.setAccessible(true);
                try {
                    String fieldName = field.getName();
                    String fieldValue = null;
                    if (field.get(o) != null)
                        fieldValue = field.get(o).toString();
                    if(field.isAnnotationPresent(Id.class)) {
                        if (fieldValue == null) {
                            if (field.getType() == Integer.class || field.getType() == Long.class
                                    || field.getType() == int.class || field.getType() == long.class) {
                                File theDir = new File("src/main/resources/Databases/" + database + "/" + name);
                                if (!theDir.exists()) {
                                    theDir.mkdirs();
                                }
                                file = new File("src/main/resources/Databases/" + database + "/" + name);
                                if (file.isDirectory()) {
                                    for (File f : Objects.requireNonNull(file.listFiles())) {
                                        curr = Integer.parseInt(f.getName().replace(".txt", ""));
                                        if (curr > max) {
                                            max = curr;
                                        }
                                    }
                                    max++;
                                }
                                fieldValue = String.valueOf(max);
                            }
                        }
                        pk = fieldValue;
                    }


                    if(field.isAnnotationPresent(Default.class)){
                        if(fieldValue == null)
                            fieldValue = field.getAnnotation(Default.class).value();
                    }
                    if(field.isAnnotationPresent(NotNull.class)){
                        if(fieldValue == null)
                            throw new Exception("field "+field.getName()+" must not be null");
                    }

                    if(field.isAnnotationPresent(Unique.class)){
                        List<Object> objects = listAllFromTable(database,name);
                        for(Object object:objects){
                            try {
                                Field f = object.getClass().getDeclaredField(field.getName());
                                f.setAccessible(true);
                                if(f.get(object).equals(field.get(o)))
                                    throw new Exception("filed "+fieldName+" must be unique");
                            } catch (NoSuchFieldException | IllegalAccessException ignore){}
                        }
                    }


                        json += "\"" + fieldName + "\":\"" + fieldValue + "\"," + newRow;
                }catch (IllegalAccessException i) {
                    throw new RuntimeException(i);
                }
            }
                int pos = json.lastIndexOf(',');
            json = json.substring(0,pos);
            json += newRow;
            json += "}";
            if(pk == null)
                throw new Exception("id cannot be empty in non numeric fields");
            File newFile = new File("src/main/resources/Databases/"+database+"/"+name+"/"+pk+".txt");
            if(newFile.exists())
                return false;
            if(newFile.createNewFile()){
                fileWriter = new FileWriter(newFile.getPath());
                fileWriter.write(json);
                fileWriter.close();
                return true;
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            }catch (IOException ignore){}
        }return false;
    }


    @Override
    public List<Object> findObjectByDatabaseAndTableAndFieldNameAndValue(String database,String tableName,
                                                                         List<RequestDTO> requestDTOS) {
        File file;
        List<Object> list = new ArrayList<>();
        try {
            File theDir = new File("src/main/resources/Databases/" + database + "/" + tableName);
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            file = new File("src/main/resources/Databases/" + database + "/" + tableName);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    String content = new Scanner(f).useDelimiter("\\Z").next();
                    Object o = clasFinder.getClassEntityFromNameAndJson(tableName,content);
                    if(clasFinder.doesThisObjectContainHashMap(tableName,o,requestDTOS))
                        list.add(o);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Object> findObjectByDatabaseAndTableAndFieldNameOrValue(String database,String tableName,
                                                                        List<RequestDTO> requestDTOS) {
        File file;
        List<Object> list = new ArrayList<>();
        try {
            File theDir = new File("src/main/resources/Databases/" + database + "/" + tableName);
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            file = new File("src/main/resources/Databases/" + database + "/" + tableName);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    String content = new Scanner(f).useDelimiter("\\Z").next();
                    Object o = clasFinder.getClassEntityFromNameAndJson(tableName,content);
                    if(clasFinder.doesAnyThisObjectContainHashMap(tableName,o,requestDTOS))
                        list.add(o);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }


    @Override
    public List<Object> updateObjectByDatabaseAndTableAndFieldNameAndValueWithOldValueAndNewValue(String database,
                                                                                                  String tableName,
                                                                                                  List<RequestDTO> requestDTOS,
                                                                                                  List<UpdateDTO> updateDTOS) throws Exception{
        List<Object> objects = findObjectByDatabaseAndTableAndFieldNameAndValue(database,tableName,requestDTOS);
        List<Object> updates = new ArrayList<>();
        for(Object o:objects){
            Object object = updateObjectInDatabaseInTable(database,tableName,o,updateDTOS);
            if(object != null)
                updates.add(object);
        }
        return updates;
    }

    @Override
    public List<Object> updateObjectByDatabaseAndTableAndFieldNameOrValueWithOldValueAndNewValue(String database,
                                                                                                 String tableName,
                                                                                                 List<RequestDTO> requestDTOS,
                                                                                                 List<UpdateDTO> updateDTOS) throws Exception{
        List<Object> objects = findObjectByDatabaseAndTableAndFieldNameOrValue(database,tableName,requestDTOS);
        List<Object> updates = new ArrayList<>();
        for(Object o:objects){
            updates.add(updateObjectInDatabaseInTable(database,tableName,o,updateDTOS));
        }
        return updates;
    }

    @Override
    public List<Object> deleteObjectByDatabaseAndTableAndFieldNameAndValue(String database, String tableName,
                                                                           List<RequestDTO> requestDTOS) {
        File file;
        try {
            File theDir = new File("src/main/resources/Databases/" + database + "/" + tableName);
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            file = new File("src/main/resources/Databases/" + database + "/" + tableName);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    Scanner scanner = new Scanner(f);
                    String content = scanner.useDelimiter("\\Z").next();
                    scanner.close();
                    Object o = clasFinder.getClassEntityFromNameAndJson(tableName,content);
                    if(clasFinder.doesThisObjectContainHashMap(tableName,o,requestDTOS))
                        Files.delete(Path.of(f.getAbsolutePath()));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Object> deleteObjectByDatabaseAndTableAndFieldNameOrValue(String database, String tableName,
                                                                          List<RequestDTO> requestDTOS) {
        File file;
        try {
            File theDir = new File("src/main/resources/Databases/" + database + "/" + tableName);
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            file = new File("src/main/resources/Databases/" + database + "/" + tableName);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    Scanner scanner = new Scanner(f);
                    String content = scanner.useDelimiter("\\Z").next();
                    scanner.close();
                    Object o = clasFinder.getClassEntityFromNameAndJson(tableName,content);
                    if(clasFinder.doesAnyThisObjectContainHashMap(tableName,o,requestDTOS))
                        Files.delete(Path.of(f.getAbsolutePath()));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Object> listAllFromTable(String database,String tableName) {
        File file;
        List<Object> list = new ArrayList<>();
        try {
            File theDir = new File("src/main/resources/Databases/" + database + "/" + tableName);
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            file = new File("src/main/resources/Databases/" + database + "/" + tableName);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if(files == null)
                    return null;
                for (File f : files) {
                    String content = new Scanner(f).useDelimiter("\\Z").next();
                    Object o = clasFinder.getClassEntityFromNameAndJson(tableName,content);
                    list.add(o);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }





    public Object updateObjectInDatabaseInTable(String database, String name, Object object,List<UpdateDTO> updates) throws Exception {
        String original = "";
        FileWriter fileWriter = null;
        File file;
        try {
            File theDir = new File("src/main/resources/Databases/"+database+"/"+name);
            if (!theDir.exists()){
                theDir.mkdirs();
            }
            file = new File("src/main/resources/Databases/"+database+"/"+name+"/"+object.getClass()
                    .getDeclaredMethod("getId").invoke(object)+".txt");
            String content = new Scanner(file).useDelimiter("\\Z").next();
            original = content;
            for(UpdateDTO update:updates){
                if(object.getClass().getDeclaredField(update.getField()).isAnnotationPresent(NotNull.class))
                    if(update.getNewValue()==null)
                        throw new Exception("new value cannot be Null");
                if(object.getClass().getDeclaredField(update.getField()).isAnnotationPresent(Unique.class)){

                    List<Object> objects = listAllFromTable(database,name);
                    for(Object obj :objects){
                        try {
                            Field f = obj.getClass().getDeclaredField(update.getField());
                            f.setAccessible(true);
                            if(f.get(obj).toString().equals(update.getNewValue()))
                                throw new Exception("filed "+f.getName()+" must be unique");
                        } catch (NoSuchFieldException | IllegalAccessException ignore){}
                    }


                }
                content = content.replace("\""+update.getField()+"\""+":"+"\""+update.getOldValue()+"\"",
                        "\""+update.getField()+"\""+":"+"\""+update.getNewValue()+"\"");
            }
            fileWriter = new FileWriter(file.getPath());
            fileWriter.write(content);
            fileWriter.close();
            if(original.equals(content))
                return null;
            return clasFinder.getClassEntityFromNameAndJson(name,content);

        }catch (IOException e){
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            }catch (IOException ignore){}
        }return null;
    }


}
