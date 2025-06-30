package org.czu.mapper;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import org.czu.pojo.Classification;
import org.czu.pojo.Dish;
import org.czu.utils.MysqlConnect;
import org.czu.utils.ShowAlert;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManageMapper {

    // 加载数据
    public static void getData(ObservableList<Dish> dishList, TableView<Dish> dishTable) {
        dishList.clear();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Dish dish = null;

        try {
            conn = MysqlConnect.getConnection(); // 直接调用静态方法
            String sql = "SELECT id, name, price, `describe`, server, state, image, classification " +
                    "FROM dish ORDER BY id";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setPrice(rs.getDouble("price"));
                dish.setDescribe(rs.getString("describe"));
                dish.setServer(rs.getInt("server"));
                dish.setState(rs.getInt("state"));
                dish.setImage(rs.getString("image"));
                dish.setClassification(rs.getInt("classification"));
                dishList.add(dish);
            }

            dishTable.setItems(dishList);

        } catch (SQLException e) {
            e.printStackTrace();
            ShowAlert.showAlert(Alert.AlertType.ERROR, "数据库查询失败", "请检查数据库连接");
        }
    }

    // 添加菜品
    public static void insertData(Dish dish) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        if (dish.getImage() == null){
            dish.setImage("https://java-studys.oss-cn-beijing.aliyuncs.com/1691c8c7-5749-410f-8278-2dcf1ece1551.jpg");
        }

        try {
            conn = MysqlConnect.getConnection();
            String sql = "INSERT INTO dish (name, price, `describe`, server, state, image, classification) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, dish.getName());
            pstmt.setDouble(2, dish.getPrice());
            pstmt.setString(3, dish.getDescribe());
            pstmt.setInt(4, dish.getServer());
            pstmt.setInt(5, dish.getState());
            pstmt.setString(6, dish.getImage());
            pstmt.setInt(7, dish.getClassification());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        dish.setId(id);
                    } else {
                        throw new SQLException("创建菜品时未获取到ID");
                    }
                }
                //ShowAlert.showAlert(Alert.AlertType.INFORMATION, "菜品添加成功", "菜品添加成功");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ShowAlert.showAlert(Alert.AlertType.ERROR, "添加菜品失败", "添加菜品失败: " );
        }
    }

    // 更新菜品
    public static void updateDish(Dish dish) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = MysqlConnect.getConnection();
            String sql = "UPDATE dish SET name=?, price=?, `describe`=?, server=?, image=?, classification=?, state=? " +
                    "WHERE id=?";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, dish.getName());
            pstmt.setDouble(2, dish.getPrice());
            pstmt.setString(3, dish.getDescribe());
            pstmt.setInt(4, dish.getServer());
            pstmt.setString(5, dish.getImage());
            pstmt.setInt(6, dish.getClassification());
            pstmt.setInt(7, dish.getState());
            pstmt.setInt(8, dish.getId());


            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ShowAlert.showAlert(Alert.AlertType.INFORMATION, "菜品更新成功", "菜品更新成功");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ShowAlert.showAlert(Alert.AlertType.ERROR, "更新菜品失败", e.getMessage());
        }
    }

    //删除菜品
    public static void deleteData(Dish dish) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = MysqlConnect.getConnection();
            String sql = "DELETE FROM dish WHERE id=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, dish.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ShowAlert.showAlert(Alert.AlertType.INFORMATION, "菜品删除成功", "菜品删除成功");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ShowAlert.showAlert(Alert.AlertType.ERROR, "删除菜品失败", e.getMessage());
        }
    }

    //获取菜品种类
    public static List<Classification> getDishClassification(){
        List<Classification> list = new ArrayList<>();
        String sql = "select * from dish_classification";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = MysqlConnect.getConnection();
            if (conn == null) {
                System.err.println("获取数据库连接失败，注册终止");
                return null;
            }
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                Classification Classification = new Classification(id, name);
                list.add(Classification);
            }
        }catch (Exception e){
            System.err.println("数据库连接失败: " + e.getMessage());
        }
        return list;
    }

    //获取菜品状态
    public static List<Classification> getDishState(){
        List<Classification> list = new ArrayList<>();
        String sql = "select * from dish_state";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = MysqlConnect.getConnection();
            if (conn == null) {
                System.err.println("获取数据库连接失败，注册终止");
                return null;
            }
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                Classification Classification = new Classification(id, name);
                list.add(Classification);
            }
        }catch (Exception e){
            System.err.println("数据库连接失败: " + e.getMessage());
        }
        return list;
    }

    //获取权限
    public static int getServerPermission(int id) {
        String sql = "SELECT permission FROM server WHERE id = ? ";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = MysqlConnect.getConnection();
            if (conn == null) {
                System.err.println("获取数据库连接失败，查询终止");
                return -1;
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                return rs.getInt("permission");
            }
        }catch (Exception e){
            System.err.println("数据库连接失败: " + e.getMessage());
        }
        return -1;
    }
}
