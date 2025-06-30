package org.czu.mapper;

import org.czu.pojo.Server;
import org.czu.pojo.Classification;
import org.czu.utils.MysqlConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServerPostMapper {

    public static List<Classification> getServerPersimmon(){
        List<Classification> list = new ArrayList<>();
        String sql = "select * from server_permission";
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

    public static int getMaxId(){
        String sql = "select max(id) from server";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = MysqlConnect.getConnection();
            pstmt = conn.prepareStatement(sql);
            if (conn == null) {
                System.err.println("获取数据库连接失败，注册终止");
                return -1;
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }catch (Exception e){
            System.err.println("数据库连接失败: " + e.getMessage());
        }
        return -1;
    }

    public static boolean addServer(Server server) {
        String sql = "INSERT INTO server(id, name, password, permission) VALUES(?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = MysqlConnect.getConnection();
            if (conn == null) {
                System.err.println("获取数据库连接失败");
                return false;
            }
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, server.getId());
            pstmt.setString(2, server.getName());
            pstmt.setString(3, server.getPassword());
            pstmt.setInt(4, server.getPermission());

            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("插入服务器记录失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("关闭数据库资源失败: " + e.getMessage());
            }
        }
    }

}
