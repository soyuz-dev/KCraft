# KCraft

> *“I have learnt from my previous mistakes. Unfortunately, I am still me.”*

KCraft is a programmable-computer and automation mod for Minecraft, built with Kotlin and Fabric.

The goal is to provide computers that expose Minecraft functionality through a Kotlin-oriented runtime, allowing players to write programs, automate tasks, control golems, and build larger programmable systems directly inside Minecraft.

Rather than treating computers as isolated virtual machines, KCraft aims to make them programmable interfaces to the Minecraft world itself.

**Current release: KCraft 0.1 Alpha — *It computes!***

## Computers

The Computer Block is the centre of KCraft's programmable systems.

Each computer has its own server-authoritative runtime and persistent filesystem, with an interactive display accessible in-game.

Internally, computers currently consist of:

- A server-authoritative `ComputerRuntime`
- An interactive terminal with history, scrolling and cursor editing
- A persistent filesystem unique to each computer
- KSh, KCraft's shell and orchestration language
- Pico, KCraft's tiny built-in text editor
- A generic `ComputerMode` system for interactive programs
- A client/server networking layer for input and display synchronisation

The Minecraft GUI acts primarily as a remote display and input device. Programs, files and computer state live on the server.

Multiple players viewing the same computer interact with the same underlying runtime.

## Survival Progression

KCraft computers are obtainable in survival.

Rubies are KCraft's primary computing material and form part of the crafting progression towards computer chips and computers.

Ruby Ore generates naturally in the Nether by replacing blackstone. It generates in small veins, making blackstone-rich areas the primary place to search for rubies.

Ruby Ore supports normal Minecraft ore behaviour, including Fortune, Silk Touch and experience drops.

The basic progression is:

```text
Reach the Nether
       ↓
Find Ruby Ore in blackstone
       ↓
Mine Rubies
       ↓
Craft a Computer Chip
       ↓
Craft a Computer
       ↓
Start programming
```

## KSh

KSh is KCraft's shell and orchestration language.

Despite sharing the `.ksh` extension with KornShell, KSh is unrelated to KornShell.

KSh is **not intended to be a simpler alternative to Kotlin scripting**. Kotlin scripts are intended to contain actual program logic; KSh exists to launch, configure and coordinate those programs.

Its role is closer to a mixture of a shell script, Makefile and Gradle build script.

Eventually, a KSh program might look something like:

```sh
source /dev/golems.ksh

run /home/storage.kts
run /home/miner.kts

wait

echo "Mining complete."
```

KSh already provides a small Unix-like environment for interacting with the computer.

Currently implemented commands include:

```text
echo
clear
source

pwd
cd
ls
cat

touch
mkdir
rmdir
rm

append
appendln

pico
```

KSh supports both absolute and relative paths, with relative paths resolved against the computer's current working directory.

Scripts can be created, edited and executed entirely inside Minecraft.

For example:

```sh
touch hello.ksh
appendln hello.ksh "echo Hello, world!"
source hello.ksh
```

produces:

```text
Hello, world!
```

Or, somewhat more comfortably:

```text
> pico hello.ksh
```

Write:

```sh
echo "Hello from Pico!"
```

save, exit, and then:

```text
> source hello.ksh
Hello from Pico!
```

## Pico

Pico is KCraft's built-in text editor.

The name is intentional: it's smaller than Nano.

Pico allows scripts and other text files to be edited directly inside a KCraft computer. It currently supports:

- Character insertion and deletion
- Multiple lines
- Cursor movement
- Automatic viewport scrolling
- Opening existing files
- Creating new files
- Saving files
- Returning to the terminal

Pico runs as a `ComputerMode`, meaning it uses the same generic input and display system as the terminal rather than requiring a separate Minecraft GUI.

## Kotlin Scripting

Kotlin scripts (`.kts`) are planned to provide KCraft's main programming environment.

Unlike mods that implement an entirely separate virtual computer, KCraft intends to expose controlled abstractions over the Minecraft runtime directly to Kotlin programs.

Kotlin scripts will eventually be able to interact with:

- Golems
- Inventories
- Blocks and the world
- Peripherals
- Networking
- Other computers

KSh will provide the higher-level orchestration layer around these programs.

In short:

```text
.kts → program logic
.ksh → program orchestration
```

`.kts` execution is **not implemented in 0.1 Alpha**.

## Filesystem

Every computer has its own persistent filesystem.

A new computer is populated from KCraft's bundled `rootfs`, currently structured around familiar Unix-like conventions:

```text
/
├── bin/
├── dev/
├── etc/
│   └── shell.kshrc
├── home/
└── tmp/
```

The filesystem currently supports:

- Reading and writing files
- Appending text to files
- Creating and deleting files
- Creating and deleting empty directories
- Directory listing
- Absolute and relative paths
- Path normalisation
- Working directories
- Protection against escaping the computer's filesystem root

Each computer is assigned a persistent UUID, allowing its files to remain associated with that computer across chunk and world reloads.

## Terminal

The terminal is KCraft's default interactive computer mode.

It currently supports:

- 12 visible lines
- 128 characters per line
- 64 lines of scrollback
- Editable input independent of terminal history
- Cursor positioning and editing
- Scrolling
- Server-authoritative state synchronisation

Input is sent to the server, processed by the computer's runtime, and the resulting display state is synchronised back to connected clients.

## Computer Modes

Interactive interfaces in KCraft implement the `ComputerMode` abstraction.

Currently implemented modes are:

```text
Terminal
Pico
```

A mode receives semantic computer input and produces a generic display state consisting primarily of text and cursor information.

This means the Minecraft client does not need to understand the application running on the computer:

```text
Minecraft keyboard
       ↓
semantic input
       ↓
ComputerRuntime
       ↓
active ComputerMode
       ↓
display state
       ↓
Minecraft screen
```

Future interactive programs can therefore be added without requiring an entirely new client/server GUI architecture.

## Planned Features

KCraft 0.1 Alpha establishes the basic computer environment. Future development is intended to expand what those computers can actually control and automate.

Planned systems include:

- `.kts` execution
- Process management
- Expanded environment variables
- Expanded KSh syntax
- Programmable golems
- Wireless communication
- Peripherals
- Computer-to-computer networking
- Persistent runtime state
- An installable and upgradeable operating environment

### Golems

A major long-term goal is programmable golems with capabilities comparable to Minecraft Education Edition's Agent.

Golems will be controlled by host computers and may communicate wirelessly when equipped with the appropriate hardware, provided they remain in ticking chunks.

The intention is for Kotlin scripts to contain the actual golem behaviour while KSh coordinates larger groups of programs and machines.

For example:

```sh
source /dev/golems.ksh

run /home/miner.kts miner-01
run /home/miner.kts miner-02
run /home/storage.kts warehouse

wait

echo "Mining operation complete."
```

## Development

KCraft is written primarily in Kotlin and currently targets:

- Minecraft 26.2
- Fabric
- Fabric Language Kotlin
- Java 25

Several runtime-independent components are unit tested separately from Minecraft, including:

- Terminal
- Pico
- Filesystem and path handling
- KSh lexer
- KSh interpreter

Minecraft-specific code is kept primarily around lifecycle, networking, persistence, rendering, world generation and interaction with the game itself.

## Requirements

KCraft 0.1 Alpha requires:

- Minecraft 26.2
- Fabric Loader
- Fabric API
- Fabric Language Kotlin
- Java 25

## Status

**0.1 Alpha — It computes!**

KCraft currently provides survival-obtainable, persistent computers with an interactive terminal, filesystem, KSh scripting environment and in-game text editor.

The larger Kotlin programming and automation environment is still under development.

APIs, filesystem formats, scripts, recipes, world data, networking protocols and basically anything else may change without backwards compatibility.

Do not entrust KCraft with the only copy of anything important.

Or do, for all I care.

## License

KCraft is licensed under the Mozilla Public License 2.0 (MPL-2.0).

See `LICENSE` for details.