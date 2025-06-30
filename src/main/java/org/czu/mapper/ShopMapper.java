package org.czu.mapper;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import org.czu.controller.TableController;
import org.czu.pojo.Classification;
import org.czu.pojo.Dish;
import org.czu.utils.MysqlConnect;
import org.czu.utils.ShowAlert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ShopMapper {
    public static List<Classification> getClassification(){
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
                    "FROM dish ORDER BY classification";
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

    public static void getRightData(ObservableList<Dish> rightDishList, TableView<Dish> rightDishTable) {
        rightDishList.clear();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Dish dish = null;
        int orderId = TableController.orderId;

        try {
            conn = MysqlConnect.getConnection(); // 直接调用静态方法
            String sql ="SELECT d.id, d.name, d.price, d.image, od.quantity " +
                        "FROM dish d " +
                        "JOIN order_detail od ON d.id = od.dish_id " +
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
                rightDishList.add(dish);
            }

            rightDishTable.setItems(rightDishList);

        } catch (SQLException e) {
            e.printStackTrace();
            ShowAlert.showAlert(Alert.AlertType.ERROR, "数据库查询失败", "请检查数据库连接");
        }
    }


    public static int getMaxOrderId(int tableId) {
        String selectSql = "SELECT MAX(id) FROM `order`";
        String insertSql = "INSERT INTO `order` (id, table_number,state) VALUES (?, ?,?)";
        int maxId = -1;
        int state = 1;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = MysqlConnect.getConnection();
            if (conn == null) {
                System.err.println("获取数据库连接失败，操作终止");
                return -1;
            }

            // 关闭自动提交，确保两个操作在同一事务中
            conn.setAutoCommit(false);

            // 1. 获取当前最大ID
            pstmt = conn.prepareStatement(selectSql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                maxId = rs.getInt(1);
            } else {
                maxId = 0; // 表为空时从0开始
            }

            // 2. 插入新记录
            int newId = maxId + 1;
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setInt(1, newId);
            pstmt.setInt(2, tableId);
            pstmt.setInt(3, state);
            pstmt.executeUpdate();

            // 3. 提交事务
            conn.commit();

            return newId; // 返回新插入的ID

        } catch (SQLException e) {
            // 发生异常时回滚
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("事务回滚失败: " + ex.getMessage());
            }
            System.err.println("数据库操作失败: " + e.getMessage());
            return -1;
        } finally {
            // 确保资源关闭
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true); // 恢复自动提交模式
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("资源关闭失败: " + e.getMessage());
            }
        }
    }

    public static boolean addDish(Dish dish) {
        if (dish == null || dish.getId() <= 0) {
            System.err.println("参数错误：菜品对象无效或ID为非正整数");
            return false;
        }

        int dishId = dish.getId();
        int orderId = TableController.orderId;
        if (orderId <= 0) {
            System.err.println("参数错误：订单ID无效，orderId=" + orderId);
            return false;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean success = false;

        try {
            // 获取数据库连接
            conn = MysqlConnect.getConnection();
            if (conn == null) {
                System.err.println("获取数据库连接失败");
                return false;
            }

            // 开启事务
            conn.setAutoCommit(false);

            // 1. 检查菜品是否已在订单中
            String checkSql = "SELECT quantity FROM order_detail WHERE dish_id = ? AND order_id = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, dishId);
            pstmt.setInt(2, orderId);
            rs = pstmt.executeQuery();

            int quantity = 1;
            if (rs.next()) {
                // 已存在，更新数量
                quantity = rs.getInt("quantity") + 1;
                String updateSql = "UPDATE order_detail SET quantity = ? WHERE dish_id = ? AND order_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, quantity);
                    updateStmt.setInt(2, dishId);
                    updateStmt.setInt(3, orderId);
                    success = updateStmt.executeUpdate() > 0;
                }
            } else {
                // 不存在，插入新记录
                String insertSql = "INSERT INTO order_detail(order_id, quantity, dish_id) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, orderId);
                    insertStmt.setInt(2, quantity);
                    insertStmt.setInt(3, dishId);
                    success = insertStmt.executeUpdate() > 0;
                }
            }

            if (success) {
                conn.commit(); // 提交事务
            } else {
                conn.rollback(); // 操作失败回滚
            }

        } catch (SQLException e) {
            // 事务回滚
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("事务回滚失败: " + ex.getMessage());
            }

            System.err.println("数据库操作失败: " + e.getMessage());
            e.printStackTrace();
        }
        return success;
    }

    public static boolean increaseDish(int dishId){
        int orderId = TableController.orderId;
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = MysqlConnect.getConnection();
            if (conn == null) {
                System.err.println("获取数据库连接失败");
                return false;
            }

            conn.setAutoCommit(false); // 开启事务

            // 1. 查询当前数量
            String querySql = "SELECT quantity FROM order_detail WHERE order_id = ? AND dish_id = ?";
            System.out.println(orderId);
            System.out.println(dishId);
            pstmt = conn.prepareStatement(querySql);
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, dishId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int currentQuantity = rs.getInt("quantity");
                int newQuantity = currentQuantity + 1;

                // 2. 更新数量
                String updateSql = "UPDATE order_detail SET quantity = ? WHERE order_id = ? AND dish_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, newQuantity);
                    updateStmt.setInt(2, orderId);
                    updateStmt.setInt(3, dishId);
                    success = updateStmt.executeUpdate() > 0;
                }
            }

            if (success) {
                conn.commit(); // 提交事务
            } else {
                conn.rollback(); // 回滚事务
            }

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("事务回滚失败: " + ex.getMessage());
            }
            System.err.println("增加菜品数量失败: " + e.getMessage());
            e.printStackTrace();
        }
        return success;
    }

    public static boolean decreaseDish(int dishId) {
        int orderId = TableController.orderId;
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = MysqlConnect.getConnection();
            if (conn == null) {
                System.err.println("获取数据库连接失败");
                return false;
            }

            conn.setAutoCommit(false); // 开启事务

            // 1. 查询当前数量
            String querySql = "SELECT quantity, id FROM order_detail WHERE order_id = ? AND dish_id = ?";
            pstmt = conn.prepareStatement(querySql);
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, dishId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int currentQuantity = rs.getInt("quantity");
                int detailId = rs.getInt("id");

                if (currentQuantity <= 1) {
                    // 数量小于等于1，删除记录
                    String deleteSql = "DELETE FROM order_detail WHERE id = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                        deleteStmt.setInt(1, detailId);
                        success = deleteStmt.executeUpdate() > 0;
                    }
                } else {
                    // 数量大于1，减少数量
                    int newQuantity = currentQuantity - 1;
                    String updateSql = "UPDATE order_detail SET quantity = ? WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, newQuantity);
                        updateStmt.setInt(2, detailId);
                        success = updateStmt.executeUpdate() > 0;
                    }
                }
            }

            if (success) {
                conn.commit(); // 提交事务
            } else {
                conn.rollback(); // 回滚事务
            }

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("事务回滚失败: " + ex.getMessage());
            }
            System.err.println("减少菜品数量失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("资源关闭失败: " + e.getMessage());
            }
        }

        return success;
    }

    public static boolean settlementDish() {
        int orderId = TableController.orderId;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean success = false;

        try {
            conn = MysqlConnect.getConnection();
            if (conn == null) {
                System.err.println("获取数据库连接失败");
                return false;
            }

            // 1. 查询订单总商品数量（考虑单品数量）
            String countSql = "SELECT SUM(quantity) AS total_items FROM order_detail WHERE order_id = ?";
            pstmt = conn.prepareStatement(countSql);
            pstmt.setInt(1, orderId);
            rs = pstmt.executeQuery();

            int totalItems = 0;
            if (rs.next()) {
                totalItems = rs.getInt("total_items");
            } else {
                System.err.println("未找到订单详情，orderId=" + orderId);
                return false;
            }
            rs.close();
            pstmt.close();

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, totalItems * 3);  // 每件商品预计3分钟
            java.sql.Timestamp endTime = new java.sql.Timestamp(calendar.getTimeInMillis());

            // 4. 更新订单状态和时间
            String updateSql = "UPDATE `order` " +
                    "SET state = 2, " +
                    "    start_time = now(), " +
                    "    end_time = ? " +
                    "WHERE id = ?";

            pstmt = conn.prepareStatement(updateSql);
            pstmt.setTimestamp(1, endTime);
            pstmt.setInt(2, orderId);

            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;

            // 5. 记录结算信息
            System.out.println("订单 " + orderId + " 结算成功，共" + totalItems + "件商品，预计完成时间：" + endTime);

        } catch (SQLException e) {
            System.err.println("结算订单失败: " + e.getMessage());
            e.printStackTrace();
        }
        return success;
    }

}
