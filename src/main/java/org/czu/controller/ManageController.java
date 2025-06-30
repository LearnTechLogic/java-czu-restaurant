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
import org.czu.pojo.Classification;
import org.czu.pojo.Dish;
import org.czu.service.AddDishServiceImpl;
import org.czu.service.DeleteDishServiceImpl;
import org.czu.service.EditDishServiceImpl;
import org.czu.utils.BindColumns;
import org.czu.utils.ShowAlert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ManageController {

    @FXML
    private TableView<Dish> dishTable;

    @FXML
    private TableColumn<Dish, ImageView> imageColumn;

    @FXML
    private TableColumn<Dish, String> nameColumn;

    @FXML
    private TableColumn<Dish, String> priceColumn;

    @FXML
    private TableColumn<Dish, String> categoryColumn;

    @FXML
    private TableColumn<Dish, String> stateColumn;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button orderButton;

    private ObservableList<Dish> dishList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        BindColumns.bindColumns(dishTable, imageColumn, nameColumn, priceColumn, categoryColumn, stateColumn);
        // 配置表格列
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrice() + ""));
        priceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrice() + ""));
        priceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f", cellData.getValue().getPrice()))
        );

        // 获取分类列表并创建映射
        List<Classification> classifications = ManageMapper.getDishClassification();
        Map<Integer, String> categoryMap = classifications.stream()
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
        categoryColumn.setCellValueFactory(cellData -> {
            Dish dish = cellData.getValue();
            Integer categoryId = dish.getClassification();
            String categoryName = categoryMap.getOrDefault(categoryId, "未知分类");
            return new SimpleStringProperty(categoryName);
        });

        // 设置分类列单元格工厂（处理颜色）
        categoryColumn.setCellFactory(column -> {
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

        // 获取状态列表并创建映射
        List<Classification> dishStates = ManageMapper.getDishState();
        Map<Integer, String> stateMap = dishStates.stream()
                .collect(Collectors.toMap(Classification::getId, Classification::getName));

        // 创建状态ID到颜色的映射
        Map<Integer, String> stateColorMap = new HashMap<>();
        stateColorMap.put(1, "-fx-background-color: #90EE90;");  // 上架 - 浅绿色
        stateColorMap.put(2, "-fx-background-color: #FFA07A;");  // 下架 - 浅橙色
        stateColorMap.put(3, "-fx-background-color: #D3D3D3;");  // 售罄 - 浅灰色
        // 可根据实际状态ID扩展更多颜色...

        // 配置状态列
        stateColumn.setCellValueFactory(cellData -> {
            Integer stateId = cellData.getValue().getState();
            String stateName = stateMap.getOrDefault(stateId, "未知状态");
            return new SimpleStringProperty(stateName);
        });

        // 设置状态列单元格工厂（处理颜色）
        stateColumn.setCellFactory(column -> {
            return new TableCell<Dish, String>() {
                @Override
                protected void updateItem(String stateName, boolean empty) {
                    super.updateItem(stateName, empty);

                    if (empty || stateName == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(stateName);

                        // 查找状态ID并应用颜色
                        Integer stateId = stateMap.entrySet().stream()
                                .filter(e -> e.getValue().equals(stateName))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse(null);

                        String colorStyle = (stateId != null) ?
                                stateColorMap.getOrDefault(stateId, "") : "";
                        setStyle(colorStyle);
                    }
                }
            };
        });

        // 配置图片列
        imageColumn.setCellFactory(param -> new TableCell<Dish, ImageView>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
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

        // 设置表格选择模式
        dishTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // 监听选择变化，控制按钮状态
        dishTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            editButton.setDisable(newVal == null);
            deleteButton.setDisable(newVal == null);
        });

        // 初始化时加载数据
        ManageMapper.getData(dishList, dishTable);
    }

    int account = Integer.parseInt(RegisterController.account);
    int persmission = ManageMapper.getServerPermission(account);
    @FXML
    private void refreshData() {
        ManageMapper.getData(dishList, dishTable);
    }

    @FXML
    private void showAddDishDialog() {

        if (persmission == 2 || persmission == 1){
            AddDishServiceImpl.addDish();
            refreshData();
        }else {
            ShowAlert.showAlert(Alert.AlertType.WARNING, "添加菜品失败", "您没有权限添加菜品");
        }
    }

    @FXML
    private void showEditDishDialog() {
        if (persmission == 3 || persmission == 1){
            Dish dish = dishTable.getSelectionModel().getSelectedItem();
            if (dish == null) {
                ShowAlert.showAlert(Alert.AlertType.WARNING, "提示", "请选择要编辑的菜品");
            }
            EditDishServiceImpl.editDish(dish);
            refreshData();
        }else {
            ShowAlert.showAlert(Alert.AlertType.WARNING, "修改菜品失败", "您没有权限修改菜品");
        }

    }

    @FXML
    private void deleteSelectedDish() {
        if (persmission == 1 || persmission == 4) {
            Dish dish = dishTable.getSelectionModel().getSelectedItem();
            if (dish == null) {
                ShowAlert.showAlert(Alert.AlertType.WARNING, "提示", "请选择要删除的菜品");
            }
            DeleteDishServiceImpl.deleteDish(dish);
            refreshData();
        }else {
            ShowAlert.showAlert(Alert.AlertType.WARNING, "删除菜品失败", "您没有权限删除菜品");
        }
    }

    @FXML
    private void switchToMenuView () {
        RestaurantApplication.changePage("/views/order.fxml");
    }
}



