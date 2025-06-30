package org.czu;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class test {

    @FXML
    private Button hehe;

    @FXML
    void chick(ActionEvent event) {
        System.out.println("click");
        log.info("click");
    }

}
