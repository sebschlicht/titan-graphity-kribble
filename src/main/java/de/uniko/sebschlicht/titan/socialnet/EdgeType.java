package de.uniko.sebschlicht.titan.socialnet;

public enum EdgeType {

    FOLLOWS("e_follows"),

    PUBLISHED("e_published");

    protected String label;

    private EdgeType(
            String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
