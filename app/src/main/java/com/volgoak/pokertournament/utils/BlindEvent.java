package com.volgoak.pokertournament.utils;

import com.volgoak.pokertournament.data.Blind;

/**
 * Created by alex on 3/6/18.
 */

public class BlindEvent {
    public Blind currentBlind;
    public Blind nextBlind;
    public long millisToNext;

    public boolean active;

    public BlindEvent(Blind currentBlind, Blind nextBlind, long timeToNext, boolean active) {
        this.currentBlind = currentBlind;
        this.nextBlind = nextBlind;
        this.millisToNext = timeToNext;
        this.active = active;
    }
}
