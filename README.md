# 🧭 Dungeon Escape and Chase Game

A Java-based simulation game that explores **graph traversal**, **OOP design**, and **pathfinding (BFS & DFS)** in a dungeon setting.
The game features two agents — a **Rogue** and a **Monster** — navigating a grid-based dungeon, where the monster attempts to catch the rogue, and the rogue tries to escape using dynamic strategies based on dungeon structure.



## Features

* Turn-based simulation between Monster and Rogue
* Visualized dungeon display in the terminal
* Dynamic dungeon layout parsing from `.txt` files
* Rogue strategy:

  * Detect and use loops (explicit entrance, hidden, and wall loops)
  * Fallback to pathfinding if no loop is safe
* Monster strategy:

  * BFS with Manhattan-distance optimization to track the rogue
* Supports different speed settings via CLI ('@' represents for rouge, A-Z represents for monster, and '.' is the path that both can go through)


![image](https://github.com/user-attachments/assets/43c1182c-5045-4d20-adb2-00f10a85a188)



## Game Mechanics

* The Rogue and Monster move alternately.
* Movement is restricted to adjacent cells (no diagonal walls).
* Rogue uses loop detection and distance calculations to maximize escape time.
* Monster always tries to take the shortest path, and wanders randomly if none exists.

---

## Technologies Used

| Category       | Stack                                                 |
| -------------- | ----------------------------------------------------- |
| Language       | Java                                                  |
| OOP Principles | Encapsulation, Inheritance, Abstraction, Polymorphism |
| Algorithms     | BFS, DFS, Manhattan Distance                          |
| Environment    | Command-Line Interface / IntelliJ IDEA                |



## 🛠️ How to Run

### 1. Compile the project

```bash
javac Game.java
```

### 2. Run with a dungeon file (in `Dungeons/`)

```bash
java Game Dungeons/111.txt 100
```

* The second parameter `100` controls display speed (ms).
* Default dungeon: `Dungeons/333.txt`.

⚠️ Note: ANSI escape codes used for screen refresh may not work properly in **Windows cmd**. Recommend using Linux/macOS terminal or IntelliJ.



## Folder Structure


project-root/
├── Dungeons/              # Sample dungeon maps (.txt)
├── Game.java              # Main class and loop control
├── Dungeon.java           
├── Site.java             
├── Monster.java / Rogue.java
├── Rule.java / EscapeRule.java / ChaseRule.java
└── README.md
```


