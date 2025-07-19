package com.example.config;

/**
 * 用户未登录异常
 * 当需要用户登录才能执行操作但用户未登录时抛出
 */
public class UserNotLoggedInException extends RuntimeException {

    /**
     * 构造一个不带详细消息的UserNotLoggedInException
     */
    public UserNotLoggedInException() {
        super("用户未登录");
    }

    /**
     * 构造一个带详细消息的UserNotLoggedInException
     * @param message 详细消息
     */
    public UserNotLoggedInException(String message) {
        super(message);
    }

    /**
     * 构造一个带详细消息和原因的UserNotLoggedInException
     * @param message 详细消息
     * @param cause 原因
     */
    public UserNotLoggedInException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造一个带原因的UserNotLoggedInException
     * @param cause 原因
     */
    public UserNotLoggedInException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造一个带详细消息、原因、是否启用抑制和是否可写堆栈跟踪的UserNotLoggedInException
     * @param message 详细消息
     * @param cause 原因
     * @param enableSuppression 是否启用抑制
     * @param writableStackTrace 是否可写堆栈跟踪
     */
    protected UserNotLoggedInException(String message, Throwable cause, 
                                     boolean enableSuppression, 
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}