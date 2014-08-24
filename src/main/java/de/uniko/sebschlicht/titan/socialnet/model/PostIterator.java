package de.uniko.sebschlicht.titan.socialnet.model;

import java.util.Iterator;

public interface PostIterator extends Iterator<StatusUpdateProxy> {

    long getCrrPublished();
}
