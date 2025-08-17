package org.graph.engine.wrapper;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.graph.engine.callback.IDagCallback;
import org.graph.engine.callback.IOperatorCallback;

import java.util.Map;
import java.util.Set;

/**
 * 有向无环图包装类
 */
@Data
@Accessors(chain = true)
public class DirectedAcyclicGraphWrapper {


    /**
     * 每个OP执行前的回调
     */
    private IOperatorCallback beforeEveryOp;
    /**
     * 每个OP执行后的回调
     */
    private IOperatorCallback afterEveryOp;

    /**
     * DAG引擎执行前回调
     */
    private IDagCallback beforeDagSchedule;


    /**
     * 开始节点结合
     */
    private Set<GraphNodeWrapper<?, ?>> startNodesWrapperSet = Sets.newHashSet();

    /**
     * 结束节点集合
     * 引擎执行过程中可以根据节点执行情况动态设置结束节点，需要使用线程安全的集合
     */
    private Set<GraphNodeWrapper<?, ?>> endNodesWrapperSet =  Sets.newConcurrentHashSet();


    /**
     * DAG节点之间的依赖关系是否已经解析
     */
    private boolean nextDependParsed = false;

    /**
     * wrapper集合
     * key: wrapperId
     */
    @Nonnull
    private Map<String, GraphNodeWrapper<?, ?>> nodeWrapperMap = Maps.newHashMap();


    public DirectedAcyclicGraphWrapper() {

    }
}
