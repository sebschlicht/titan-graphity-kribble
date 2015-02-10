package de.uniko.sebschlicht.titan.extensions;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.rexster.RexsterResourceContext;
import com.tinkerpop.rexster.extension.AbstractRexsterExtension;
import com.tinkerpop.rexster.extension.ExtensionConfiguration;

import de.metalcon.domain.Muid;
import de.metalcon.domain.UidType;
import de.metalcon.domain.helper.UidConverter;
import de.metalcon.exceptions.ServiceOverloadedException;
import de.uniko.sebschlicht.titan.graphity.TitanGraphity;
import de.uniko.sebschlicht.titan.graphity.WriteOptimizedGraphity;

public abstract class GraphityExtension extends AbstractRexsterExtension {

    protected static Logger LOG = Logger.getLogger(GraphityExtension.class);

    protected static final String EXT_NAMESPACE = "graphity";

    protected static final String KEY_CONF_SOURCE_ID = "sourceId";

    protected static final String KEY_RESPONSE_VALUE = "responseValue";

    protected static final int NUM_MAX_RETRIES = 0;

    protected static byte SOURCE_ID = 0;

    protected static int LAST_MUID_CREATION_TIME = 0;

    protected static AtomicInteger LAST_MUID_ID = new AtomicInteger(0);

    protected TitanGraphity graphity = null;

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
            SOURCE_ID = Byte.valueOf(sSourceId);
            LOG.info("source id set to " + SOURCE_ID);
        }
        graphity = new WriteOptimizedGraphity(graph);
        graphity.init();
        return graphity;
    }

    public static Muid generateMuid(UidType type)
            throws ServiceOverloadedException {
        int timestamp = (int) (System.currentTimeMillis() / 1000);
        short id = 0;
        if (timestamp == LAST_MUID_CREATION_TIME) {
            if (LAST_MUID_ID.intValue() == UidConverter.getMaximumMuidID()) {
                // more than 65535 MUIDs / second
                // we could separate this number by type
                throw new ServiceOverloadedException(
                        "Alreay created more then "
                                + UidConverter.getMaximumMuidID()
                                + " during the current second");
            }
            id = (short) LAST_MUID_ID.incrementAndGet();
        } else {
            LAST_MUID_ID.set(0);
            LAST_MUID_CREATION_TIME = timestamp;
        }
        return Muid.createFromID(UidConverter.calculateMuidWithoutChecking(
                type.getRawIdentifier(), SOURCE_ID, timestamp, id));
    }
}
