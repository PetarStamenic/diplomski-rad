package rs.raf.service.clas;

import rs.raf.controler.dto.RequestDTO;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;

public interface ClasFinder {
    Class findClas(String name);
    List<Field> findAllFieldsFromClass(String name);
    Class getValueFromClassField(String cls,String field);
    Object getClassObjectFromNameAndLinkedHashMap(String name, LinkedHashMap hashMap);
    Object getClassEntityFromNameAndJson(String name, String json);
    boolean doesThisObjectContainHashMap(String name,Object object, List<RequestDTO> requestDTOS);
    boolean doesAnyThisObjectContainHashMap(String name,Object object, List<RequestDTO> requestDTOS);
}
