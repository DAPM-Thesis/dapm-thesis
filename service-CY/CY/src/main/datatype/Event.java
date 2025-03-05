package main.datatype;

import main.Attribute;

import java.util.Collection;
import java.util.HashSet;

public class Event extends DataType {
    private final String caseID;
    private final String activity;
    private final String timestamp;
    private final Collection<Attribute<?>> attributes;

    public Event(String caseID, String activity, String timestamp, HashSet<Attribute<?>> attributes) {
        this.caseID = caseID;
        this.activity = activity;
        this.timestamp = timestamp;
        this.attributes = attributes;
    }

    public String getCaseID() {return caseID;}

}
