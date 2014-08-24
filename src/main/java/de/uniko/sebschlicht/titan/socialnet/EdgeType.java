package de.uniko.sebschlicht.titan.socialnet;

public enum EdgeType {

    FOLLOWS("follows"),

    PUBLISHED("published");

    protected String label;

    private EdgeType(
            String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
