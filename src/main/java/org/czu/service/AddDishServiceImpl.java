package org.czu.service;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.czu.mapper.ManageMapper;
import org.czu.pojo.Classification;
import org.czu.pojo.Dish;
import org.czu.utils.AliyunOSSOperator;
import org.czu.utils.ShowAlert;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class AddDishServiceImpl {

    public static void addDish() {
        Dish dish = new Dish();
        Dialog<Dish> dialog = new Dialog<>();
        dialog.setTitle("添加菜品");
        dialog.setHeaderText("请输入菜品信息");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("菜品名称");
        nameField.setMaxWidth(200);

        TextField priceField = new TextField();
        priceField.setPromptText("价格");
        priceField.setMaxWidth(200);

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("菜品描述");
        descriptionField.setMaxWidth(200);

        ComboBox<String> classificationCombo = new ComboBox<>();
        List<Classification> classifications = ManageMapper.getDishClassification();
        if (classifications != null) {
            for (Classification classification : classifications) {
                classificationCombo.getItems().add(classification.getName());
            }
        }
        classificationCombo.setValue("选择分类");
        classificationCombo.setOnAction(event -> {
            String selectedName = classificationCombo.getValue();
            for (Classification perm : classifications) {
                if (perm.getName().equals(selectedName)) {
                    dish.setClassification(perm.getId());
                    break;
                }
            }
        });
        classificationCombo.setMaxWidth(200);

        Button uploadButton = new Button("上传图片");
        ImageView previewImageView = new ImageView("https://czurestaurant.oss-cn-beijing.aliyuncs.com/2025/06/c23c928f-c075-4f3e-ae40-677bf1557227.png");
        previewImageView.setFitWidth(80);
        previewImageView.setFitHeight(80);
        previewImageView.setPreserveRatio(true);

        // 价格输入过滤
        priceField.setTextFormatter(new TextFormatter<>(change -> {
            String text = change.getControlNewText();
            if (text.matches("\\d*(\\.\\d{0,2})?")) {
                return change;
            }
            return null;
        }));

        // 图片上传功能
        uploadButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择菜品图片");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());
            if (selectedFile != null) {
                try {
                    byte[] content = Files.readAllBytes(selectedFile.toPath());
                    String originalFilename = selectedFile.getName();
                    AliyunOSSOperator ossOperator = new AliyunOSSOperator();
                    String imageUrl = ossOperator.uploadImage(content, originalFilename);
                    dish.setImage(imageUrl);
                    previewImageView.setImage(new Image(imageUrl));
                } catch (Exception e) {
                    e.printStackTrace();
                    ShowAlert.showAlert(Alert.AlertType.ERROR, "错误", "图片上传失败: " + e.getMessage());
                }
            }
        });

        // 设置对话框内容
        VBox formLayout = new VBox(15);
        HBox imagebox = new HBox(10, uploadButton, previewImageView);
        imagebox.setPrefHeight(80);
        formLayout.getChildren().addAll(
                new Label("菜品名称:"), nameField,
                new Label("价格:"), priceField,
                new Label("菜品描述:"), descriptionField,
                new Label("分类ID:"), classificationCombo,
                new Label("菜品图片:"),
                imagebox
        );
        dialog.getDialogPane().setContent(formLayout);

        // 请求焦点到名称字段
        javafx.application.Platform.runLater(nameField::requestFocus);

        // 获取OK按钮引用
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

        // 禁用默认的OK按钮行为
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume(); // 阻止默认行为

            // 表单验证
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                ShowAlert.showAlert(Alert.AlertType.ERROR, "名称不能为空", "请输入名称");
                nameField.requestFocus();
                return;
            }

            String priceText = priceField.getText().trim();
            if (priceText.isEmpty()) {
                ShowAlert.showAlert(Alert.AlertType.ERROR, "价格格式错误", "请输入正确价格");
                priceField.requestFocus();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceText);
                if (price <= 0) {
                    ShowAlert.showAlert(Alert.AlertType.ERROR, "价格格式错误", "请输入大于0的价格");
                    priceField.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                ShowAlert.showAlert(Alert.AlertType.ERROR, "价格格式错误", "请输入数字格式的价格");
                priceField.requestFocus();
                return;
            }

            if (dish.getClassification() <= 0) {
                ShowAlert.showAlert(Alert.AlertType.ERROR, "分类错误", "请选择有效分类");
                classificationCombo.requestFocus();
                return;
            }

            if (dish.getImage() == null || dish.getImage().isEmpty()) {
                ShowAlert.showAlert(Alert.AlertType.ERROR, "图片错误", "请上传菜品图片");
                return;
            }

            // 验证通过，设置dish属性
            dish.setName(name);
            dish.setPrice(price);
            dish.setDescribe(descriptionField.getText().trim());

            // 提交数据
            try {
                ManageMapper.insertData(dish);
                ShowAlert.showAlert(Alert.AlertType.INFORMATION, "成功", "菜品添加成功");
                dialog.setResult(dish); // 设置结果以关闭对话框
                dialog.close(); // 手动关闭对话框
            } catch (Exception e) {
                e.printStackTrace();
                ShowAlert.showAlert(Alert.AlertType.ERROR, "数据库错误", "菜品添加失败: " + e.getMessage());
            }
        });

        // 简化结果转换器，仅用于关闭对话框
//        dialog.setResultConverter(dialogButton -> {
//            if (dialogButton == ButtonType.OK) {
//                return dish; // 实际逻辑已在事件过滤器中处理
//            }
//            return null;
//        });

        // 显示对话框
        dialog.showAndWait();
    }
}