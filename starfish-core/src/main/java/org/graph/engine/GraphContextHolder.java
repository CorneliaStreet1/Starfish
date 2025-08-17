package org.graph.engine;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.graph.engine.context.GraphContext;
import org.graph.engine.operator.OperatorResult;

public class GraphContextHolder {

    private static ThreadLocal<GraphContext> holder = new TransmittableThreadLocal<>();

    protected static void set(GraphContext dagContext) {
        holder.set(dagContext);
    }

    public static GraphContext get() {
        return holder.get();
    }

    protected static void remove() {
        holder.remove();
    }


    public static void putOperatorResult(String wrapperId, OperatorResult<?> operatorResult) {
        holder.get().putOperatorResult(wrapperId, operatorResult);
    }

    public static OperatorResult<?> getOperatorResult(String wrapperId) {
        return holder.get().getOperatorResult(wrapperId);
    }

}
