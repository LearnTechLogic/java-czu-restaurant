package org.czu.controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.czu.RestaurantApplication;

public class RoleController {

    @FXML
    void chlick_customer(ActionEvent event) {
        RestaurantApplication.changePage("/views/table.fxml");

    }

    @FXML
    void chlick_staff(ActionEvent event) {
        RestaurantApplication.changePage("/views/register.fxml");

    }

}
