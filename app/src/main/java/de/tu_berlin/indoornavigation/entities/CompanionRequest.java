package de.tu_berlin.indoornavigation.entities;

/**
 * Created by Jan on 2. 02. 2016.
 * <p/>
 * Entity represents companion requests.
 */
public class CompanionRequest {

    private String id;
    private User from;
    private User to;

    public CompanionRequest(String id) {
        this.id = id;
    }

    public CompanionRequest(String id, User from) {
        this.id = id;
        this.from = from;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
    }
}
