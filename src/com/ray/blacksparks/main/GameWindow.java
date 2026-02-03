package com.ray.blacksparks.main;

import javax.swing.JFrame;

public class GameWindow {
    public GameWindow(GamePanel gamePanel) {
        JFrame jframe = new JFrame();
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setResizable(false);
        jframe.setTitle("Project BlackSparks");

        jframe.add(gamePanel);
        jframe.pack();

        jframe.setLocationRelativeTo(null);
        jframe.setVisible(true);
    }
}
