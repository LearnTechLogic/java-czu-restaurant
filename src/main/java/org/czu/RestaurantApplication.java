package org.czu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class RestaurantApplication extends Application {

    private static RestaurantApplication instance; // 保存应用实例
    private Stage primaryStage;
    private static String initialPage = "/views/role.fxml";

    @Override
    public void start(Stage primaryStage) {
        instance = this; // 初始化时保存实例
        this.primaryStage = primaryStage;
        try {
            loadPage(initialPage);
            primaryStage.setTitle("餐厅管理系统");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 加载指定页面
    public void loadPage(String pagePath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(pagePath));
        Parent root = loader.load();

        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(root));
        } else {
            primaryStage.getScene().setRoot(root);
        }
    }

    // 静态方法，用于从任何地方切换页面
    public static void changePage(String pagePath) {
        if (instance != null) { // 直接使用静态实例
            try {
                instance.loadPage(pagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}