package com.ai.ocsg.process.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wangkai8 on 16/8/24.
 */
public class DateUtil {

    public static final String PARTTERN = "yyyyMM";

    public static String getYearMonth() {
        SimpleDateFormat format = new SimpleDateFormat(PARTTERN);
        return format.format(new Date());
    }
}
