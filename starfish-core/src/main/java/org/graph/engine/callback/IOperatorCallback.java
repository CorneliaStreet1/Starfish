package org.graph.engine.callback;

import org.graph.engine.wrapper.GraphNodeWrapper;

public interface IOperatorCallback {

    void call(GraphNodeWrapper<?, ?> wrapper);

}
