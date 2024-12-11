package com.webank.wedpr.components.dataset.sqlutils;

import com.alibaba.druid.util.JdbcUtils;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import com.webank.wedpr.components.dataset.datasource.DBType;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLExecutor {

    private static final Logger logger = LoggerFactory.getLogger(SQLExecutor.class);

    public static final String JDBC_URL_TEMPLATE =
            "jdbc:%s://%s:%d/%s?serverTimezone=GMT%%2B8&characterEncoding=UTF-8&connectTimeout=60000&socketTimeout=60000";

    @FunctionalInterface
    public interface ExecutorCallback {
        void onReadRowData(List<String> fields, List<String> rowValues) throws DatasetException;
    }

    /**
     * generate jdbc url
     *
     * @param dbType
     * @param dbIp
     * @param dbPort
     * @param database
     * @param extraParams
     * @return
     */
    public static String generateJdbcUrl(
            DBType dbType,
            String dbIp,
            Integer dbPort,
            String database,
            Map<String, String> extraParams) {

        String jdbcProtocol = dbType.getJdbcProtocol();
        StringBuilder stringBuilder =
                new StringBuilder(
                        String.format(JDBC_URL_TEMPLATE, jdbcProtocol, dbIp, dbPort, database));

        if (extraParams != null && !extraParams.isEmpty()) {
            for (Entry<String, String> valueEntry : extraParams.entrySet()) {
                String key = valueEntry.getKey();
                String value = valueEntry.getValue();

                stringBuilder.append("&");
                stringBuilder.append(key);
                stringBuilder.append("=");
                stringBuilder.append(value);
            }
        }

        logger.info("generate jdbc url, db type: {}, url: {}", dbType, stringBuilder);

        return stringBuilder.toString();
    }

    /**
     * load class driver
     *
     * @param url
     * @throws DatasetException
     */
    public static void initializeJdbcDriver(String url) throws DatasetException {
        String driverClassName = null;
        try {
            driverClassName = JdbcUtils.getDriverClassName(url);
            logger.info("driver class name: {}, url: {}", driverClassName, url);
            Class.forName(driverClassName);
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("cannot find the driver class: {}", driverClassName);
            throw new DatasetException("cannot find the driver class:  " + driverClassName);
        } catch (Exception e) {
            logger.error("initialize jdbc driver Exception, e: ", e);
            throw new DatasetException("initialize jdbc driver Exception, e: " + e.getMessage());
        }
    }

    // explain sql for test db connectivity and check sql syntax
    public void explainSQL(String jdbcUrl, String user, String password, String sql)
            throws DatasetException {

        String explainSql = "EXPLAIN " + sql;

        SQLExecutor.initializeJdbcDriver(jdbcUrl);

        try (Connection connection = DriverManager.getConnection(jdbcUrl, user, password);
                PreparedStatement preparedStatement = connection.prepareStatement(explainSql)) {

            /*
            preparedStatement.execute();

            ResultSetMetaData metaData = preparedStatement.getMetaData();
            ResultSet resultSet = preparedStatement.getResultSet();

             query fields list
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {

                if (logger.isDebugEnabled()) {
                    logger.debug("id: {}", resultSet.getInt("id"));
                    logger.debug("select_type: {}", resultSet.getString("select_type"));
                    logger.debug("table: {}", resultSet.getString("table"));
                    logger.debug("type: {}", resultSet.getString("type"));
                    logger.debug("possible_keys: {}", resultSet.getString("possible_keys"));
                    logger.debug("key: {}", resultSet.getString("key"));
                    logger.debug("key_len: {}", resultSet.getString("key_len"));
                    logger.debug("ref: {}", resultSet.getString("ref"));
                    logger.debug("rows: {}", resultSet.getInt("rows"));
                    logger.debug("Extra: {}", resultSet.getString("Extra"));
                }

                logger.info("---------------------------------");
            }
            */

            logger.info(
                    "execute explain sql success, url: {}, sql: {}, preparedStatement: {}",
                    jdbcUrl,
                    sql,
                    preparedStatement);

        } catch (SQLSyntaxErrorException sqlSyntaxErrorException) {
            logger.error(
                    "sql syntax error, url: {}, sql: {}, e: ",
                    jdbcUrl,
                    sql,
                    sqlSyntaxErrorException);
            throw new DatasetException("sql syntax error, sql: " + sql);
        } catch (CommunicationsException communicationsException) {
            logger.error(
                    "connect to db server error, url: {}, sql: {}, e: ",
                    jdbcUrl,
                    sql,
                    communicationsException);
            throw new DatasetException("connect to db server error, sql: " + sql);
        } catch (SQLException sqlException) {
            logger.error(
                    "execute explain sql SQLException, url: {}, sql: {}, e: ",
                    jdbcUrl,
                    sql,
                    sqlException);
            throw new DatasetException(
                    "execute explain sql SQLException, e: " + sqlException.getMessage());
        } catch (Exception e) {
            logger.error("execute explain sql Exception, url: {}, sql: {}, e: ", jdbcUrl, e);
            throw new DatasetException("execute explain sql Exception, e: " + e.getMessage());
        }
    }

    public void executeSQL(
            String jdbcUrl, String user, String password, String sql, ExecutorCallback callback)
            throws DatasetException {
        long startTimeMillis = System.currentTimeMillis();

        // load jdbc driver class
        initializeJdbcDriver(jdbcUrl);

        logger.info("try to execute sql, url: {}, sql: {}", jdbcUrl, sql);

        try (Connection connection = DriverManager.getConnection(jdbcUrl, user, password);
                PreparedStatement preparedStatement =
                        connection.prepareStatement(
                                sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {

            // set stream query
            //            preparedStatement.setFetchSize(Integer.MIN_VALUE);

            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();

            List<String> fieldList = getQueryResultColumnList(metaData);
            callback.onReadRowData(fieldList, null);
            int columnCount = fieldList.size();

            int rowCount = 0;
            while (resultSet.next()) {
                rowCount++;
                int columnIndex = 0;
                List<String> rowDataList = new ArrayList<>();
                while (columnIndex++ < columnCount) {
                    String columnStringValue = resultSet.getString(columnIndex);

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "columnIndex: {}, columnValue: {}", columnIndex, columnStringValue);
                    }

                    rowDataList.add(columnStringValue);
                }

                callback.onReadRowData(fieldList, rowDataList);
            }

            long endTimeMillis = System.currentTimeMillis();

            logger.info(
                    "execute sql success, url: {}, sql: {}, columnCount: {}, rowCount: {}, cost(ms): {}",
                    jdbcUrl,
                    sql,
                    columnCount,
                    rowCount,
                    endTimeMillis - startTimeMillis);

        } catch (SQLException sqlException) {
            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "execute sql SQLException, url: {}, sql: {}, cost(ms): {}, e: ",
                    jdbcUrl,
                    sql,
                    (endTimeMillis - startTimeMillis),
                    sqlException);
            throw new DatasetException("execute sql SQLException, e: " + sqlException.getMessage());
        } catch (Exception e) {
            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "execute sql Exception, url: {}, sql: {}, cost(ms): {}, e: ",
                    jdbcUrl,
                    (endTimeMillis - startTimeMillis),
                    e);
            throw new DatasetException("execute sql Exception, e: " + e.getMessage());
        }
    }

    public List<String> getQueryResultColumnList(ResultSetMetaData metaData) throws SQLException {

        List<String> columnList = new ArrayList<>();
        // query fields list
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            logger.info(" {}. columnName: {}", i, columnName);
            columnList.add(columnName);
        }

        return columnList;
    }
}
