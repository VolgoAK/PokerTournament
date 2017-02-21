package com.volgoak.pokertournament;

/**
 * Interface for bind Tournament activity
 * to BlindService
 */

 interface BlindTimer {
    boolean changeState();
    void stop();
}
