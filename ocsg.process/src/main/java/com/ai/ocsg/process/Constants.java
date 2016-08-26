package com.ai.ocsg.process;

/**
 * Created by wangkai8 on 16/8/24.
 */
public class Constants {

    public static final String HDFS_UPLOAD_ROOT = "hdfs.upload.root";

    public static final String HBASE_SPLIT_SIZE = "hbase.split.size";

    public static final String HBASE_UPLOAD_TABLE_PREFIX = "hbase.upload.table.prefix";

    public static final byte[] DATA_FAMILY = "F".getBytes();

    public static final byte[] INFO_FAMILY = "INFO".getBytes();

    public static final byte[] FILE_NAME = "FN".getBytes();

    public static final byte[] FILE_LENGTH = "FL".getBytes();
}
