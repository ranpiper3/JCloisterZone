package com.jcloisterzone.ui;

import com.jcloisterzone.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Config config;

    private final Map<String, Clip> resourceSounds = new HashMap<String, Clip>();

    AudioManager(Config config) {
        this.config = config;
    }

    private BufferedInputStream loadResourceAsStream(String filename) throws IOException {
        BufferedInputStream resourceStream = new BufferedInputStream(FxClient.class.getClassLoader().getResource(filename).openStream());
        return resourceStream;
    }

    /*
     * Load and play sound clip from resources by filename.
     */
    private void playResourceSound(String resourceFilename) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        // Load sound if necessary.
        if (!resourceSounds.containsKey(resourceFilename)) {
            BufferedInputStream resourceStream = loadResourceAsStream(resourceFilename);
            Clip loadedClip = loadSoundFromStream(resourceStream);
            resourceSounds.put(resourceFilename, loadedClip);
        }

        Clip clip = resourceSounds.get(resourceFilename);

        // Stop before starting, in case it plays rapidly (haven't tested).
        clip.stop();

        // Always start from the beginning
        clip.setFramePosition(0);
        clip.start();
    }

    /*
     * Pre-load sound clip so it can play from memory.
     */
    private Clip loadSoundFromStream(BufferedInputStream inputStream)
            throws UnsupportedAudioFileException, IOException,
            LineUnavailableException {
        AudioInputStream audioInputStream = AudioSystem
                .getAudioInputStream(inputStream);

        // Auto-detect file format.
        AudioFormat format = audioInputStream.getFormat();
        DataLine.Info info = new DataLine.Info(Clip.class, format);

        Clip clip = (Clip) AudioSystem.getLine(info);
        clip.open(audioInputStream);

        // Don't need the stream anymore.
        audioInputStream.close();

        return clip;
    }

    public void playSound(String resourceFilename) {
        try {
            playResourceSound(resourceFilename);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    void beep() {
        if (config.getBeep_alert()) {
            playSound("audio/beep.wav");
        }
    }
}
