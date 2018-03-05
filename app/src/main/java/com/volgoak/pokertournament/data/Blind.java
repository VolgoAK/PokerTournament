package com.volgoak.pokertournament.data;

import java.io.Serializable;

/**
 * Created by alex on 3/5/18.
 */

public class Blind implements Serializable{
    private int sb;
    private int bb;
    private int ante;

    public int getAnte() {
        return ante;
    }

    public void setAnte(int ante) {
        this.ante = ante;
    }

    public int getSb() {
        return sb;
    }

    public void setSb(int sb) {
        this.sb = sb;
    }

    public int getBb() {
        return bb;
    }

    public void setBb(int bb) {
        this.bb = bb;
    }

    @Override
    public String toString() {
        return String.format("%1$d/%2$d", sb, bb);
    }
}
