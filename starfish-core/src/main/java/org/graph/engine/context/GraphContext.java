package org.graph.engine.context;

import org.graph.engine.operator.OperatorResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GraphContext {

    /**
     * 保存每个Operator返回的结果
     */
    private Map<String /* wrapperId */, OperatorResult<?>> operatorResultMap = new ConcurrentHashMap<>();

    public GraphContext() {

    }

    public void putOperatorResult(String wrapperId, OperatorResult<?> operatorResult) {
        operatorResultMap.put(wrapperId, operatorResult);
    }

    public OperatorResult<?> getOperatorResult(String wrapperId) {
        return operatorResultMap.get(wrapperId);
    }
}
