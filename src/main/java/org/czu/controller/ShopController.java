package org.czu.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.czu.RestaurantApplication;
import org.czu.mapper.ManageMapper;
import org.czu.mapper.ShopMapper;
import org.czu.pojo.Classification;
import org.czu.pojo.Dish;
import org.czu.utils.ShowAlert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShopController {
    public static int classificationId = 7;

    @FXML
    private Label leftTableNumberLabel;

    @FXML
    private Label rightTableNumberLabel;

    @FXML
    private ComboBox<String> dishCombo;

    @FXML
    private TextField dishNameIndexTextField;

    @FXML
    private Button searchButton;

    @FXML
    private TableView<Dish> leftDishTable;

    @FXML
    private TableColumn<Dish, ImageView> leftImageColumn;

    @FXML
    private TableColumn<Dish, String> leftNameColumn;

    @FXML
    private TableColumn<Dish, String> leftClassificationColumn;

    @FXML
    private TableColumn<Dish, String> leftDirectionColumn;

    @FXML
    private TableColumn<Dish, String> leftPriceColumn;

    @FXML
    private TableView<Dish> rightDishTable;

    @FXML
    private TableColumn<Dish, ImageView> rightImageColumn;

    @FXML
    private TableColumn<Dish, String> rightNameColumn;

    @FXML
    private TableColumn<Dish, String> rightPriceColumn;

    @FXML
    private TableColumn<Dish, Integer> rightNumColumn;

    @FXML
    private Button addButton;

    @FXML
    private Button increaseButton;

    @FXML
    private Button decreaseButton;

    @FXML
    private Button settlementButton;

    private ObservableList<Dish> dishList = FXCollections.observableArrayList();
    private ObservableList<Dish> rightDishList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        bindColumns(leftDishTable, leftImageColumn, leftNameColumn, leftClassificationColumn, leftDirectionColumn, leftPriceColumn);
        bindColumns(rightDishTable, rightImageColumn, rightNameColumn, rightPriceColumn, rightNumColumn);
        leftTableNumberLabel.setText("当前:" + TableController.tableId + "号桌");
        rightTableNumberLabel.setText("订单号:" + TableController.orderId);

        dishNameIndexTextField.setText( "");
        dishNameIndexTextField.toFront();
        searchButton.toFront();
        dishCombo.toFront();
        List<Classification> classifications = ShopMapper.getClassification();
        // 将权限列表添加到ComboBox中
        if (classifications != null) {
            for (Classification classification : classifications) {
                dishCombo.getItems().add(classification.getName());
            }
        }
        dishCombo.setValue("全部菜品");
        dishCombo.setOnAction(event -> {
            // 获取选中的权限名称
            String selectedPermission = dishCombo.getValue();
            // 根据权限名称获取对应的权限ID（需要优化查询逻辑）
            for (Classification perm : classifications) {
                if (perm.getName().equals(selectedPermission)) {
                    classificationId = perm.getId();
                    refreshDishTable();
                    break;
                }
            }
        });

        // 配置表格列
        leftNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        leftPriceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f", cellData.getValue().getPrice())));
        leftDirectionColumn.setCellValueFactory(new PropertyValueFactory<>("describe"));

        // 获取分类列表并创建映射
        List<Classification> classificationss = ManageMapper.getDishClassification();
        Map<Integer, String> categoryMap = classificationss.stream()
                .collect(Collectors.toMap(Classification::getId, Classification::getName));
        // 创建分类ID到颜色的映射
        Map<Integer, String> categoryColorMap = new HashMap<>();
        categoryColorMap.put(1, "-fx-background-color: lightcoral;");  // 酒水
        categoryColorMap.put(2, "-fx-background-color: lightblue;");   // 川菜
        categoryColorMap.put(3, "-fx-background-color: lightyellow;"); // 鲁菜
        categoryColorMap.put(4, "-fx-background-color: lightgreen;");  // 湘菜
        categoryColorMap.put(5, "-fx-background-color: lightpink;");   // 粤菜
        categoryColorMap.put(6, "-fx-background-color: lightskyblue;");// 闽菜
        // 配置分类列
        leftClassificationColumn.setCellValueFactory(cellData -> {
            Dish dish = cellData.getValue();
            Integer categoryId = dish.getClassification();
            String categoryName = categoryMap.getOrDefault(categoryId, "未知分类");
            return new SimpleStringProperty(categoryName);
        });
        // 设置分类列单元格工厂（处理颜色）
        leftClassificationColumn.setCellFactory(column -> {
            return new TableCell<Dish, String>() {
                @Override
                protected void updateItem(String categoryName, boolean empty) {
                    super.updateItem(categoryName, empty);

                    if (empty || categoryName == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(categoryName);

                        // 查找分类ID并应用颜色
                        Integer categoryId = categoryMap.entrySet().stream()
                                .filter(e -> e.getValue().equals(categoryName))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse(null);

                        String colorStyle = (categoryId != null) ?
                                categoryColorMap.getOrDefault(categoryId, "") : "";
                        setStyle(colorStyle);
                    }
                }
            };
        });

        // 配置图片列
        leftImageColumn.setCellFactory(param -> new TableCell<Dish, ImageView>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(ImageView item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getTableView() == null || getTableView().getItems() == null) {
                    setGraphic(null);
                } else {
                    Task<Void> loadImageTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            Dish dish = getTableView().getItems().get(getIndex());
                            String imagePath = dish.getImage();
                            Image image;
                            if (imagePath != null && !imagePath.isEmpty()) {
                                image = new Image(imagePath);
                            } else {
                                // 使用默认图片路径
                                image = new Image("https://czurestaurant.oss-cn-beijing.aliyuncs.com/2025/06/a9442778-f0b7-4371-b5b0-6a9e3790ade9.png");
                            }
                            Platform.runLater(() -> {
                                imageView.setImage(image);
                                setGraphic(imageView);
                            });
                            return null;
                        }
                    };

                    Thread thread = new Thread(loadImageTask);
                    thread.start();
                }
            }
        });

        ShopMapper.getData(dishList,leftDishTable);


        rightNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        rightPriceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f", cellData.getValue().getPrice())));
        rightNumColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        rightImageColumn.setCellFactory(param -> new TableCell<Dish, ImageView>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(ImageView item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getTableView() == null || getTableView().getItems() == null) {
                    setGraphic(null);
                } else {
                    Task<Void> loadImageTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            Dish dish = getTableView().getItems().get(getIndex());
                            String imagePath = dish.getImage();
                            Image image;
                            if (imagePath != null && !imagePath.isEmpty()) {
                                image = new Image(imagePath);
                            } else {
                                // 使用默认图片路径
                                image = new Image("https://czurestaurant.oss-cn-beijing.aliyuncs.com/2025/06/a9442778-f0b7-4371-b5b0-6a9e3790ade9.png");
                            }
                            Platform.runLater(() -> {
                                imageView.setImage(image);
                                setGraphic(imageView);
                            });
                            return null;
                        }
                    };

                    Thread thread = new Thread(loadImageTask);
                    thread.start();
                }
            }
        });

        refreshDishTable();

    }

    private void bindColumns(TableView<Dish> tableView, TableColumn<Dish, ?>... columns) {
        double columnCount = columns.length;
        for (TableColumn<Dish, ?> column : columns) {
            column.prefWidthProperty().bind(tableView.widthProperty().divide(columnCount));
        }
    }

    private void refreshDishTable() {
        String searchName = dishNameIndexTextField.getText().trim();
        ShopMapper.getData(searchName, dishList, leftDishTable);
        //ShopMapper.getData(dishList, leftDishTable);
        ShopMapper.getRightData(rightDishList, rightDishTable);
    }

    @FXML
    private void addButton(){
        Dish dish = leftDishTable.getSelectionModel().getSelectedItem();
        if (dish == null) {
            ShowAlert.showAlert(Alert.AlertType.WARNING, "提示", "请选择要添加的菜品");
            return;
        }
        ShopMapper.addDish(dish);
        refreshDishTable();
    }

    @FXML
    private void increaseButton(){
        Dish dish = rightDishTable.getSelectionModel().getSelectedItem();
        if (dish == null) {
            ShowAlert.showAlert(Alert.AlertType.WARNING, "提示", "请选择要添加的菜品");
            return;
        }
        ShopMapper.increaseDish(dish.getId());
        refreshDishTable();
    }

    @FXML
    private void decreaseButton(){
        Dish dish = rightDishTable.getSelectionModel().getSelectedItem();
        if (dish == null) {
            ShowAlert.showAlert(Alert.AlertType.WARNING, "提示", "请选择要删除的菜品");
            return;
        }
        ShopMapper.decreaseDish(dish.getId());
        refreshDishTable();
    }

    @FXML
    private void settlementButton(){
        ShopMapper.settlementDish();
        RestaurantApplication.changePage("/views/settlement.fxml");
    }

    @FXML
    private void searchDish(){
        String name = dishNameIndexTextField.getText();
        ShopMapper.getData(name, dishList, leftDishTable);

    }

}



