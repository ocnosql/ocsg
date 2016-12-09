package com.ai.ocsg.process.conf;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;

/**
 * Created by wangkai8 on 16/8/23.
 */
public class TableConfigruation {

    private static Connection conn;
    private static Configuration conf;

    static {
        conf = HBaseConfiguration.create();
    }

    public static Configuration getConf() {
        return conf;
    }

    public static Connection getConnection() throws IOException {
        if(conn == null || conn.isClosed()) {
            synchronized (conf) {
                if(conn == null || conn.isClosed()) {
                    conn = ConnectionFactory.createConnection(conf);
                }
            }
        }
        return conn;
    }

    public static Table getTable(String tableName) throws IOException {
        Connection conn = getConnection();
        return conn.getTable(TableName.valueOf(tableName));
    }
}
