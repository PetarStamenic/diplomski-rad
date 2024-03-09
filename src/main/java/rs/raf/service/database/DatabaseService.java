package rs.raf.service.database;

import rs.raf.controler.dto.RequestDTO;
import rs.raf.controler.dto.UpdateDTO;

import java.util.LinkedHashMap;
import java.util.List;

public interface DatabaseService {

    boolean saveObjectToDatabaseToTable(String database, String name, LinkedHashMap entity);
    List<Object> findObjectByDatabaseAndTableAndFieldNameAndValue(String database,String tableName, List<RequestDTO> requestDTOS);
    List<Object> findObjectByDatabaseAndTableAndFieldNameOrValue(String database,String tableName, List<RequestDTO> requestDTOS);
    List<Object> updateObjectByDatabaseAndTableAndFieldNameAndValueWithOldValueAndNewValue(String database, String tableName, List<RequestDTO> requestDTOS, List<UpdateDTO> updateDTOS);
    List<Object> updateObjectByDatabaseAndTableAndFieldNameOrValueWithOldValueAndNewValue(String database,String tableName, List<RequestDTO> requestDTOS, List<UpdateDTO> updateDTOS);
    List<Object> deleteObjectByDatabaseAndTableAndFieldNameAndValue(String database,String tableName, List<RequestDTO> requestDTOS);
    List<Object> deleteObjectByDatabaseAndTableAndFieldNameOrValue(String database,String tableName, List<RequestDTO> requestDTOS);
    List<Object> listAllFromTable(String database,String tableName);
    List<Object> joinFromTableToTableByFieldFromFirstTableWithFieldFromSecondTable(String joinType,String fromTable,String toTable,String fieldNameFromFirstTable,String fieldNameFromSecondTable);
}
