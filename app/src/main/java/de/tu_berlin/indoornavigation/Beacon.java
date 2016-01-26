package de.tu_berlin.indoornavigation;

/**
 * Created by Jan on 19. 01. 2016.
 */
public class Beacon {

    private String companyUUID;
    private int major;
    private int minor;
    private int rssi;
    private String name;

    public Beacon(String companyUUID, int major, int minor) {
        this.companyUUID = companyUUID;
        this.major = major;
        this.minor = minor;
    }

    public Beacon(String companyUUID, int major, int minor, int rssi) {
        this.companyUUID = companyUUID;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
    }

    public Beacon(String name, String companyUUID, int major, int minor) {
        this.name = name;
        this.companyUUID = companyUUID;
        this.major = major;
        this.minor = minor;
    }

    @Override
    public boolean equals(Object o) {

        Beacon object = (Beacon) o;

        if (this.companyUUID.toLowerCase().equals(object.companyUUID.toLowerCase()) && this.major == object
                .getMajor() && this.minor == object.getMinor()) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.minor * this.major;
    }

    public String getCompanyUUID() {
        return companyUUID;
    }

    public void setCompanyUUID(String companyUUID) {
        this.companyUUID = companyUUID;
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
        return "Name: " + this.name + " UUID: " + this.companyUUID + " major: " + this.major + "" +
                " " + "minor: " + this.minor + " rssi: " + this.rssi;
    }
}
