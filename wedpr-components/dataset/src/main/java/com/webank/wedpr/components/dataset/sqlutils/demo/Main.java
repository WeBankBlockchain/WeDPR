package com.webank.wedpr.components.dataset.sqlutils.demo;

import static com.webank.wedpr.components.dataset.sqlutils.SQLExecutor.generateJdbcUrl;

import com.webank.wedpr.components.dataset.datasource.DBType;
import com.webank.wedpr.components.dataset.datasource.category.DBDataSource;
import com.webank.wedpr.components.dataset.sqlutils.SQLUtils;
import com.webank.wedpr.components.dataset.utils.CsvUtils;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;

public class Main {
    public static void main(String[] args) throws DatasetException {

        if (args.length < 7) {
            System.out.println(
                    "java -cp \"libs\" com.webank.wedpr.components.dataset.sqlutils.demo.Main mysql 127.0.0.1 3306 wedprv3 root xxx \"select * from tb\"");
            System.exit(0);
        }
        System.out.println(args);

        String strDbType = args[0];
        String host = args[1];
        int port = Integer.parseInt(args[2]);
        String database = args[3];
        String user = args[4];
        String password = args[5];
        String sql = args[6];

        System.out.println("\t dbType: " + strDbType);
        System.out.println("\t host: " + host);
        System.out.println("\t port: " + port);
        System.out.println("\t database: " + database);
        System.out.println("\t user: " + user);
        System.out.println("\t password: " + password);
        System.out.println("\t sql: " + sql);

        DBType dbType = DBType.fromStrType(strDbType);

        //        String strDbType = "mysql";
        //        String sql = "select * from wedpr_dataset";
        //        String database = "wedpr3";
        //        String host = "127.0.0.1";
        //        int port = 3306;
        //        String user = "root";
        //        String password = "123456";

        DBDataSource dbDataSource = new DBDataSource();
        dbDataSource.setEncryptionModel(false);
        dbDataSource.setSql(sql);
        dbDataSource.setDatabase(database);
        dbDataSource.setDbIp(host);
        dbDataSource.setDbPort(port);
        dbDataSource.setUserName(user);
        dbDataSource.setPassword(password);

        String jdbcUrl = generateJdbcUrl(dbType, host, port, database, null);

        String resultFile = "./abc_db.csv";
        SQLUtils.validateDataSourceParameters(dbType, dbDataSource);
        CsvUtils.convertDBDataToCsv(jdbcUrl, user, password, sql, resultFile);
    }
}
