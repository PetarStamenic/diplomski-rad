package rs.raf.service.clas;

import rs.raf.controler.dto.RequestDTO;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;

public interface ClasFinder {
    public Class findClas(String name);
    public List<Field> findAllFieldsFromClass(String name);
    public Class getValueFromClassField(String cls,String field);
    public Object getClassObjectFromNameAndLinkedHashMap(String name, LinkedHashMap hashMap);
    public Object getClassEntityFromNameAndJson(String name, String json);
    public boolean doesThisObjectContainHashMap(String name,Object object, List<RequestDTO> requestDTOS);
    public boolean doesAnyThisObjectContainHashMap(String name,Object object, List<RequestDTO> requestDTOS);
}
