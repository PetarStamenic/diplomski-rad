package rs.raf.controler.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RequestDTO {
    private String field;
    private String value;
}
