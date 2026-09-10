# KCraft

> *“I have learnt from my previous mistakes. Unfortunately, I am still me.”*

KCraft is a programmable-computer and automation mod for Minecraft, built with Kotlin and Fabric.

The goal is to provide computers that expose Minecraft functionality through a Kotlin-oriented runtime, allowing players to write programs, automate tasks, control golems, and build larger programmable systems directly inside Minecraft.

KCraft is currently in early development.

## Computers

The Computer Block is the centre of KCraft's programmable systems.

Each computer has its own runtime and persistent filesystem, with an interactive terminal accessible in-game.

Internally, computers currently consist of:

- A server-authoritative `ComputerRuntime`
- An interactive terminal with scrollback and cursor support
- A persistent filesystem unique to each computer
- KSh, KCraft's shell and orchestration language
- A client/server networking layer for terminal interaction

The Minecraft GUI acts primarily as a remote terminal: programs and computer state live on the server.

## KSh

KSh is KCraft's shell language.

Despite sharing the `.ksh` extension with KornShell, KSh is unrelated to KornShell.

KSh is **not intended to replace Kotlin scripting**. Instead, it acts as an orchestration layer for Kotlin programs, similar in spirit to a shell script, Makefile, or Gradle build script.

For example:

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

KSh will then provide the higher-level orchestration layer around these programs.

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

The filesystem supports absolute and relative paths, path normalisation, working directories, and protection against escaping the computer's filesystem root.

Each computer is assigned a persistent UUID, allowing its files to survive chunk and world reloads.

## Terminal

The terminal currently supports:

- 12 visible lines
- 128 characters per line
- 64 lines of scrollback
- Editable input independent of terminal history
- Cursor positioning
- Scrolling
- Server-authoritative state synchronisation

Multiple clients viewing the same computer are intended to observe the same underlying runtime.

## Planned Features

KCraft is still very early in development. Planned systems include:

- `.kts` execution
- A small in-game text editor inspired by `nano`
- Process management
- Environment variables
- Expanded KSh syntax
- Programmable golems
- Wireless communication
- Peripherals
- Computer-to-computer networking
- Persistent runtime state
- An installable/upgradeable operating environment

### Golems

A major long-term goal is programmable golems with capabilities comparable to Minecraft Education Edition's Agent.

Golems will be controlled by host computers and may communicate wirelessly when equipped with the appropriate hardware, provided they remain in ticking chunks.

## Development

KCraft is written primarily in Kotlin and currently targets:

- Minecraft 26.2
- Fabric
- Fabric Language Kotlin
- Java 25

Several runtime-independent components—including the terminal, filesystem, lexer, and KSh interpreter—are unit tested separately from Minecraft.

## Status

**Early alpha.**

APIs, filesystem formats, scripts, recipes, world data, and basically anything else may change without backwards compatibility.

Do not entrust KCraft with the only copy of anything important.

## License

MPL 2.0