# Pac-Man AI Research Implementation

**Authors:** Adam Stafford & Barnabas Fluck

## Overview
This project implements advanced pathfinding algorithms to control Ghost AI in a Java-based Pac-Man engine. We explore how rule-based navigation strategies—specifically those used as benchmarks in UAV swarm control research—can be applied to game agents to create complex, coordinated adversarial behaviors.

## How to Run
To play the game and observe the algorithms in action:

1.  Navigate to **`src/game/GameLauncher.java`**.
2.  Run the `main` method inside that file.

*(Note: The application entry point is `GameLauncher`, not `Game.java`.)*

## Research & Algorithms
This project implements the benchmark strategies discussed in the following research paper:

> **Paper:** *EXPLORING THE POSSIBILITIES OF MADDPG FOR UAV SWARM CONTROL BY SIMULATING IN PAC-MAN ENVIRONMENT*
> **Authors:** Artem Novikov, Sergiy Yakovlev, Ivan Gushchin
> **Published in:** Radioelectronic and Computer Systems, 2025, no. 1(113)
> **DOI:** 10.32620/reks.2025.1.21

[cite_start]While the paper explores Deep Reinforcement Learning, it establishes **A\* Search** and **Breadth-First Search (BFS)** as critical benchmarks for navigation efficiency[cite: 12]. We implemented these specific strategies to control our ghosts:

* [cite_start]**Pinky:** Implements **A\*** to intercept the player[cite: 13, 133].
* [cite_start]**Inky:** Implements a **Hybrid Strategy** (switching between A\* and BFS) to coordinate pincer attacks[cite: 13].
* [cite_start]**Blinky:** Implements **BFS** for optimal pursuit[cite: 12].

 **For a detailed summary of our results and logic, please refer to the project summary PDF located in this repository.**

## 🛠 Credits
* **Original Codebase:** Adapted from the Java Pac-Man engine by **Lucas Vigier**.
* **Research:** Novikov, Yakovlev, and Gushchin for the theoretical framework on adversarial environments.
