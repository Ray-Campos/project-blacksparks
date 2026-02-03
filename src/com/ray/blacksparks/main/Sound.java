package com.ray.blacksparks.main;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class Sound {

    Clip[] clips = new Clip[10];
    URL[] soundURL = new URL[10];

    Clip currentClip;
    int currentID;

    public Sound() {
        soundURL[0] = getClass().getResource("/sound/Blue.wav");
        soundURL[1] = getClass().getResource("/sound/KaikaiKitan.wav");
        soundURL[2] = getClass().getResource("/sound/SpecialZ.wav");
        soundURL[3] = getClass().getResource("/sound/Battle.wav");
        soundURL[4] = getClass().getResource("/sound/Jump.wav");
        soundURL[5] = getClass().getResource("/sound/Hit.wav");
        soundURL[6] = getClass().getResource("/sound/Unlock.wav");
        soundURL[7] = getClass().getResource("/sound/LevelUp.wav");
        soundURL[8] = getClass().getResource("/sound/vic.wav");

        for (int i = 0; i < soundURL.length; i++) {
            if(soundURL[i] != null) {
                loadSound(i);
            }
        }
    }

    private void loadSound(int i) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clips[i] = AudioSystem.getClip();
            clips[i].open(ais);
        } catch(Exception e) {
            System.out.println("could not load id " + i);
        }
    }

    public void setFile(int i) {
        this.currentClip = clips[i];
        this.currentID = i;
    }

    public void play() {
        if(currentClip != null) {
            //System.out.println("playing id " + currentID);
            currentClip.setFramePosition(0);
            currentClip.start();
        }
    }

    public void loop() {
        if(currentClip != null) {
            //System.out.println("looping id " + currentID);
            currentClip.setFramePosition(0);
            currentClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop() {
        if(currentClip != null) {
            //System.out.println("stopping id " + currentID);
            currentClip.stop();
        }
    }

    public boolean isPlaying() {
        if (currentClip != null) {
            return currentClip.isRunning();
        }
        return false;
    }
}