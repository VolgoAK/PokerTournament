package com.volgoak.pokertournament.utils;

/**
 * Created by alex on 3/6/18.
 */

public class ControlEvent {
    public enum Type {
        CHANGE_STATE, STOP
    }

    public Type type;

    public ControlEvent(Type type) {
        this.type = type;
    }
}
