package com.flit.runtime;

/**
 * Provides the mapping of error code (string status) to the corresponding HTTP status code.
 */
public enum ErrorCode {

    /**
     * The operation was canceled.
     **/
    CANCELED("canceled", 408),

    /**
     * An unknown error occurred. For example, this can be used when handling errors raised by APIs that do not
     * return any error information.
     **/
    UNKNOWN("unknown", 500),

    /**
     * The client specified an invalid argument. This indicates arguments that are invalid regardless of the state
     * of the system (i.e. a malformed file name, required argument, number out of range, etc.).
     **/
    INVALID_ARGUMENT("invalid_argument", 400),

    /**
     * Operation expired before completion. For operations that change the state of the system, this error may be
     * returned even if the operation has completed successfully (timeout).
     **/
    DEADLINE_EXCEEDED("deadline_exceeded", 408),

    /**
     * Some requested entity was not found.
     **/
    NOT_FOUND("not_found", 404),

    /**
     * The requested URL path wasn't routable to a Twirp service and method. This is returned by generated server code
     * and should not be returned by application code (use "not_found" or "unimplemented" instead).
     **/
    BAD_ROUTE("bad_route", 404),

    /**
     * An attempt to create an entity failed because one already exists.
     **/
    ALREADY_EXISTS("already_exists", 409),

    /**
     * The caller does not have permission to execute the specified operation. It must not be used if the caller
     * cannot be identified (use "unauthenticated" instead).
     **/
    PERMISSION_DENIED("permission_denied", 403),

    /**
     * The request does not have valid authentication credentials for the operation.
     **/
    UNAUTHENTICATED("unauthenticated", 401),

    /**
     * Some resource has been exhausted, perhaps a per-user quota, or perhaps the entire file system is out of space.
     **/
    RESOURCE_EXHAUSTED("resource_exhausted", 403),

    /**
     * The operation was rejected because the system is not in a state required for the operation's execution.
     * For example, doing an rmdir operation on a directory that is non-empty, or on a non-directory object, or when
     * having conflicting read-modify-write on the same resource.
     **/
    FAILED_PRECONDITION("failed_precondition", 412),

    /**
     * The operation was aborted, typically due to a concurrency issue like sequencer check
     * failures, transaction aborts, etc.
     **/
    ABORTED("aborted", 409),

    /**
     * The operation was attempted past the valid range. For example, seeking or reading past end of a paginated
     * collection. Unlike "invalid_argument", this error indicates a problem that may be fixed if the system state
     * changes (i.e. adding more items to the collection). There is a fair bit of overlap between "failed_precondition"
     * and "out_of_range". We recommend using "out_of_range" (the more specific error) when it applies so that
     * callers who are iterating through a space can easily look for an "out_of_range" error to detect when they
     * are done.
     **/
    OUT_OF_RANGE("out_of_range", 400),

    /**
     * The operation is not implemented or not supported/enabled in this service.
     **/
    UNIMPLEMENTED("unimplemented", 501),

    /**
     * When some invariants expected by the underlying system have been broken. In other words, something bad
     * happened in the library or backend service. Twirp specific issues like wire and serialization problems are
     * also reported as "internal" errors.
     **/
    INTERNAL("internal", 500),

    /**
     * The service is currently unavailable. This is most likely a transient condition and may be corrected by
     * retrying with a backoff.
     **/
    UNAVAILABLE("unavailable", 503),

    /**
     * The operation resulted in unrecoverable data loss or corruption.
     **/
    DATALOSS("dataloss", 500);

    private final String errorCode;
    private final int httpStatus;

    ErrorCode(String errorCode, int httpStatus) {
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
