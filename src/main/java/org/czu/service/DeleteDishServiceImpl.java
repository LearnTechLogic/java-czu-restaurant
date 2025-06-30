package org.czu.service;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.czu.controller.RegisterController;
import org.czu.pojo.Dish;
import org.czu.mapper.ManageMapper;
import org.czu.utils.ShowAlert;

public class DeleteDishServiceImpl {
    public static void deleteDish(Dish dish) {
        // 创建确认对话框
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("您确定要删除 " + dish.getName() + " 吗？");

        // 显示对话框并等待用户响应
        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);

        // 如果用户点击了“确定”按钮，则执行删除操作
        if (result == ButtonType.OK) {
            ManageMapper.deleteData(dish);
        }
    }
}

