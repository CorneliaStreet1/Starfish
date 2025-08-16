package org.graph.engine.util;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.graph.engine.wrapper.GraphNodeWrapper;

import java.util.List;
import java.util.Set;

public class ConvertUtil {

    @SuppressWarnings("rawtypes")
    public static GraphNodeWrapper[] set2Array(Set<GraphNodeWrapper<?, ?>> wrapperSet) {
        if (wrapperSet == null || wrapperSet.size() == 0) {
            return new GraphNodeWrapper[0];
        }
        return wrapperSet.toArray(new GraphNodeWrapper[0]);
    }

    public static List<GraphNodeWrapper<?, ?>> set2List(Set<GraphNodeWrapper<?, ?>> wrapperSet) {
        if (CollectionUtils.isEmpty(wrapperSet)) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(wrapperSet);
    }
}
