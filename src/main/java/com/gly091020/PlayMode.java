package com.gly091020;

import net.minecraft.text.Text;

public enum PlayMode {
    LOOP,SEQUENTIAL,RANDOM;
    public Text getName(){
        return Text.translatable("button.net_music_list." + this.name().toLowerCase());
    }
    public PlayMode getNext() {
        switch (this) {
            case LOOP -> {
                return SEQUENTIAL;
            }
            case SEQUENTIAL -> {
                return RANDOM;
            }
            case RANDOM -> {
                return LOOP;
            }
        }
        return LOOP;
    }

    public static PlayMode getMode(Integer i){
        switch (i){
            case 0 -> {
                return LOOP;
            }
            case 1 -> {
                return SEQUENTIAL;
            }
            case 2 -> {
                return RANDOM;
            }
        }
        return LOOP;
    }
}