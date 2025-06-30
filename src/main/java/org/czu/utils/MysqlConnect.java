package org.czu.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MysqlConnect {
    private static String driver;
    private static String url;
    private static String username;
    private static String password;

    static {
        readConfig();
    }

    private static void readConfig() {
        Properties properties = new Properties();
        try (InputStream input = MysqlConnect.class.getClassLoader().getResourceAsStream("driver.properties")) {
            if (input == null) {
                throw new IOException("找不到 driver.properties 文件");
            }
            properties.load(input);
            driver = properties.getProperty("db.driver");
            url = properties.getProperty("db.url");
            username = properties.getProperty("db.username");
            password = properties.getProperty("db.password");

            // 验证配置是否加载成功
            System.out.println("数据库配置加载成功:");
            System.out.println("URL: " + url);
            System.out.println("Username: " + username);
            // 密码不打印，避免泄露
        } catch (IOException e) {
            System.err.println("加载数据库配置失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("初始化数据库连接失败", e);
        }
    }

    public static Connection getConnection() {
        try {
            if (driver == null || url == null || username == null || password == null) {
                throw new SQLException("数据库配置未初始化");
            }
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("数据库连接成功");
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("数据库驱动加载失败: " + driver);
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("数据库连接失败: " + url);
            System.err.println("错误信息: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // 关闭连接的工具方法
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("数据库连接已关闭");
            } catch (SQLException e) {
                System.err.println("关闭数据库连接失败");
                e.printStackTrace();
            }
        }
    }
}