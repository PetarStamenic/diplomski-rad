package rs.raf.controler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.raf.controler.dto.CreateDatabaseDTO;
import rs.raf.controler.dto.RequestDTO;
import rs.raf.controler.dto.UpdateDTO;
import rs.raf.service.clas.ClasFinder;
import rs.raf.service.clas.ClasGenerator;
import rs.raf.service.database.DatabaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
public class DatabaseControler {
    DatabaseService databaseService;
    ClasGenerator clasGenerator;
    ClasFinder clasFinder;

    public DatabaseControler(DatabaseService databaseService, ClasGenerator clasGenerator,ClasFinder clasFinder) {
        this.databaseService = databaseService;
        this.clasGenerator = clasGenerator;
        this.clasFinder = clasFinder;
    }

    @PutMapping("/createtable/{tableName}")
    public ResponseEntity<String > createTable(@PathVariable(value = "tableName")String tableName
            , @RequestBody List<CreateDatabaseDTO> createDatabaseDTOS){
        HashMap<String ,String> fields = new HashMap<>();
        createDatabaseDTOS.forEach(createDatabaseDTO ->
                fields.put(createDatabaseDTO.getName(),createDatabaseDTO.getType()));

        if(clasGenerator.generateClass(tableName,createDatabaseDTOS))
            return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    @PostMapping("/{database}/{table}")
    public ResponseEntity<String > add(@PathVariable(value = "table")String table
            ,@PathVariable(value = "database")String database, @RequestBody LinkedHashMap body){
        try {
            if(databaseService.saveObjectToDatabaseToTable(database,table,body))
                return new ResponseEntity<>(HttpStatus.OK);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }catch (Exception e){
            return new ResponseEntity<String>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{database}/{table}/{requests}")
    public ResponseEntity<Object> find(@PathVariable(value = "table")String table
            ,@PathVariable(value = "database")String database,@PathVariable(value = "requests")String requests){
        List<RequestDTO> requestDTOS = convertRequestsToList(requests);
        if(requestDTOS == null || requestDTOS.isEmpty())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if(requests.contains("&")){
            Object o = databaseService.findObjectByDatabaseAndTableAndFieldNameAndValue(database,table, convertRequestsToList(requests));
            return new ResponseEntity<>(o,HttpStatus.OK);
        }
        Object o = databaseService.findObjectByDatabaseAndTableAndFieldNameOrValue(database,table, convertRequestsToList(requests));
        return new ResponseEntity<>(o,HttpStatus.OK);
    }
    @GetMapping("/{database}/{table}")
    public ResponseEntity<Object> findAll(@PathVariable(value = "table")String table
            ,@PathVariable(value = "database")String database){
        Object o = databaseService.listAllFromTable(database,table);
        return new ResponseEntity<>(o,HttpStatus.OK);
    }

    @PutMapping("/{database}/{table}/{requests}/{updates}")
    public ResponseEntity<Object> update(@PathVariable(value = "table")String table
            ,@PathVariable(value = "database")String database
            ,@PathVariable(value = "requests")String requests
            ,@PathVariable(value = "updates")String updates){
        List<RequestDTO> requestDTOS = convertRequestsToList(requests);
        List<UpdateDTO> updateDTOS = convertUpdatesToList(updates);
        if(requestDTOS == null || requestDTOS.isEmpty())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        try {
            if(requests.contains("&")){
                Object o = databaseService.updateObjectByDatabaseAndTableAndFieldNameAndValueWithOldValueAndNewValue(database,table, requestDTOS,updateDTOS);
                return new ResponseEntity<>(o,HttpStatus.OK);
            }
            Object o = databaseService.updateObjectByDatabaseAndTableAndFieldNameOrValueWithOldValueAndNewValue(database,table, requestDTOS,updateDTOS);
            return new ResponseEntity<>(o,HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<Object>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{database}/{table}/{requests}")
    public ResponseEntity<Object> delete(@PathVariable(value = "table")String table
            ,@PathVariable(value = "database")String database
            ,@PathVariable(value = "requests")String requests){

        List<RequestDTO> requestDTOS = convertRequestsToList(requests);
        if(requestDTOS == null || requestDTOS.isEmpty())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if(requests.contains("&")){
            Object o = databaseService.deleteObjectByDatabaseAndTableAndFieldNameAndValue(database,table, convertRequestsToList(requests));
            return new ResponseEntity<>(o,HttpStatus.OK);
        }
        Object o = databaseService.deleteObjectByDatabaseAndTableAndFieldNameOrValue(database,table, convertRequestsToList(requests));
        return new ResponseEntity<>(o,HttpStatus.OK);
    }


    private List<RequestDTO> convertRequestsToList(String requests){
        List<RequestDTO> requestDTOS = new ArrayList<>();
        try {
            String[] hm = new String[0];
            if(requests.contains("&")){
                hm = requests.split("&");
            }else if(requests.contains("$")){
                hm = requests.split("\\$");
            }else if(requests.contains("=")){
                requestDTOS.add(new RequestDTO(requests.split("=")[0],requests.split("=")[1]));
            }
            for(String st:hm){
                requestDTOS.add(new RequestDTO(st.split("=")[0],st.split("=")[1]));
            }
            return requestDTOS;
        }catch (Exception e){
            return null;
        }
    }

    private List<UpdateDTO> convertUpdatesToList(String updates){
        List<UpdateDTO> updateDTOS = new ArrayList<>();
        try {
            String[] hm = new String[0];
            if(updates.contains("&")){
                hm = updates.split("&");
            }else if(updates.contains("$")){
                hm = updates.split("\\$");
            }else if(updates.contains("=") && updates.contains("<>")){
                updateDTOS.add(new UpdateDTO(updates.split("=")[0]
                        ,updates.split("=")[1].split("<>")[0]
                        ,updates.split("=")[1].split("<>")[1]));
            }
            for(String st:hm){
                updateDTOS.add(new UpdateDTO(st.split("=")[0]
                        ,st.split("=")[1].split("<>")[0]
                        ,st.split("=")[1].split("<>")[1]));
            }
            return updateDTOS;
        }catch (Exception e){
            return null;
        }
    }
}

