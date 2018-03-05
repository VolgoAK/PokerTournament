package com.volgoak.pokertournament.data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by alex on 3/5/18.
 */

public class Structure implements Serializable{
    private String name;
    private List<Blind> blinds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Blind> getBlinds() {
        return blinds;
    }

    public void setBlinds(List<Blind> blinds) {
        this.blinds = blinds;
    }

    @Override
    public String toString() {
        return name;
    }
}
