package de.tu_berlin.indoornavigation;

import java.util.LinkedList;

/**
 * Created by Jan on 16. 01. 2016.
 */
public class Group {
    public boolean autoPing;
    private String id;
    private String name;
    private LinkedList<String> members;

    public Group(String id, String name, boolean autoPing, LinkedList<String> members) {
        this.id = id;
        this.name = name;
        this.autoPing = autoPing;
        this.members = members;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAutoPing() {
        return autoPing;
    }

    public void setAutoPing(boolean autoPing) {
        this.autoPing = autoPing;
    }

    public LinkedList<String> getMembers() {
        return members;
    }

    public void setMembers(LinkedList<String> members) {
        this.members = members;
    }
}
