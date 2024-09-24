package rs.raf.service.clas;

import org.springframework.stereotype.Service;
import rs.raf.controler.dto.RequestDTO;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

@Service
public class ClasFinderImplementation implements ClasFinder {
    @Override
    public Class findClas(String name) {
        try {
            return Class.forName("rs.raf.entities."+name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Override
    public List<Field> findAllFieldsFromClass(String name) {
        List<Field> classFields;
        Class c = findClas(name);
        if(c == null)
            return null;
        classFields = List.of(c.getDeclaredFields());
        return classFields;
    }

    @Override
    public Class getValueFromClassField(String cls, String field) {
        Class c = findClas(cls);
        if(c == null)
            return null;
        try {
            Field field1 = c.getDeclaredField(field);
            return field1.getType();
        } catch (NoSuchFieldException e) {
            return null;
        }
    }


    @Override
    public Object getClassObjectFromNameAndLinkedHashMap(String name, LinkedHashMap hashMap) {
        Object o;
        try {
            o = findClas(name).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for(Object key: hashMap.keySet()){
            if(!(key instanceof String))
                return null;
            try {
                o.getClass().getDeclaredField((String) key);
            } catch (NoSuchFieldException e) {
                return null;
            }
            try {
                Object value;
                if(getValueFromClassField(name,(String) key) == Long.class) {
                    System.out.println(hashMap.get(key));
                    value = Integer.class.cast(hashMap.get(key)).longValue();
                }else {
                    value = getValueFromClassField(name, (String) key).cast(hashMap.get(key));
                }
                o.getClass().getMethod("set"+((String) key).substring(0,1).toUpperCase()+((String) key).substring(1),value.getClass()).invoke(o,value);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return o;
    }

    @Override
    public Object getClassEntityFromNameAndJson(String name,String json) {
        LinkedHashMap<String,String> hashMap = mapFromString(json);
        Object o;
        try {
            o = findClas(name).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for(String key: hashMap.keySet()){
            try {
                o.getClass().getDeclaredField(key);
            } catch (NoSuchFieldException e) {
                return null;
            }
            try {
                Object value;
                Class type = getValueFromClassField(name,key);
                if(type == Long.class) {
                    value = Long.parseLong(hashMap.get(key));
                }else if(type == Integer.class){
                    value = Integer.valueOf(hashMap.get(key));
                }else if(type == Boolean.class){
                    value = Boolean.valueOf(hashMap.get(key));
                }else if(type == Float.class){
                    value = Float.valueOf(hashMap.get(key));
                }else if(type == BigDecimal.class){
                    value = BigDecimal.valueOf(Long.parseLong(hashMap.get(key)));
                }else {
                    value = hashMap.get(key);
                }
                o.getClass().getMethod("set"+key.substring(0,1).toUpperCase()+key.substring(1),value.getClass()).invoke(o,value);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return o;
    }

    @Override
    public boolean doesThisObjectContainHashMap(String name,Object object,List<RequestDTO> requestDTOS) {
        Class clas = findClas(name);
        Object o = clas.cast(object);
        for(RequestDTO request:requestDTOS){
            try {
                if(!(o.getClass().getDeclaredField(request.getField()).getName().equals(request.getField())))
                    return false;
                if(!(o.getClass().getMethod("get"+request.getField().substring(0,1).toUpperCase()
                        +request.getField().substring(1)).invoke(o).toString().equals(request.getValue())))
                    return false;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean doesAnyThisObjectContainHashMap(String name,Object object, List<RequestDTO> requestDTOS) {
        Class clas = findClas(name);
        Object o = clas.cast(object);
        for(RequestDTO request:requestDTOS){
            try {
                if(!(o.getClass().getDeclaredField(request.getField()).getName().equals(request.getField())))
                    return false;
                if(o.getClass().getMethod("get"+request.getField().substring(0,1).toUpperCase()
                        + request.getField().substring(1)).invoke(o).toString().equals(request.getValue()))
                    return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private LinkedHashMap<String,String> mapFromString(String json){
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        json = json.substring(1,json.length()-1);
        json = json.replace("\r\n","");
        json = json.replace("\n","");
        String[] fv = json.split("\",\"");
        for(String f:fv){
            if(f.startsWith("\""))
                f = f.replaceFirst("\"","");
            if(f.endsWith("\""))
                f = f.substring(0,f.length()-1);
            map.put(f.split("\":\"")[0],f.split("\":\"")[1]);
        }
        return map;
    }
}
