package org.graph.engine.domain.exception;

import java.io.Serial;

public class GraphConstructionException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1755365673L;

    /**
     * 无参构造方法
     */
    public GraphConstructionException() {
        super();
    }

    /**
     * 带消息的构造方法
     *
     * @param message 错误消息
     */
    public GraphConstructionException(String message) {
        super(message);
    }

    /**
     * 带消息和原因的构造方法
     *
     * @param message 错误消息
     * @param cause   原始异常
     */
    public GraphConstructionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 带原因的构造方法
     *
     * @param cause 原始异常
     */
    public GraphConstructionException(Throwable cause) {
        super(cause);
    }
}
