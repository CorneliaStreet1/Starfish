package org.graph.engine.operator;

/**
 * 算子接口
 */
@FunctionalInterface
public interface IOperator<Input, Output> {
    /**
     * 自定义OP的默认返回值，比如节点执行异常时
     */
    default Output defaultValue() {
        return null;
    }

    /**
     * 该方法实现OP的具体处理逻辑
     */
    Output execute(Input param) throws Exception;

    /**
     * OP执行前回调
     */
    default void onStart(Input param) {

    }
    /**
     * OP执行成功后回调
     */
    default void onSuccess(Input param, OperatorResult<Output> result) {

    }
    /**
     * OP执行异常后回调
     */
    default void onError(Input param, OperatorResult<Output> result) {
    }
}
