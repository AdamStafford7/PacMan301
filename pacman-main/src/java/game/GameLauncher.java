/*
 * -----------------------------------------------------------------------------------
 * PROJECT: Pac-Man Program 3
 * AUTHORS: Adam Stafford, Barnabas Fluck
 * DATE: 5 December 2025
 * PURPOSE: Implementation of Pac-Man game with ghosts using various strategies based on research paper exploring MADDPG for UAV swarm control.
 * -----------------------------------------------------------------------------------
 * CITATION:
 * Logic implemented based on the paper:
 * "EXPLORING THE POSSIBILITIES OF MADDPG FOR UAV SWARM CONTROL BY SIMULATING IN PAC-MAN ENVIRONMENT"
 * Authors: Artem Novikov, Sergiy Yakovlev, Ivan Gushchin
 * Published in: Radioelectronic and Computer Systems, 2025, no. 1(113)
 * DOI: 10.32620/reks.2025.1.21
 * * Implemented Algorithms:
 * - [cite_start]Pinky: A* Search [cite: 12, 133]
 * - [cite_start]Inky: Hybrid Strategy (BFS + Vector) [cite: 13]
 * -----------------------------------------------------------------------------------
 */

package game;

import java.io.IOException;
import javax.swing.*;

//Point d'entrée de l'application
public class GameLauncher {
    private static UIPanel uiPanel;

    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setTitle("Pacman");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel gameWindow = new JPanel();

        //Création de la "zone de jeu"
        try {
            gameWindow.add(new GameplayPanel(448,496));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Création de l'UI (pour afficher le score)
        uiPanel = new UIPanel(256,496);
        gameWindow.add(uiPanel);

        window.setContentPane(gameWindow);
        window.setResizable(false);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    public static UIPanel getUIPanel() {
        return uiPanel;
    }
}
