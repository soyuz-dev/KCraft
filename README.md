# KCraft

> *“I have learnt from my previous mistakes. Unfortunately, I am still me.”*

KCraft is a programmable-computer and automation mod for Minecraft, built with Kotlin and Fabric.

The goal is to provide computers that expose Minecraft functionality through a Kotlin-oriented runtime, allowing players to write programs, automate tasks, control golems, and build larger programmable systems directly inside Minecraft.

Rather than treating computers as isolated virtual machines, KCraft aims to make them programmable interfaces to the Minecraft world itself.

KCraft is currently in early development.

## Computers

The Computer Block is the centre of KCraft's programmable systems.

Each computer has its own runtime and persistent filesystem, with an interactive terminal accessible in-game.

Internally, computers currently consist of:

- A server-authoritative `ComputerRuntime`
- An interactive terminal with scrollback and cursor support
- A persistent filesystem unique to each computer
- KSh, KCraft's shell and orchestration language
- A client/server networking layer for computer interaction
- A mode system for interactive programs such as the terminal and future text editor

The Minecraft GUI acts primarily as a remote display and input device. Programs, files and computer state live on the server.

## Rubies

Rubies are KCraft's primary computing material.

They are used in the crafting progression towards computer chips and computers, giving programmable systems a survival progression rather than making computers immediately available from common Overworld materials.

Ruby Ore is a Nether ore associated with blackstone. It can be mined to obtain rubies and supports normal ore behaviour including Fortune and Silk Touch.

The intended progression is roughly:

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

Ruby Ore generates naturally in the Nether by replacing blackstone. Generation is intentionally limited to small veins, so finding blackstone-rich areas is an important part of obtaining rubies in survival mode.

## KSh

KSh is KCraft's shell and orchestration language.

Despite sharing the `.ksh` extension with KornShell, KSh is unrelated to KornShell.

KSh is **not intended to be a simpler alternative to Kotlin scripting**. Kotlin scripts contain the actual program logic; KSh exists to launch, configure and coordinate those programs.

Its role is closer to a mixture of a shell script, Makefile and Gradle build script.

Eventually, a KSh program might look something like:

```sh
source /dev/golems.ksh

run /home/storage.kts
run /home/miner.kts

wait

echo "Mining complete."
```

KSh is intended for launching programs, configuring the computer environment, managing files and processes, and connecting larger systems together.

Currently implemented shell functionality includes:

```text
echo
clear
source
touch
mkdir
cd
pwd
append
appendln
```

KSh files can already be created, modified and executed entirely from inside Minecraft. For example:

```sh
touch hello.ksh
appendln hello.ksh "echo Hello, world!"
source hello.ksh
```

produces:

```text
Hello, world!
```

More shell functionality will be added as the runtime develops.

## Kotlin Scripting

Kotlin scripts (`.kts`) are planned to provide the main programming environment for KCraft.

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

The filesystem supports:

- Reading and writing files
- Appending text to files
- File and directory creation
- Absolute and relative paths
- Path normalisation
- Working directories
- Directory listing
- Protection against escaping the computer's filesystem root

Each computer is assigned a persistent UUID, allowing its files to remain associated with that computer across chunk and world reloads.

## Terminal

The terminal is currently the default interactive computer mode.

It supports:

- 12 visible lines
- 128 characters per line
- 64 lines of scrollback
- Editable input independent of terminal history
- Cursor positioning
- Scrolling
- Server-authoritative state synchronisation

Input is sent to the server, processed by the computer's runtime, and the resulting display state is synchronised back to connected clients.

Multiple players viewing the same computer therefore interact with the same underlying runtime.

## Computer Modes

Interactive interfaces in KCraft implement the `ComputerMode` abstraction.

The terminal itself is one computer mode. This allows the runtime to switch between different interactive programs without requiring the Minecraft GUI or networking layer to understand their internal behaviour.

Planned modes include:

- Terminal
- Pico text editor
- Potential future file browsers, process monitors and other interactive programs

The client only needs to send input and display the state produced by the active mode.

## Pico

Pico is KCraft's planned built-in text editor.

It is intended to provide the small subset of editor functionality needed to comfortably write KSh and Kotlin scripts directly inside Minecraft:

- Text insertion and deletion
- Cursor movement
- Multiple lines
- Saving files
- Exiting back to the terminal

The name is intentional: it's smaller than Nano.

## Planned Features

KCraft is still very early in development. Planned systems include:

- `.kts` execution
- Pico, the in-game text editor
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
- Filesystem and path handling
- KSh lexer
- KSh interpreter

Minecraft-specific code is kept primarily around lifecycle, networking, persistence, rendering and interaction with the game itself.

## Status

**Early alpha.**

KCraft can already provide persistent computers with an interactive terminal, filesystem and executable KSh scripts, but much of the larger programming and automation environment is still under development.

APIs, filesystem formats, scripts, recipes, world data, networking protocols and basically anything else may change without backwards compatibility.

Do not entrust KCraft with the only copy of anything important.

## License

KCraft is licensed under the Mozilla Public License 2.0 (MPL-2.0).

See `LICENSE` for details.