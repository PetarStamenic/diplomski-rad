package rs.raf.service.clas;

import rs.raf.controler.dto.CreateDatabaseDTO;

import java.util.HashMap;
import java.util.List;

public interface ClasGenerator {
    boolean generateClass(String name, List<CreateDatabaseDTO> fields);
}
