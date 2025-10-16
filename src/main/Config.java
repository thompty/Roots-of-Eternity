package src.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Config now stores settings in the user's AppData folder on Windows:
 * %APPDATA%/RootsOfEternity/config.txt
 * Falls back to user.home/.RootsOfEternity/config.txt on other OSes.
 */
public class Config {

    GamePanel gamePanel;
    private final File configFile;

    // Constructor for Config
    public Config(GamePanel gamePanel) {
        this.gamePanel = gamePanel;

        String appData = System.getenv("APPDATA");
        File dir;
        if (appData != null && appData.length() > 0) {
            dir = new File(appData, "RootsOfEternity");
        } else {
            // Fallback for non-Windows systems
            dir = new File(System.getProperty("user.home"), ".RootsOfEternity");
        }

        if (!dir.exists()) {
            // try to create the directory; ignore failures (we'll fall back to working dir)
            try {
                dir.mkdirs();
            } catch (Exception e) {
                // ignore
            }
        }

        configFile = new File(dir, "config.txt");
    }

    // Saves the current config to the config file
    public void saveConfig() {
        BufferedWriter writer = null;
        try {
            // Ensure parent exists (best effort)
            File parent = configFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            writer = new BufferedWriter(new FileWriter(configFile));

            // FullScreen
            writer.write(gamePanel.fullScreenOn ? "On" : "Off");
            writer.newLine();

            // Music Volume Scale
            writer.write(String.valueOf(gamePanel.music.volumeScale));
            writer.newLine();

            // Sound Effect Volume Scale
            writer.write(String.valueOf(gamePanel.soundEffect.volumeScale));
            writer.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    /* ignore */ }
            }
        }
    }

    // Loads config from the config file; if missing, will write defaults on disk
    public void loadConfig() {
        if (!configFile.exists()) {
            // No config yet — write defaults so future runs have a file
            saveConfig();
            return;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(configFile));
            String s = reader.readLine();

            // FullScreen
            if (s != null) {
                if (s.equalsIgnoreCase("On")) {
                    gamePanel.fullScreenOn = true;
                } else if (s.equalsIgnoreCase("Off")) {
                    gamePanel.fullScreenOn = false;
                }
            }

            // Music Volume Scale
            s = reader.readLine();
            if (s != null) {
                try {
                    gamePanel.music.volumeScale = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    /* leave default */ }
            }

            // Sound Effect Volume Scale
            s = reader.readLine();
            if (s != null) {
                try {
                    gamePanel.soundEffect.volumeScale = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    /* leave default */ }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    /* ignore */ }
            }
        }
    }
}
