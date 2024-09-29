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

    public static void validateDataSourceParameters(DBType dbType, DBDataSource dbDataSource)
            throws DatasetException {
        SQLExecutor sqlExecutor = new SQLExecutor();
        sqlExecutor.explainSQL(dbType, dbDataSource);
    }

    public static void isSingleSelectStatement(String sql) throws DatasetException {
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
        // TODO: make it configurable
        Pattern pattern =
                Pattern.compile(
                        "^(SELECT.*?)(?<!\\G)(;|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sql);
        // check if it contains only one SELECT statement
        boolean onlySelectStmt = matcher.find() && !matcher.find();
        if (!onlySelectStmt) {
            logger.error("only support single select SQL statement, sql: {}", sql);
            throw new DatasetException("only support single select SQL statement, sql: " + sql);
        }
    }
}
