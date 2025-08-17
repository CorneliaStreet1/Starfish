package org.graph.engine;


import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.graph.engine.context.GraphContext;
import org.graph.engine.domain.enums.EngineState;
import org.graph.engine.domain.enums.NodeWrapperState;
import org.graph.engine.domain.enums.ResultState;
import org.graph.engine.util.ConvertUtil;
import org.graph.engine.wrapper.DirectedAcyclicGraphWrapper;
import org.graph.engine.wrapper.GraphNodeWrapper;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class DagScheduler implements IGraphEngine {


    /**
     * 工作线程的快照
     */
    private ConcurrentMap<Thread, GraphNodeWrapper<?, ?>> runningThreadMap = Maps.newConcurrentMap();

    /**
     * 工作线程池
     */
    private ExecutorService executor;


    /**
     * 主线程阻塞等待所有结束节点执行完成
     * 阻塞主线程, 直到计数跌零
     */
    private CountDownLatch syncLatch;

    /**
     * 编排流程的超时时间，单位：毫秒
     */
    private long timeoutInMilliSeconds;

    /**
     * 执行的状态
     */
    private EngineState engineState = EngineState.INIT;


    /**
     * 执行引擎上下文
     */
    private GraphContext graphContext = new GraphContext();

    /**
     * 阻塞主线程，等待流程执行结束，根据依赖关系自动解析出开始节点
     * @param timeoutInMilliSeconds 超时时间，单位：毫秒
     * @return 调度是否成功. true成功
     */
    public boolean runAndWait(long timeoutInMilliSeconds, DirectedAcyclicGraphWrapper dagWrapper) {
        //解析Dag的依赖
        parseNextDepends4DAG(dagWrapper);
        if (CollectionUtils.isEmpty(dagWrapper.getStartNodesWrapperSet())) {
            return false;
        }
        this.timeoutInMilliSeconds = timeoutInMilliSeconds;

        return this.schedule(dagWrapper);
    }

    private boolean schedule(DirectedAcyclicGraphWrapper dagWrapper) {
        try {
            if (dagWrapper.getBeforeDagSchedule() != null) {
                dagWrapper.getBeforeDagSchedule().callback();
            }
            this.engineState = EngineState.RUNNING;
            List<GraphNodeWrapper<?, ?>> beginWrappers = ConvertUtil.set2List(dagWrapper.getStartNodesWrapperSet());
            if (CollectionUtils.isEmpty(beginWrappers)) {
                this.engineState = EngineState.FINISHED;
                return false;
            }

            //设置DAG引擎上下文，上下文的生命周期从开始节点到结束节点之间
            GraphContextHolder.set(graphContext);
            /**
             * 初始化信号量,每个结束节点执行完毕则计数减一
             * 所有的结束节点都执行完毕后, 计数归零, 主线程才解除阻塞
             */
            syncLatch = new CountDownLatch(dagWrapper.getEndNodesWrapperSet().size());

            for (GraphNodeWrapper<?, ?> beginWrapper : beginWrappers) {

            }
        }
        catch (Throwable e) {

        }
    }

    private void runNode(GraphNodeWrapper<?, ?> node, DirectedAcyclicGraphWrapper dagWrapper) {

        if (engineState != EngineState.RUNNING) {
            return;
        }

        /**
         * 判断节点是否可以执行:
         * 1. 准入条件满足(所有节点均是强依赖节点时, 准入条件不起作用,满足2和3即可)
         * 2. 所有强依赖节点均已执行完毕
         * 3. 节点还未执行
         */
        if (node.getCondition() != null && node.getNodeWrapperState().get() == NodeWrapperState.INIT.getState()) {
            synchronized (node.getCondition()) {
                //当前节点可以执行时，中断弱依赖的节点
                if (node.getCondition().test(node)) {
                    // todo this.interruptOrUpdateDependSkipState(wrapper);
                }
                else {
                    /**
                     * 不满足条件,则只有所有强依赖节点执行完毕才可以执行当前节点
                     * 也即任意前置依赖节点还未执行完毕,就不可执行当前节点
                     */
                    Set<GraphNodeWrapper<?, ?>> dependWrappers = node.getDependWrappers();
                    boolean anyDependNodeNotEnd = dependWrappers.stream()
                            .filter(Objects::nonNull)
                            .anyMatch(nodeWrapper -> nodeWrapper.getNodeWrapperState().get() <= NodeWrapperState.RUNNING.getState());
                    if (anyDependNodeNotEnd) {
                        return;
                    }
                }
            }
        }
        //节点状态不等于INIT，说明该节点已经执行过
        if (!node.compareAndSetState(NodeWrapperState.INIT.getState(), NodeWrapperState.RUNNING.getState())) {
            return;
        }

        // 到这里才说明节点是第一次执行
        executor.submit(
                ()-> nodeExecute(node, dagWrapper)
        );
    }

    private void nodeExecute(GraphNodeWrapper<?, ?> node, DirectedAcyclicGraphWrapper dagWrapper) {
        Thread thread = Thread.currentThread();
        try {
            node.setThread(thread);
            this.runningThreadMap.put(thread, node);
            // 执行每个Op执行前的都要执行的回调
            if (dagWrapper.getBeforeEveryOp() != null) {
                dagWrapper.getBeforeEveryOp().call(node);
            }

            // 执行this Op执行前的回调
            if (node.getBeforeThisOp() != null) {
                node.getBeforeThisOp().call(node);
            }

            this.doNodeExecute(node);

            node.compareAndSetState(NodeWrapperState.RUNNING.getState(), NodeWrapperState.FINISH.getState());
            node.getOperatorResult().setResultState(ResultState.SUCCESS);
        }
        catch (Throwable e) {

        }
        finally {

        }
    }




    private void parseNextDepends4DAG(DirectedAcyclicGraphWrapper dagWrapper) {
        if (dagWrapper.isNextDependParsed()) {
            return;
        }

        dagWrapper.setNextDependParsed(true);

        Map<String, GraphNodeWrapper<?, ?>> nodeWrapperMap = dagWrapper.getNodeWrapperMap();
        if (MapUtils.isEmpty(nodeWrapperMap)) {
            return;
        }
        for (Map.Entry<String, GraphNodeWrapper<?, ?>> entry : nodeWrapperMap.entrySet()) {
            GraphNodeWrapper<?, ?> currentNodeWrapper = entry.getValue();
            if (!currentNodeWrapper.isInit()) {
                currentNodeWrapper.init();
            }

            //根据 next 解析依赖关系. 解析当前节点的后继节点
            parseNext4Node(currentNodeWrapper, nodeWrapperMap);

            //根据 depend 解析依赖关系. 解析当前节点的前置依赖节点
            parseDepend4Node(currentNodeWrapper, nodeWrapperMap);
        }

        // 解析整张Dag的开始节点和结束节点. 开始节点是没有前置依赖节点(入度为0)的节点, 结束节点是没有后继节点(出度为0)的节点
        parseStartNodesAndEndNodes(dagWrapper);
    }

    private void parseStartNodesAndEndNodes(DirectedAcyclicGraphWrapper dagWrapper) {
        Map<String, GraphNodeWrapper<?, ?>> nodeWrapperMap = dagWrapper.getNodeWrapperMap();
        if (MapUtils.isEmpty(nodeWrapperMap)) {
            return;
        }

        for (Map.Entry<String, GraphNodeWrapper<?, ?>> entry : nodeWrapperMap.entrySet()) {
            GraphNodeWrapper<?, ?> wrapper = entry.getValue();
            if (CollectionUtils.isEmpty(wrapper.getDependWrappers())) {
                dagWrapper.getStartNodesWrapperSet().add(wrapper);
            }
            if (CollectionUtils.isEmpty(wrapper.getNextWrappers())) {
                dagWrapper.getEndNodesWrapperSet().add(wrapper);
            }
        }
    }

    /**
     * 解析某个节点的后继节点们
     */
    private void parseNext4Node(GraphNodeWrapper<?, ?> currentNodeWrapper, Map<String, GraphNodeWrapper<?, ?>> nodeWrapperMap) {
        if (currentNodeWrapper.isInit()) {
            return;
        }
        //根据当前节点的后继节点, 解析后继依赖关系
        Map<String, Boolean> nextWrapperIdMap = currentNodeWrapper.getNextWrapperIdMap();
        if (MapUtils.isEmpty(nextWrapperIdMap)) {
            return;
        }

        for (Map.Entry<String, Boolean> id2Node : nextWrapperIdMap.entrySet()) {
            String nextId = id2Node.getKey();
            GraphNodeWrapper<?, ?> nextNode = nodeWrapperMap.get(nextId);
            // 后继结点的前置依赖
            if (nextNode.getDependWrappers() == null) {
                nextNode.setDependWrappers(Sets.newHashSet());
            }

            // 当前节点已经在其后继结点的依赖集合中, 不重复加入
            if (CollectionUtils.isNotEmpty(nextNode.getDependWrappers()) && nextNode.getDependWrappers().contains(currentNodeWrapper)) {
                continue;
            }

            // 否则把当前节点加入此后继结点的依赖节点中
            nextNode.getDependWrappers().add(currentNodeWrapper);

            // 后继节点添加到当前节点的后继节点集合中
            if (currentNodeWrapper.getNextWrappers() == null) {
                currentNodeWrapper.setNextWrappers(Sets.newHashSet());
            }
            currentNodeWrapper.getNextWrappers().add(nextNode);

            //将强依赖该节点的后继节点的 indegree+1，弱依赖不需要
            if (nextWrapperIdMap.get(nextId)) {
                nextNode.getIndegree().incrementAndGet();
                if (currentNodeWrapper.getSelfIsMustNextNodeSet() == null) {
                    currentNodeWrapper.setSelfIsMustNextNodeSet(Sets.newHashSet());
                }
                //将后继节点添加到当前节点的强依赖集合
                currentNodeWrapper.getSelfIsMustNextNodeSet().add(nextNode);
            }
        }

    }

    /**
     * 解析某个节点的前置依赖节点们
     */
    private void parseDepend4Node(GraphNodeWrapper<?, ?> currentNodeWrapper, Map<String, GraphNodeWrapper<?, ?>> nodeWrapperMap) {
        if (currentNodeWrapper.isInit()) {
            return;
        }

        //根据 depend 解析依赖关系
        Map<String, Boolean> dependWrapperIdMap = currentNodeWrapper.getDependWrapperIdMap();
        if (MapUtils.isEmpty(dependWrapperIdMap)) {
            return;
        }

        for (String dependId : dependWrapperIdMap.keySet()) {
            GraphNodeWrapper<?, ?> dependNode = nodeWrapperMap.get(dependId);
            if (CollectionUtils.isNotEmpty(currentNodeWrapper.getDependWrappers()) && currentNodeWrapper.getDependWrappers().contains(dependNode)) {
                continue;
            }

            //将前驱节点添加到当前节点的依赖集合中
            if (currentNodeWrapper.getDependWrappers() == null) {
                currentNodeWrapper.setDependWrappers(new HashSet<>());
            }
            currentNodeWrapper.getDependWrappers().add(dependNode);

            //将当前节点添加到前驱节点的后继集合中
            if (dependNode.getNextWrappers() == null) {
                dependNode.setNextWrappers(new HashSet<>());
            }
            dependNode.getNextWrappers().add(currentNodeWrapper);


            //强依赖前驱节点时，当前节点的indegree+1，弱依赖不需要
            if (dependWrapperIdMap.get(dependId)) {
                currentNodeWrapper.getIndegree().incrementAndGet();
                if (dependNode.getSelfIsMustNextNodeSet() == null) {
                    dependNode.setSelfIsMustNextNodeSet(new HashSet<>());
                }
                //将当前节点添加到前驱节点的强依赖集合
                dependNode.getSelfIsMustNextNodeSet().add(currentNodeWrapper);
            }
        }
    }
}
