package rs.raf.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
private String password;
private Long Id;
private String email;
private Integer age;
private String username;
}