package com.sdu.irlab.chatlabelling.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Configuration
@Primary
public class DataSourceConfig {
    @Value("${dbUser}")
    private String username;
    @Value("${dbPass}")
    private String password;
    @Value("${dbHost}")
    private String host;
    @Value("${dbPort}")
    private String port;

    private String driverClassName = "com.mysql.cj.jdbc.Driver";
    private String databaseName = "chat_labelling";
    private String timezone = "GMT%2B8";
    private String datasourceUrl;

    @Bean     //声明其为Bean实例
    public DataSource dataSource() {
        try {
            Class.forName(driverClassName);
            // 连接已经存在的数据库，如：mysql
            Connection connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "?serverTimezone=" + timezone, username, password);
            Statement statement = connection.createStatement();
            // 创建数据库
            statement.executeUpdate("create database if not exists `" + databaseName + "` default character set utf8 COLLATE utf8_general_ci");
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        datasourceUrl = "jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?serverTimezone=" + timezone;
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(datasourceUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 500);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.setConnectionTestQuery("SELECT 1");
        config.setAutoCommit(true);
        //池中最小空闲链接数量
        config.setMinimumIdle(10);
        //池中最大链接数量
        config.setMaximumPoolSize(50);
        HikariDataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }
}