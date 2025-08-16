package org.graph.engine.operator;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.graph.engine.domain.enums.ResultState;


@Data
@AllArgsConstructor
public class OperatorResult<Output> {

    /**
     * 算子执行的结果
     */
    private Output result;

    /**
     * 结果状态
     */
    private ResultState resultState;

    /**
     * 异常信息
     */
    private Throwable ex;

    public OperatorResult(Output result, ResultState resultState) {
        this(result, resultState, null);
    }
    @Override
    public String toString() {
        return "OperatorResult{" +
                "result=" + result +
                ", resultState=" + resultState +
                ", ex=" + ex +
                '}';
    }

}
