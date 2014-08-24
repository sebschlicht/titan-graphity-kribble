package de.uniko.sebschlicht.titan.socialnet;

public enum VertexType {

    UPDATE("update"),

    USER("user");

    protected String label;

    private VertexType(
            String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
