# TypingRaceSimulator

Object Oriented Programming Project — ECS414U

## Project Structure

```
TypingRaceSimulator/
├── Part1/    # Textual simulation (Java, command-line)
└── Part2/    # GUI simulation (to be completed)
```

## Part 1 — Textual Simulation

### How to compile

```bash
cd Part1
javac Typist.java TypingRace.java
```

### How to run

The race is started by calling `startRace()` on a `TypingRace` object.
A simple way to test this is to add a `main` method to `TypingRace`, for example:

```java
public static void main(String[] args) {
    TypingRace race = new TypingRace(40);
    race.addTypist(new Typist('①', "TURBOFINGERS", 0.85), 1);
    race.addTypist(new Typist('②', "QWERTY_QUEEN",  0.60), 2);
    race.addTypist(new Typist('③', "HUNT_N_PECK",   0.30), 3);
    race.startRace();
}
```

Then run:

```bash
java TypingRace
```

## Part 2 — GUI Simulation


### How to compile
```bash
cd Part2
javac Typist2.java TypingGUI.java RaceHistory.java
```

### How to run

The graphical version of TypingRace is started by calling `startRaceGUI()`.
A `main` method, like the one shown below, is already declared in `TypingGUI` that calls `startRaceGUI()`. The `main` method is awaiting to be run.
```java
public static void main(String[] args) {
    startRaceGUI();
}
```

To run:

```bash
java TypingGUI
```


## Dependencies

- Java Development Kit (JDK) 11 or higher
- No external libraries required for Part 1
- Part 2 may use Java Swing (included in standard JDK) or JavaFX
- Developed and test on java version "24.0.2" 2025-07-15

## Notes

- All code should compile and run using standard command-line tools without any IDE-specific configuration.
- The starter code in Part1 was originally written by Ty Posaurus. It contains known issues — finding and fixing them is part of the coursework.
- Part2 TypingGUI and Typist2 classes are adapted ver of both classes from Part1, but is made compatible for the graphical ver.
