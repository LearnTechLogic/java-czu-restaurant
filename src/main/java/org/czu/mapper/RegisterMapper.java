package org.czu.mapper;

import org.czu.utils.MysqlConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterMapper {

    public static boolean verify(String id, String password){
        String sql = "SELECT * FROM server WHERE id = ? AND password = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = MysqlConnect.getConnection();
            if (conn == null) {
                System.err.println("获取数据库连接失败，注册终止");
                return false;
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                rs.close();
                return true;
            }else{
                rs.close();
                return false;
            }
        }
        catch (SQLException e) {
            System.err.println("数据库连接失败: " + e.getMessage());
            return false;
        }
    }
    // 根据 id 获取用户权限
    public static int getPermission(String id, String password) {
        String sql = "SELECT permission FROM server WHERE id = ? AND password = ? ";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = MysqlConnect.getConnection();
            if (conn == null) {
                System.err.println("获取数据库连接失败，查询终止");
                return 0;
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("permission");
            }
            return 0;

        } catch (SQLException e) {
            System.err.println("获取用户权限失败: " + e.getMessage());
            return 0;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    // 通用资源关闭方法
    private static void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        MysqlConnect.closeConnection(conn);
    }
}