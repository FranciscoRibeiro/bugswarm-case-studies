package com.flipkart.foxtrot.core.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rishabh.goyal on 19/12/15.
 */
public class TableExistsException extends FoxtrotException {

    private String table;

    public TableExistsException(String table) {
        super(ErrorCode.TABLE_ALREADY_EXISTS);
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
        return map;
    }
}
