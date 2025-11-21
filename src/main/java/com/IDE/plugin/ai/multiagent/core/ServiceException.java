package com.IDE.plugin.ai.multiagent.core;

/**
 * Custom exception for service-related errors
 */
public class ServiceException extends Exception {
    private final ErrorCode errorCode;
    private final String serviceName;
    
    public ServiceException(String message) {
        this(message, ErrorCode.GENERAL_ERROR, null);
    }
    
    public ServiceException(String message, Throwable cause) {
        this(message, ErrorCode.GENERAL_ERROR, null, cause);
    }
    
    public ServiceException(String message, ErrorCode errorCode) {
        this(message, errorCode, null);
    }
    
    public ServiceException(String message, ErrorCode errorCode, String serviceName) {
        super(message);
        this.errorCode = errorCode;
        this.serviceName = serviceName;
    }
    
    public ServiceException(String message, ErrorCode errorCode, String serviceName, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.serviceName = serviceName;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Error codes for different types of service exceptions
     */
    public enum ErrorCode {
        GENERAL_ERROR(1000, "General service error"),
        SERVICE_NOT_FOUND(1001, "Service not found"),
        SERVICE_UNAVAILABLE(1002, "Service unavailable"),
        SERVICE_INITIALIZATION_ERROR(1003, "Service initialization error"),
        SERVICE_CONFIGURATION_ERROR(1004, "Service configuration error"),
        SERVICE_DEPENDENCY_ERROR(1005, "Service dependency error"),
        SERVICE_TIMEOUT(1006, "Service operation timeout"),
        SERVICE_PERMISSION_DENIED(1007, "Service permission denied"),
        SERVICE_INVALID_INPUT(1008, "Invalid input to service"),
        SERVICE_INVALID_STATE(1009, "Service in invalid state"),
        SERVICE_RESOURCE_EXHAUSTED(1010, "Service resource exhausted");
        
        private final int code;
        private final String description;
        
        ErrorCode(int code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ServiceException[");
        sb.append("errorCode=").append(errorCode.name());
        sb.append(", code=").append(errorCode.getCode());
        if (serviceName != null) {
            sb.append(", service=").append(serviceName);
        }
        sb.append(", message=").append(getMessage());
        sb.append("]");
        return sb.toString();
    }
}