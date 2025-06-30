package org.czu.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import org.czu.RestaurantApplication;
import org.czu.mapper.ServerPostMapper;
import org.czu.pojo.Server;
import org.czu.pojo.Classification;
import org.czu.utils.ShowAlert;

import java.util.List;
import java.util.regex.Pattern;

public class ServerPostController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField accountField;

    @FXML
    private TextField passwordField;

    @FXML
    private Label passowordRequire;

    @FXML
    private ComboBox<String> functionCombo;

    int permissionId = 0;
    int maxId;

    // 密码格式正则表达式（至少8位，包含大小写字母、数字和特殊字符）
    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
    private static final Pattern PASSWORD_REGEX = Pattern.compile(PASSWORD_PATTERN);

    @FXML
    private void initialize() {
        List<Classification> classifications = ServerPostMapper.getServerPersimmon();

        // 将权限列表添加到ComboBox中
        if (classifications != null) {
            for (Classification classification : classifications) {
                functionCombo.getItems().add(classification.getName());
            }
        }

        functionCombo.setValue("选择对应的权限");
        functionCombo.setOnAction(event -> {
            // 获取选中的权限名称
            String selectedPermission = functionCombo.getValue();
            // 根据权限名称获取对应的权限ID（需要优化查询逻辑）
            for (Classification perm : classifications) {
                if (perm.getName().equals(selectedPermission)) {
                    permissionId = perm.getId();
                    break;
                }
            }
        });

        maxId = ServerPostMapper.getMaxId();
        accountField.setText(String.valueOf(maxId + 1));
        accountField.setEditable(false);

        // 添加密码输入实时验证
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePassword(newValue);
        });
    }

    private boolean validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            passwordField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return false;
        }

        boolean isValid = PASSWORD_REGEX.matcher(password).matches();
        if (isValid) {
            passwordField.setStyle(""); // 验证通过，清除边框
        } else {
            passwordField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        }
        return isValid;
    }

    public void handleSubmit(ActionEvent actionEvent) {
        // 验证姓名
        if (nameField.getText() == null || nameField.getText().isEmpty()) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "输入错误", "请输入姓名");
            return;
        }

        // 验证密码
        String password = passwordField.getText();
        if (password == null || password.isEmpty() || !validatePassword(password)) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "密码错误", "密码必须至少8位，包含大小写字母、数字和特殊字符");
            return;
        }

        // 验证权限
        if (permissionId == 0) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "选择错误", "请选择权限");
            return;
        }

        // 创建Server对象并添加
        Server server = new Server(maxId + 1, nameField.getText(), password, permissionId);
        if (ServerPostMapper.addServer(server)) {
            System.out.println("添加成功");
            RestaurantApplication.changePage("/views/register.fxml");
        } else {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "添加失败", "服务器错误，添加失败");
            System.out.println("添加失败");
        }
    }

    public void handleReset(ActionEvent actionEvent) {
        nameField.clear();
        functionCombo.setValue("选择对应的权限");
        permissionId = 0;
        passwordField.clear();
        passwordField.setStyle(""); // 清除错误样式
    }


}