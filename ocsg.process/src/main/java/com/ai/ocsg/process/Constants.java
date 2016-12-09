package com.ai.ocsg.process;

import com.ai.ocsg.process.utils.PropertiesUtil;

/**
 * Created by wangkai8 on 16/8/24.
 */
public class Constants {

    public static final String RESOURCE_NAME = "runtime.properties";

    public static final long FILE_THRESTHOLD = Long.parseLong(PropertiesUtil.getProperty("runtime.properties", "hdfs.upload.threshold", "67108864"));

    public static final String HDFS_UPLOAD_ROOT = "hdfs.upload.root";

    public static final String HBASE_SPLIT_SIZE = "hbase.split.size";

    public static final String HBASE_UPLOAD_TABLE_PREFIX = "hbase.upload.table.prefix";

    public static final String HBASE_SPLIT_RESULT_QUEUE = "hbase.split.result.queue";

    public static final byte[] DATA_FAMILY = "F".getBytes();

    public static final byte[] INFO_FAMILY = "INFO".getBytes();

    public static final byte[] FILE_NAME = "FN".getBytes();

    public static final byte[] FILE_LENGTH = "FL".getBytes();
}
