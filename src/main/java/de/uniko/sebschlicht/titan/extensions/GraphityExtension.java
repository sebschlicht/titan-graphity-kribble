package de.uniko.sebschlicht.titan.extensions;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

    protected static final String KEY_CONF_ID = "sourceId";

    protected static final String KEY_RESPONSE_VALUE = "responseValue";

    protected static final String EXT_NAMESPACE = "graphity";

    protected static final int NUM_MAX_RETRIES = 10;

    protected static byte SOURCE_ID = 0;

    protected static int LAST_MUID_CREATION_TIME = 0;

    protected static AtomicInteger LAST_MUID_ID = new AtomicInteger(0);

    protected TitanGraphity graphity;

    protected String name;

    protected GraphityExtension(
            String name) {
        this.name = name;
    }

    @Override
    public boolean isConfigurationValid(
            ExtensionConfiguration extensionConfiguration) {
        boolean valid = false;
        if (extensionConfiguration != null) {
            Map<String, String> map =
                    extensionConfiguration.tryGetMapFromConfiguration();
            valid =
                    map != null && !map.isEmpty()
                            && map.containsKey(KEY_CONF_ID);
        }
        return valid;
    }

    protected TitanGraphity getGraphityInstance(
            RexsterResourceContext context,
            TitanGraph graph) {
        if (graphity != null) {
            return graphity;
        }
        if (SOURCE_ID == 0) {
            ExtensionConfiguration configuration =
                    context.getRexsterApplicationGraph()
                            .findExtensionConfiguration(EXT_NAMESPACE, name);
            Map<String, String> map =
                    configuration.tryGetMapFromConfiguration();
            String sId = map.get(KEY_CONF_ID);
            if (sId == null) {
                throw new IllegalStateException(
                        "Rexster \""
                                + EXT_NAMESPACE
                                + "\" extension configuration is missing field \""
                                + KEY_CONF_ID
                                + "\": Please provide an unique identifier for this node.");
            }
            byte id = Byte.valueOf(sId);
            setSourceId(id);
        }
        graphity = new WriteOptimizedGraphity(graph);
        graphity.init();
        return graphity;
    }

    protected static void setSourceId(byte sourceId) {
        SOURCE_ID = sourceId;
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
