package com.flipkart.foxtrot.core.exception;

import com.flipkart.foxtrot.common.ActionRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rishabh.goyal on 19/12/15.
 */
public class QueryCreationException extends FoxtrotException {

    private ActionRequest actionRequest;

    public QueryCreationException(ActionRequest actionRequest, Throwable cause) {
        super(ErrorCode.MALFORMED_QUERY, cause);
        this.actionRequest = actionRequest;
    }

    public ActionRequest getActionRequest() {
        return actionRequest;
    }

    public void setActionRequest(ActionRequest actionRequest) {
        this.actionRequest = actionRequest;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("request", this.actionRequest);
        map.put("message", this.getCause().getMessage());
        return map;
    }
}
