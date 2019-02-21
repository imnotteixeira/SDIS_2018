package com.angelo.Server;

import java.util.HashMap;

public class PlateManager {

    private HashMap<String, String> plates;

    public PlateManager() {
        this.plates = new HashMap<String, String>();
    }

    public String register(String plate, String name) {

        if(this.plates.containsKey(plate)) {
            return "-1";
        }
        this.plates.put(plate, name);

        return String.valueOf(this.plates.size()) + "\n" + plate + " " + name;
    }

    public String lookup(String plate) {

        if(!this.plates.containsKey(plate)) {
            return "-1";
        }


        return String.valueOf(this.plates.size()) + "\n" + plate + " " + this.plates.get(plate);
    }
}
