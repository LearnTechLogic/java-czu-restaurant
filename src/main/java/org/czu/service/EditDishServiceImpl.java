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
import java.util.Objects;

public class EditDishServiceImpl {

    public static void editDish(Dish dish) {
        // 防御性编程：确保传入的dish对象不为空
        if (dish == null) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "数据错误", "编辑的菜品对象不能为空");
            return;
        }

        Dialog<Dish> dialog = new Dialog<>();
        dialog.setTitle("编辑菜品");
        dialog.setHeaderText("请修改菜品信息");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 初始化菜品名称（使用dish对象的属性）
        TextField nameField = new TextField(dish.getName());
        nameField.setPromptText("菜品名称");
        nameField.setMaxWidth(200);

        // 初始化价格（使用dish对象的属性）
        TextField priceField = new TextField(String.valueOf(dish.getPrice()));
        priceField.setPromptText("价格");
        priceField.setMaxWidth(200);

        // 初始化描述（使用dish对象的属性）
        TextField descriptionField = new TextField(dish.getDescribe());
        descriptionField.setPromptText("菜品描述");
        descriptionField.setMaxWidth(200);

        ComboBox<String> classificationCombo = new ComboBox<>();
        List<Classification> classifications = ManageMapper.getDishClassification();

        // 加载分类数据并设置默认选择
        if (classifications != null && !classifications.isEmpty()) {
            for (Classification classification : classifications) {
                classificationCombo.getItems().add(classification.getName());

                // 自动选择当前菜品的分类
                if (classification.getId() == dish.getClassification()) {
                    classificationCombo.setValue(classification.getName());
                }
            }
        } else {
            classificationCombo.getItems().add("无分类数据");
        }

        // 处理分类选择事件
        classificationCombo.setOnAction(event -> {
            String selectedName = classificationCombo.getValue();
            if (selectedName != null && classifications != null) {
                for (Classification classification : classifications) {
                    if (classification.getName().equals(selectedName)) {
                        dish.setClassification(classification.getId());
                        break;
                    }
                }
            }
        });
        classificationCombo.setMaxWidth(200);

        ComboBox<String> stateCombo = new ComboBox<>();
        List<Classification> states = ManageMapper.getDishState();
        // 加载状态数据并设置默认选择
        if (states != null && !states.isEmpty()) {
            for (Classification state : states) {
                stateCombo.getItems().add(state.getName());

                // 自动选择当前菜品的状态（修正此处的setValue错误）
                if (state.getId() == dish.getState()) {
                    stateCombo.setValue(state.getName());
                }
            }
        } else {
            stateCombo.getItems().add("无状态数据");
        }

        // 处理状态选择事件
        stateCombo.setOnAction(event -> {
            String selectedName = stateCombo.getValue();
            if (selectedName != null && states != null) {
                for (Classification state : states) {
                    if (state.getName().equals(selectedName)) {
                        System.out.println(state.getId());
                        dish.setState(state.getId());
                        break;
                    }
                }
            }
        });
        stateCombo.setMaxWidth(200);



        // 图片显示与上传功能
        Button uploadButton = new Button("重传图片");
        String imageUrl = dish.getImage();
        // 设置默认图片（当菜品图片为空时）
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = "https://czurestaurant.oss-cn-beijing.aliyuncs.com/2025/06/c23c928f-c075-4f3e-ae40-677bf1557227.png";
        }
        ImageView previewImageView = new ImageView(imageUrl);
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
            fileChooser.setTitle("重新上传菜品图片");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());
            if (selectedFile != null) {
                try {
                    byte[] content = Files.readAllBytes(selectedFile.toPath());
                    String originalFilename = selectedFile.getName();
                    AliyunOSSOperator ossOperator = new AliyunOSSOperator();
                    String newImageUrl = ossOperator.uploadImage(content, originalFilename);
                    dish.setImage(newImageUrl);
                    previewImageView.setImage(new Image(newImageUrl));
                } catch (Exception e) {
                    e.printStackTrace();
                    ShowAlert.showAlert(Alert.AlertType.ERROR, "错误", "图片上传失败: " + e.getMessage());
                }
            }
        });

        // 设置对话框内容（添加状态组合框到布局中）
        VBox formLayout = new VBox(15);
        HBox imagebox = new HBox(10, uploadButton, previewImageView);
        imagebox.setPrefHeight(80);
        formLayout.getChildren().addAll(
                new Label("菜品名称:"), nameField,
                new Label("价格:"), priceField,
                new Label("菜品描述:"), descriptionField,
                new Label("分类:"), classificationCombo,
                new Label("状态:"), stateCombo, // 添加状态标签和组合框
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

            // 分类验证（允许默认分类，但需确保分类ID有效）
            if (dish.getClassification() <= 0 && classifications != null && !classifications.isEmpty()) {
                ShowAlert.showAlert(Alert.AlertType.ERROR, "分类错误", "请选择有效分类");
                classificationCombo.requestFocus();
                return;
            }

            // 状态验证（添加状态验证逻辑）
            if (dish.getState() <= 0 && states != null && !states.isEmpty()) {
                ShowAlert.showAlert(Alert.AlertType.ERROR, "状态错误", "请选择有效状态");
                stateCombo.requestFocus();
                return;
            }

            // 图片验证（编辑时允许保留原有图片，不强制重新上传）
            if (dish.getImage() == null || dish.getImage().isEmpty()) {
                // 仅在图片为空时提示（编辑时允许保留原图）
                ShowAlert.showAlert(Alert.AlertType.WARNING, "图片提示", "菜品图片为空，建议上传图片");
            }

            // 验证通过，设置dish属性
            dish.setName(name);
            dish.setPrice(price);
            dish.setDescribe(descriptionField.getText().trim());

            // 提交数据
            try {
                ManageMapper.updateDish(dish);
                ShowAlert.showAlert(Alert.AlertType.INFORMATION, "成功", "菜品修改成功");
                dialog.setResult(dish);
                dialog.close();
            } catch (Exception e) {
                e.printStackTrace();
                ShowAlert.showAlert(Alert.AlertType.ERROR, "数据库错误", "菜品修改失败: " + e.getMessage());
            }
        });

        // 显示对话框
        dialog.showAndWait();
    }
}