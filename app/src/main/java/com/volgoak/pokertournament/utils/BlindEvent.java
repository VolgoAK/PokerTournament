package com.volgoak.pokertournament.utils;

import com.volgoak.pokertournament.data.model.BlindLegacy;

/**
 * Created by alex on 3/6/18.
 */

public class BlindEvent {
    public BlindLegacy currentBlind;
    public BlindLegacy nextBlind;
    public long millisToNext;

    public boolean active;

    public BlindEvent(BlindLegacy currentBlind, BlindLegacy nextBlind, long timeToNext, boolean active) {
        this.currentBlind = currentBlind;
        this.nextBlind = nextBlind;
        this.millisToNext = timeToNext;
        this.active = active;
    }
}
