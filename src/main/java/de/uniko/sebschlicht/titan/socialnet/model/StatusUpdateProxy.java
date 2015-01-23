package de.uniko.sebschlicht.titan.socialnet.model;

import java.util.concurrent.atomic.AtomicInteger;

import com.tinkerpop.blueprints.Vertex;

import de.metalcon.domain.Muid;
import de.metalcon.domain.UidType;
import de.metalcon.domain.helper.UnknownMuidException;
import de.metalcon.exceptions.MetalconRuntimeException;
import de.metalcon.exceptions.ServiceOverloadedException;
import de.uniko.sebschlicht.socialnet.StatusUpdate;

public class StatusUpdateProxy extends SocialItemProxy {

    /**
     * date and time the activity was published
     */
    public static final String PROP_PUBLISHED = "published";

    /**
     * content message
     */
    public static final String PROP_MESSAGE = "message";

    /**
     * timestamp of publishing
     */
    protected long published;

    /**
     * author (proxy to user vertex)
     */
    protected UserProxy pAuthor;

    public StatusUpdateProxy(
            Vertex vStatusUpdate) {
        super(vStatusUpdate);
    }

    public boolean init() {
        setIdentifier(generateStatusUpdateIdentifier());
        return true;
    }

    public void setAuthor(UserProxy pAuthor) {
        this.pAuthor = pAuthor;
    }

    /**
     * @return cached timestamp of publishing
     */
    public long getPublished() {
        if (published == 0) {
            //TODO catch return value null
            published = vertex.getProperty(PROP_PUBLISHED);
        }
        return published;
    }

    public void setPublished(long published) {
        vertex.setProperty(PROP_PUBLISHED, published);
        this.published = published;
    }

    public String getMessage() {
        return (String) vertex.getProperty(PROP_MESSAGE);
    }

    public void setMessage(String message) {
        vertex.setProperty(PROP_MESSAGE, message);
    }

    public StatusUpdate getStatusUpdate() {
        return new StatusUpdate(String.valueOf(pAuthor.getIdentifier()),
                getPublished(), getMessage());
    }

    protected static final AtomicInteger NEXT_TYPE = new AtomicInteger(0);

    protected static long generateStatusUpdateIdentifier() {
        int muidType = NEXT_TYPE.getAndIncrement();
        while (muidType == 10 || muidType > 13) {
            if (muidType > 13) {
                NEXT_TYPE.set(0);
            }
            muidType = NEXT_TYPE.getAndIncrement();
        }
        try {
            return Muid.create(UidType.parseShort((short) muidType)).getValue();
        } catch (UnknownMuidException e) {// should not happen, but hey...
            return generateStatusUpdateIdentifier();
        } catch (ServiceOverloadedException e) {// we are too fast (>12k/s)
            return generateStatusUpdateIdentifier();
        } catch (MetalconRuntimeException e) {// muidType == 10 -> is URL
            return generateStatusUpdateIdentifier();
        }
    }
}
