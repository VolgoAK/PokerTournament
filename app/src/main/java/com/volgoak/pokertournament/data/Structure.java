package com.volgoak.pokertournament.data;

/**
 * Created by Volgoak on 30.01.2017.
 */

public class Structure {
    public String name;
    public long id;

    public Structure(String name, long id){
        this.name = name;
        this.id = id;
    }

    @Override
    public String toString(){
        return name;
    }
}
