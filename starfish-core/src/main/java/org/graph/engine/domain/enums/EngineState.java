package org.graph.engine.domain.enums;

import lombok.Getter;

@Getter
public enum EngineState {
    INIT(1),

    RUNNING(2),

    FINISHED(3),

    ERROR(4);

    private final int state;

    EngineState(int state) {
        this.state = state;
    }

}
