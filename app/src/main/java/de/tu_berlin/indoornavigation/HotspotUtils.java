package de.tu_berlin.indoornavigation;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Jan on 1. 12. 2015.
 */
public class HotspotUtils {

    private static final String[] hotspots = new String[]{"MAR", "TEL"};

    public static ArrayList<String> getHotspots() {
        return new ArrayList<>(Arrays.asList(hotspots));
    }
}
