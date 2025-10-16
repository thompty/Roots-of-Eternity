package src.main;

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class Sound {
    Clip clip;
    URL soundUrl[] = new URL[30];
    FloatControl fc;
    int volumeScale = 3;
    float volume;

    public Sound() {
        soundUrl[0] = getClass().getResource("/res/sound/BlueBoyAdventure.wav");
        soundUrl[1] = getClass().getResource("/res/sound/coin.wav");
        soundUrl[2] = getClass().getResource("/res/sound/powerup.wav");
        soundUrl[3] = getClass().getResource("/res/sound/unlock.wav");
        soundUrl[4] = getClass().getResource("/res/sound/fanfare.wav");
        soundUrl[5] = getClass().getResource("/res/sound/hitmonster.wav");
        soundUrl[6] = getClass().getResource("/res/sound/receivedamage.wav");
        soundUrl[7] = getClass().getResource("/res/sound/cuttree.wav");
        soundUrl[8] = getClass().getResource("/res/sound/cursor.wav");
        soundUrl[9] = getClass().getResource("/res/sound/burning.wav");
        soundUrl[10] = getClass().getResource("/res/sound/gameover.wav");
    }

    public void setFile(int i) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundUrl[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
            fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            checkVolume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip != null) {
            clip.start();
        } else {
            System.out.println("Clip is not initialized.");
        }
    }

    public void loop() {
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {
        clip.stop();
    }

    public void checkVolume() {
        // Assuming volumeScale is a percentage between 0 and 100
        float minVolume = -30.0f; // Minimum decibel level (silence)
        float maxVolume = 6.0f; // Maximum decibel level (full volume)

        // Convert the percentage (volumeScale) to decibels
        // A value of 0% maps to minVolume and 100% maps to maxVolume
        volume = minVolume + (maxVolume - minVolume) * (volumeScale / 100.0f);
        fc.setValue(volume);
    }
}
