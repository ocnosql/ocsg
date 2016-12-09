package com.ai.ocsg.process.core;

import com.ai.ocsg.process.utils.DateUtil;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangkai8 on 16/11/2.
 */
public class ResultData {

    private Code code;
    private byte[] data;

    private long startTimeInQueue;
    private long scanCostTime;

    private Throwable throwable;


    public ResultData(Code code) {
        this.code = code;
    }

    public ResultData(Code code, Throwable throwable) {
        this.code = code;
        this.throwable = throwable;
    }


    public ResultData(Code code, byte[] data, long scanCostTime) {
        this.code = code;
        this.data = data;
        this.startTimeInQueue = DateUtil.now();
        this.scanCostTime = scanCostTime;
    }

    public long getScanCostTime() {
        return scanCostTime;
    }

    public void setScanCostTime(long scanCostTime) {
        this.scanCostTime = scanCostTime;
    }

    public long getStartTimeInQueue() {
        return startTimeInQueue;
    }

    public void setStartTimeInQueue(long startTimeInQueue) {
        this.startTimeInQueue = startTimeInQueue;
    }

    public Code getCode() {
        return code;
    }


    public void setCode(Code code) {
        this.code = code;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public long getSize() {
        if(data != null) {
            return data.length;
        }
        return 0;
    }


    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }



    public static enum Code {
        DATA(0),
        FINISHI_FLAG(1),
        ERROR(2);

        private int code;

        private static final Map<Integer,Code> lookup
                = new HashMap<Integer,Code>();


        Code(int code) {
            this.code = code;
        }



        static {
            for(Code c : EnumSet.allOf(Code.class)) {
                lookup.put(c.code, c);
            }
        }

        public static Code getCode(int code) {
            return lookup.get(code);
        }

    }


}
