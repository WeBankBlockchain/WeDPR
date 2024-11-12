package com.webank.wedpr.components.dataset.sqlutils;

import com.webank.wedpr.components.dataset.datasource.DBType;
import com.webank.wedpr.components.dataset.datasource.category.DBDataSource;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLUtils {

    private static final Logger logger = LoggerFactory.getLogger(SQLUtils.class);

    private SQLUtils() {}

    public static String clearDataSourceField(String fieldName, String dataSourceMeta) {
        return dataSourceMeta.replaceAll(
                "\"" + fieldName + "\\s*\":\\s*\"[^\"]*\"", "\"" + fieldName + "\": \"\"");
    }

    public static String clearDbDataSource(String dataSourceMeta) {

        dataSourceMeta = clearDataSourceField("username", dataSourceMeta);
        dataSourceMeta = clearDataSourceField("password", dataSourceMeta);
        dataSourceMeta = clearDataSourceField("dbIp", dataSourceMeta);
        dataSourceMeta = clearDataSourceField("dbPort", dataSourceMeta);
        dataSourceMeta = clearDataSourceField("database", dataSourceMeta);

        return dataSourceMeta;
    }

    public static void validateDataSourceParameters(DBType dbType, DBDataSource dbDataSource)
            throws DatasetException {

        String dbIp = dbDataSource.getDbIp();
        Integer dbPort = dbDataSource.getDbPort();
        String database = dbDataSource.getDatabase();
        String user = dbDataSource.getUserName();
        String password = dbDataSource.getPassword();
        String sql = dbDataSource.getSql();

        // build jdbc url
        String jdbcUrl = SQLExecutor.generateJdbcUrl(dbType, dbIp, dbPort, database, null);
        validateDataSourceParameters(jdbcUrl, user, password, sql);
    }

    public static void validateDataSourceParameters(
            String jdbcUrl, String user, String password, String sql) throws DatasetException {

        SQLExecutor sqlExecutor = new SQLExecutor();
        // explain sql for test db connectivity and check sql syntax
        sqlExecutor.explainSQL(jdbcUrl, user, password, sql);
    }

    public static void isSingleSelectStatement(String sql, String sqlValidationPattern)
            throws DatasetException {
        if (sql == null) {
            return;
        }

        sql = sql.trim();
        String[] statements = sql.split(";");
        if (statements.length > 1) {
            logger.error("only support single SQL statement, sql: {}", sql);
            throw new DatasetException("only support single SQL statement, sql: " + sql);
        }

        // regular expression for matching a single SELECT statement.
        //        Pattern pattern =
        //                Pattern.compile(
        //                        "^(SELECT.*?)(?<!\\G)(;|$)", Pattern.CASE_INSENSITIVE |
        // Pattern.DOTALL);
        Pattern pattern =
                Pattern.compile(sqlValidationPattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sql);
        // check if it contains only one SELECT statement
        boolean onlySelectStmt = matcher.find() && !matcher.find();
        if (!onlySelectStmt) {
            logger.error(
                    "only support single select SQL statement, sqlValidationPattern: {}, sql: {}",
                    sqlValidationPattern,
                    sql);
            throw new DatasetException("only support single select SQL statement, sql: " + sql);
        }
    }
}
