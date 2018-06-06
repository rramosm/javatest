package com.prueba;

public class JobLoggerException extends Exception {

    private static final long serialVersionUID = -799956346239073266L;

    public JobLoggerException(String msg) {
        super(msg);
    }

    public JobLoggerException(String msg, Throwable nested) {
        super(msg, nested);
    }

}