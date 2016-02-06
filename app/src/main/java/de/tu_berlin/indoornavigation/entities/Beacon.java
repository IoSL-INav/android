package de.tu_berlin.indoornavigation.entities;

/**
 * Created by Jan on 19. 01. 2016.
 * <p/>
 * Entity Beacon that could represent estimote Beacon or Nearable.
 */
public class Beacon {

    // Beacon or Nearable identifiers
    private String proximityUUID;
    private int major;
    private int minor;
    // signal strength
    private int rssi;
    // name for development and testing (e.g. blueBeacon, car, door...)
    private String name;

    public Beacon(String proximityUUID, int major, int minor) {
        this.proximityUUID = proximityUUID;
        this.major = major;
        this.minor = minor;
    }

    public Beacon(String proximityUUID, int major, int minor, int rssi) {
        this.proximityUUID = proximityUUID;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
    }

    public Beacon(String name, String proximityUUID, int major, int minor) {
        this.name = name;
        this.proximityUUID = proximityUUID;
        this.major = major;
        this.minor = minor;
    }

    /**
     * Two Beacons are equal, if they have the same identifiers proximityUUID, major and minor
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {

        Beacon object = (Beacon) o;

        if (this.proximityUUID.toLowerCase().equals(object.proximityUUID.toLowerCase()) && this.major == object
                .getMajor() && this.minor == object.getMinor()) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.minor * this.major;
    }

    public String getProximityUUID() {
        return proximityUUID;
    }

    public void setProximityUUID(String proximityUUID) {
        this.proximityUUID = proximityUUID;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return "Name: " + this.name + " UUID: " + this.proximityUUID + " major: " + this.major + "" +
                " " + "minor: " + this.minor + " rssi: " + this.rssi;
    }
}
