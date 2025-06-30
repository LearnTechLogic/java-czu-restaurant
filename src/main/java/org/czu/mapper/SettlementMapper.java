package org.czu.mapper;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import org.czu.controller.TableController;
import org.czu.pojo.Classification;
import org.czu.pojo.Dish;
import org.czu.utils.MysqlConnect;
import org.czu.utils.ShowAlert;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SettlementMapper {
    public static void getData(ObservableList<Dish> DishList, TableView<Dish> DishTable) {
        DishList.clear();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Dish dish = null;
        int orderId = TableController.orderId;

        try {
            conn = MysqlConnect.getConnection(); // 直接调用静态方法
            String sql = "SELECT d.id, d.name, d.price, d.image, od.quantity, o.end_time, o.state " +
                    "FROM dish d " +
                    "JOIN order_detail od ON d.id = od.dish_id " +
                    "JOIN `order` o ON od.order_id = o.id " + // 连接订单表获取end_time
                    "WHERE od.order_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setPrice(rs.getDouble("price"));
                dish.setImage(rs.getString("image"));
                dish.setQuantity(rs.getInt("quantity"));
                Timestamp endTime = rs.getTimestamp("end_time");
                if (endTime != null) {
                    dish.setEndTime(endTime.toLocalDateTime());
                }
                dish.setState(rs.getInt("state"));
                DishList.add(dish);
            }

            DishTable.setItems(DishList);

        } catch (SQLException e) {
            e.printStackTrace();
            ShowAlert.showAlert(Alert.AlertType.ERROR, "数据库查询失败", "请检查数据库连接");
        }
    }

    public static List<Classification> getOrderState(){
        List<Classification> list = new ArrayList<>();
        String sql = "select * from order_state";
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
}
