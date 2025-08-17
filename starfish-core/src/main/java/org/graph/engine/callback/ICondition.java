package org.graph.engine.callback;

import org.graph.engine.wrapper.GraphNodeWrapper;

@FunctionalInterface
public interface ICondition {

    boolean test(GraphNodeWrapper<?, ?> nodeWrapper);
}
