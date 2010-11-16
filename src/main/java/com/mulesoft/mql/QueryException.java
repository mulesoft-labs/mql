package com.mulesoft.mql;

public class QueryException extends RuntimeException {

    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryException(String message) {
        super(message);
    }

    public QueryException(Exception e) {
        super(e);
    }

}
