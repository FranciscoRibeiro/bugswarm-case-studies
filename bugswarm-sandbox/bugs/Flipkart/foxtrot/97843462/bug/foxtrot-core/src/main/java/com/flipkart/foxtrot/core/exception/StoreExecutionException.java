package com.flipkart.foxtrot.core.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rishabh.goyal on 19/12/15.
 */
public class StoreExecutionException extends FoxtrotException {

    private String table;

    public StoreExecutionException(String table, Throwable cause) {
        super(ErrorCode.STORE_EXECUTION_ERROR, cause);
        this.table = table;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("table", this.table);
        map.put("message", this.getCause().getMessage());
        return map;
    }
}
