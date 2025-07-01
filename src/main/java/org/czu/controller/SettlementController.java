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
import org.czu.mapper.SettlementMapper;
import org.czu.mapper.ShopMapper;
import org.czu.pojo.Classification;
import org.czu.pojo.Dish;
import org.czu.utils.BindColumns;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SettlementController {

    @FXML
    private Label leftTableNumberLabel;

    @FXML
    private Label rightTableNumberLabel;

    @FXML
    private TableView<Dish> DishTable;

    @FXML
    private TableColumn<Dish, ImageView> ImageColumn;

    @FXML
    private TableColumn<Dish, String> NameColumn;

    @FXML
    private TableColumn<Dish, String> PriceColumn;

    @FXML
    private TableColumn<Dish, Integer> NumColumn;

    @FXML
    private TableColumn<Dish, String> StateColumn;

    @FXML
    private TableColumn<Dish, LocalDateTime> EndTimeColumn;

    @FXML
    private Button backButton;


    private ObservableList<Dish> DishList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        BindColumns.bindColumns(DishTable, ImageColumn, NameColumn, PriceColumn, NumColumn, StateColumn, EndTimeColumn);
        leftTableNumberLabel.setText("当前:" + TableController.tableId + "号桌");
        rightTableNumberLabel.setText("订单号:" + TableController.orderId);
        NameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        PriceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f", cellData.getValue().getPrice())));
        NumColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        ImageColumn.setCellFactory(param -> new TableCell<Dish, ImageView>() {
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

        // 获取分类列表并创建映射
        List<Classification> classificationss = SettlementMapper.getOrderState();
        Map<Integer, String> categoryMap = classificationss.stream()
                .collect(Collectors.toMap(Classification::getId, Classification::getName));
        // 创建分类ID到颜色的映射
        Map<Integer, String> categoryColorMap = new HashMap<>();
        categoryColorMap.put(1, "-fx-background-color: lightcoral;");  // 未支付
        categoryColorMap.put(2, "-fx-background-color: lightblue;");   // 备菜中
        categoryColorMap.put(3, "-fx-background-color: lightyellow;"); // 制作中
        categoryColorMap.put(4, "-fx-background-color: lightgreen;");  // 上菜中
        categoryColorMap.put(5, "-fx-background-color: lightpink;");   // 已完成
        // 配置分类列
        StateColumn.setCellValueFactory(cellData -> {
            Dish dish = cellData.getValue();
            Integer categoryId = dish.getState();
            System.out.println(categoryId);
            String categoryName = categoryMap.getOrDefault(categoryId, "未知状态");
            return new SimpleStringProperty(categoryName);
        });
        // 设置分类列单元格工厂（处理颜色）
        StateColumn.setCellFactory(column -> {
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

        // 配置时间列
        EndTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        // 设置时间格式（自定义单元格工厂）
        EndTimeColumn.setCellFactory(column -> {
            TableCell<Dish, LocalDateTime> cell = new TableCell<Dish, LocalDateTime>() {
                private final DateTimeFormatter formatter =
//                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                DateTimeFormatter.ofPattern("HH:mm:ss");

                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item)); // 格式化时间显示
                    }
                }
            };
            return cell;
        });

        SettlementMapper.getData(DishList, DishTable);

    }


    private void refreshDishTable() {
        ShopMapper.getData(DishList, DishTable);
    }

    @FXML
    private void BackSettlement() {
        RestaurantApplication.changePage("/views/table.fxml");
    }


}



