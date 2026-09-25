package com.interview.lld.retry;

public class HttpException extends RuntimeException {

    private final int statusCode;
    private final Long retryAfterMillis;

    public HttpException(int statusCode, String message) {
        this(statusCode, message, null);
    }

    public HttpException(
            int statusCode,
            String message,
            Long retryAfterMillis) {

        super(message);
        this.statusCode = statusCode;
        this.retryAfterMillis = retryAfterMillis;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Long getRetryAfterMillis() {
        return retryAfterMillis;
    }
}