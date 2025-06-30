package org.czu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.czu.controller.RegisterController;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dish {
    private int id;
    private String name;
    private double price;
    private String describe;
    private int server = Integer.parseInt(RegisterController.account);
    private int state = 1;
    private String image;
    private int classification;
    private int quantity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int tableId;

}