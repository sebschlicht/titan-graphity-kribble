package de.uniko.sebschlicht.titan.extensions;

import java.util.Map;

import org.apache.log4j.Logger;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.rexster.RexsterResourceContext;
import com.tinkerpop.rexster.extension.AbstractRexsterExtension;
import com.tinkerpop.rexster.extension.ExtensionConfiguration;

import de.uniko.sebschlicht.graphity.titan.TitanGraphity;
import de.uniko.sebschlicht.graphity.titan.impl.ReadOptimizedGraphity;
import de.uniko.sebschlicht.graphity.titan.impl.WriteOptimizedGraphity;

public abstract class GraphityExtension extends AbstractRexsterExtension {

    protected static Logger LOG = Logger.getLogger(GraphityExtension.class);

    protected static final String EXT_NAMESPACE = "graphity";

    protected static final String KEY_CONF_SOURCE_ID = "sourceId";

    protected static final String KEY_CONF_ALGORITHM = "algorithm";

    protected static final String KEY_RESPONSE_VALUE = "responseValue";

    protected static final int NUM_MAX_RETRIES = 0;

    protected static byte SOURCE_ID = 0;

    protected static boolean IS_READ_OPTIMIZED_GRAPHITY;

    protected TitanGraphity graphity = null;

    protected RuntimeException buildConfigException(String key, String message) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("[config:");
        errorMessage.append(key);
        errorMessage.append("] ");
        errorMessage.append(message);
        return new IllegalStateException(errorMessage.toString());
    }

    protected TitanGraphity getGraphityInstance(
            RexsterResourceContext context,
            TitanGraph graph) {
        if (graphity != null) {
            return graphity;
        }
        // read source id from Rexster extension configuration
        if (SOURCE_ID == 0) {
            ExtensionConfiguration configuration =
                    context.getRexsterApplicationGraph()
                            .findExtensionConfiguration(EXT_NAMESPACE,
                                    EXT_NAMESPACE);
            Map<String, String> map =
                    configuration.tryGetMapFromConfiguration();
            String sSourceId = map.get(KEY_CONF_SOURCE_ID);
            if (sSourceId == null) {
                RuntimeException e =
                        buildConfigException(KEY_CONF_SOURCE_ID,
                                "Source id is missing!");
                LOG.error(e.getMessage());
                throw e;
            } else {
                SOURCE_ID = Byte.valueOf(sSourceId);
                LOG.info("[config] source id set to " + SOURCE_ID);
                TitanGraphity.setTitanId(SOURCE_ID);
            }
            String sAlgorithm = map.get(KEY_CONF_ALGORITHM);
            if ("read-optimized".equalsIgnoreCase(sAlgorithm)) {
                IS_READ_OPTIMIZED_GRAPHITY = true;
            } else if ("write-optimized".equalsIgnoreCase(sAlgorithm)) {
                IS_READ_OPTIMIZED_GRAPHITY = false;
            } else {
                RuntimeException e =
                        buildConfigException(KEY_CONF_ALGORITHM,
                                "Invalid algorithm identifier! Use \"read-optimized\" or \"write-optimized\".");
                LOG.error(e.getMessage());
                throw e;
            }
        }
        graphity =
                IS_READ_OPTIMIZED_GRAPHITY
                        ? new ReadOptimizedGraphity(graph)
                        : new WriteOptimizedGraphity(graph);
        graphity.init();
        return graphity;
    }
}
