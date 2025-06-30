package org.czu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Server {
    private int id;
    private String name;
    private String password;
    private int permission;
}
