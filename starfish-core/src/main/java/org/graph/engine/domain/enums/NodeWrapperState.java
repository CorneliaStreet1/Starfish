package org.graph.engine.domain.enums;

import lombok.Data;


public enum NodeWrapperState {
    /**
     * 初始状态
     */
     INIT(0),
    /**
     * 执行中
     */
    RUNNING(1),
    /**
     * 执行结束
     */
    FINISH(2),
    /**
     * 节点执行异常
     */
    ERROR(3),
    /**
     * 跳过当前节点
     */
    SKIP(4);
    private final int state;

    NodeWrapperState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
