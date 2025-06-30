package org.czu.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.czu.RestaurantApplication;
import org.czu.mapper.ShopMapper;

public class TableController {
    public static int tableId;
    public static int orderId;

    @FXML
    void B1(ActionEvent event) {
        setTableId(1);
    }

    @FXML
    void B10(ActionEvent event) {
        setTableId(10);
    }

    @FXML
    void B2(ActionEvent event) {
        setTableId(2);
    }

    @FXML
    void B3(ActionEvent event) {
        setTableId(3);
    }

    @FXML
    void B4(ActionEvent event) {
        setTableId(4);
    }

    @FXML
    void B5(ActionEvent event) {
        setTableId(5);
    }

    @FXML
    void B6(ActionEvent event) {
        setTableId(6);
    }

    @FXML
    void B7(ActionEvent event) {
        setTableId(7);
    }

    @FXML
    void B8(ActionEvent event) {
        setTableId(8);
    }

    @FXML
    void B9(ActionEvent event) {
        setTableId(9);
    }

    private void setTableId(int tableId) {
        this.tableId = tableId;
        orderId = ShopMapper.getMaxOrderId(tableId);

        RestaurantApplication.changePage("/views/shop.fxml");
    }

}
