package src.main;

import javax.swing.JFrame;

public class Main {

    public static JFrame window;

    public static void main(String[] args) {
        // Create a new window in the middle of the screen
        window = new JFrame("Game");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Roots of Eternity");

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        gamePanel.config.loadConfig();
        if (gamePanel.fullScreenOn == true) {
            window.setUndecorated(true);
        }

        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        // Autosave on close: write save before exiting
        window.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    String used;
                    if (gamePanel.currentSaveName == null || gamePanel.currentSaveName.isBlank()) {
                        used = gamePanel.saveManager.saveAndReturnName(null);
                        gamePanel.currentSaveName = used;
                    } else {
                        gamePanel.saveManager.save(gamePanel.currentSaveName);
                        used = gamePanel.currentSaveName;
                    }
                    System.out.println("Auto-saved to " + used);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        gamePanel.setupGame();
        gamePanel.startGameThread();
    }
}

// 2686 lines of code on 9/18/2024
// 3447 lines of code on 9/25/2024