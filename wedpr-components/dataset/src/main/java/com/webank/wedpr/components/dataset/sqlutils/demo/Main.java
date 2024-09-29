package com.webank.wedpr.components.dataset.sqlutils.demo;

import com.webank.wedpr.components.dataset.datasource.DBType;
import com.webank.wedpr.components.dataset.datasource.category.DBDataSource;
import com.webank.wedpr.components.dataset.sqlutils.SQLExecutor;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;

public class Main {
    public static void main(String[] args) throws DatasetException {

        String sql = "select * from wedpr_dataset";
        String database = "wedpr3";
        String host = "127.0.0.1";
        int port = 3306;
        String user = "root";
        String password = "123456";

        DBDataSource dbDataSource = new DBDataSource();
        dbDataSource.setSql(sql);
        dbDataSource.setDatabase(database);
        dbDataSource.setDbIp(host);
        dbDataSource.setDbPort(port);
        dbDataSource.setUserName(user);
        dbDataSource.setPassword(password);

        SQLExecutor sqlExecutor = new SQLExecutor();
        sqlExecutor.executeSQL(
                DBType.MYSQL,
                dbDataSource,
                (fields, rowValues) -> {
                    System.out.println(fields);
                    System.out.println(rowValues);
                });
    }
}
