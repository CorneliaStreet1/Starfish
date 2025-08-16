package org.graph.engine.wrapper;


import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.graph.engine.domain.exception.GraphConstructionException;
import org.graph.engine.operator.IOperator;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dag的节点包装类
 * 一个节点是一个算子的实例以及其他的上下文信息
 * 多个节点可以是同一个算子的实例化, 但是这些节点的Id必须唯一
 */
@Data
@Accessors(chain = true)
public class GraphNodeWrapper<P, V> {

    // todo equals方法override一下

    /**
     * 强依赖于该OP的后续wrapper集合，是nextWrappers的子集
     */
    private Set<GraphNodeWrapper<?, ?>> selfIsMustNextNodeSet;

    /**
     * 节点的入度，不同于常规的定义，这里的入度只计算强依赖的节点，当 indegree=0 时，当前OP才能执行，在一个编排流程中，一定满足如下条件
     * indegree <= dependWrappers.size()
     */
    private AtomicInteger indegree = new AtomicInteger(0);


    /**
     * 该wrapper具体要执行的目标OP
     */
    private IOperator<P, V> operator;

    /**
     * 该wrapper的id，默认是Operator的全限定名
     */
    private String id;

    /**
     * 与这个Op绑定的DAG
     */
    private DirectedAcyclicGraphWrapper graph;


    /**
     * 是否已经初始化，只初始化一次
     */
    private boolean init;

    /**
     * 依赖该OP的后续OP集合id
     * 也即该OP的后继OP集合
     */
    private Map<String /*nextWrapperId*/, Boolean /*后续节点是否强依赖该节点*/> nextWrapperIdMap;

    /**
     * 该OP依赖的OP集合id
     * 也即该OP的前置依赖OP集合
     */
    private Map<String /*dependWrapperId*/, Boolean /*当前节点是否强依赖前置节点*/> dependWrapperIdMap;


    /**
     * 该OP前置依赖的OP集合
     */
    private Set<GraphNodeWrapper<?, ?>> dependWrappers;

    /**
     * 依赖该OP的后续OP集合
     */
    private Set<GraphNodeWrapper<?, ?>> nextWrappers;


    public GraphNodeWrapper<P, V> init() {
        if (init) {
            return this;
        }
        this.init = true;
        return this;
    }

    public GraphNodeWrapper<P, V> graph(DirectedAcyclicGraphWrapper graphWrapper) {
        if (graphWrapper == null) {
            throw new GraphConstructionException("DAG is null");
        }
        if (StringUtils.isBlank(id)) {
            throw new GraphConstructionException("id is null");
        }

        if (graphWrapper.getNodeWrapperMap().containsKey(id)) {
            throw new GraphConstructionException("id duplicates");
        }

        graphWrapper.getNodeWrapperMap().put(id, this);
        this.graph = graphWrapper;
        return this;
    }

    public GraphNodeWrapper<P, V> operator(IOperator<P, V> operator) {
        this.operator = operator;
        if (StringUtils.isBlank(this.id)) {
            this.id = operator.getClass().getCanonicalName();
        }
        return this;
    }

    public GraphNodeWrapper<P, V> nextNodes(GraphNodeWrapper<?, ?>... wrappers) {
        if (wrappers == null) {
            return this;
        }
        for (GraphNodeWrapper<?, ?> wrapper : wrappers) {
            if (wrapper == null) {
                throw new GraphConstructionException("wrapper is null");
            }
            if (StringUtils.isBlank(wrapper.getId())) {
                throw new GraphConstructionException("input wrapper's id is null");
            }
            this.nextNode(wrapper.getId(), true);
        }
        return this;
    }

    public GraphNodeWrapper<P, V> nextNodes(String ... operatorWrapperIds) {
        if (operatorWrapperIds == null) {
            return this;
        }
        for (String wrapperId : operatorWrapperIds) {
            if (StringUtils.isBlank(wrapperId)) {
                throw new GraphConstructionException("operatorWrapper id is blank");
            }
            this.nextNode(wrapperId, true);
        }
        return this;
    }

    public GraphNodeWrapper<P, V> nextNode(GraphNodeWrapper<?, ?> graphNodeWrapper, boolean selfIsMust) {

        if (graphNodeWrapper == null) {
            throw new GraphConstructionException("operatorWrapper is null");
        }
        if (StringUtils.isBlank(graphNodeWrapper.getId())) {
            throw new GraphConstructionException("operatorWrapper id is blank");
        }

        return this.nextNode(graphNodeWrapper.getId(), selfIsMust);
    }

    public GraphNodeWrapper<P, V> nextNode(String wrapperId, boolean selfIsMust) {
        if (wrapperId == null) {
            return this;
        }
        if (nextWrapperIdMap == null) {
            nextWrapperIdMap = new HashMap<>();
        }
        nextWrapperIdMap.put(wrapperId, selfIsMust);
        return this;
    }

    public GraphNodeWrapper<P, V> dependNodes(GraphNodeWrapper<?, ?>... graphNodeWrappers) {
        if (graphNodeWrappers == null) {
            return this;
        }
        for (GraphNodeWrapper<?, ?> wrapper : graphNodeWrappers) {
            if (wrapper == null) {
                throw new GraphConstructionException("operatorWrapper is null");
            }
            if (StringUtils.isBlank(wrapper.getId())) {
                throw new GraphConstructionException("operatorWrapper id is blank");
            }
            this.dependNode(wrapper.getId(), true);
        }
        return this;
    }


    public GraphNodeWrapper<P, V> dependNodes(String ... wrapperIds) {
        if (wrapperIds == null) {
            return this;
        }
        for (String wrapperId : wrapperIds) {
            this.dependNode(wrapperId, true);
        }
        return this;
    }

    public GraphNodeWrapper<P, V> dependNode(GraphNodeWrapper<?, ?> graphNodeWrapper, boolean isMust) {
        if (graphNodeWrapper == null) {
            throw new GraphConstructionException("operatorWrapper is null");
        }
        if (StringUtils.isBlank(graphNodeWrapper.getId())) {
            throw new GraphConstructionException("operatorWrapper id is blank");
        }
        return this.dependNode(graphNodeWrapper.getId(), isMust);
    }

    public GraphNodeWrapper<P, V> dependNode(String wrapperId, boolean isMust) {
        if (wrapperId == null) {
            return this;
        }
        if (dependWrapperIdMap == null) {
            dependWrapperIdMap = new HashMap<>();
        }
        dependWrapperIdMap.put(wrapperId, isMust);
        return this;
    }
}
