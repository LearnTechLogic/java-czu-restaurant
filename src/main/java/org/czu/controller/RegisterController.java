package org.czu.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import org.czu.RestaurantApplication;
import org.czu.mapper.RegisterMapper;
import javafx.scene.control.TextField;

public class RegisterController {

    public static String account =  "999999";

    @FXML
    private TextField accountField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label registerResult;

    @FXML
    void register(ActionEvent event) {
        String id = accountField.getText();
        String password = passwordField.getText();
        if (RegisterMapper.verify(id, password)) {
            System.out.println("登录成功");
            account = id;
            RestaurantApplication.changePage("/views/mange.fxml");
        }else {
            System.out.println("登录失败");
            accountField.setText("");
            passwordField.setText("");
            registerResult.setText("账号或者密码错误");
        }

    }


    @FXML
    void post(ActionEvent event) {
        if (RegisterMapper.getPermission(accountField.getText(), passwordField.getText()) == 1){
            RestaurantApplication.changePage("/views/serverPost.fxml");
        }else {
            registerResult.setText("权限不足,请联系管理员进行注册");
        }

    }
}
