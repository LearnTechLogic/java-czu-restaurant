package org.czu.mapper;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import org.czu.pojo.Dish;
import org.czu.utils.MysqlConnect;
import org.czu.utils.ShowAlert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderMapper {
    public static void getData (ObservableList<Dish> dishList, TableView<Dish> dishTable){
        dishList.clear();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Dish dish = null;
        int satate = 5;

        try {
            conn = MysqlConnect.getConnection(); // 直接调用静态方法
            String sql = "SELECT od.order_id, d.name, d.image, od.quantity, o.end_time, o.state ,o.table_number " +
                    "FROM dish d " +
                    "JOIN order_detail od ON d.id = od.dish_id " +
                    "JOIN `order` o ON od.order_id = o.id " +
                    "WHERE o.state < ? " +
                    "ORDER BY od.order_id";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, satate);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                dish = new Dish();
                dish.setId(rs.getInt("order_id"));
                dish.setName(rs.getString("name"));
                dish.setState(rs.getInt("state"));
                dish.setImage(rs.getString("image"));
                dish.setTableId(rs.getInt("table_number"));
                dish.setQuantity(rs.getInt("quantity"));
                dishList.add(dish);
            }

            dishTable.setItems(dishList);

        } catch (SQLException e) {
            e.printStackTrace();
            ShowAlert.showAlert(Alert.AlertType.ERROR, "数据库查询失败", "请检查数据库连接");
        }

    }
}
