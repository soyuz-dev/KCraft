# KCraft v0.2 Alpha

> *“I have learnt from my previous mistakes. Unfortunately, I am still me.”*

KCraft is a programmable-computer and automation mod for Minecraft, built with Kotlin and Fabric.

The goal is to provide computers that expose Minecraft functionality through a Kotlin-oriented runtime, allowing players to write programs, automate tasks, control golems, and build larger programmable systems directly inside Minecraft.

Rather than implementing a separate virtual machine, KCraft runs real Kotlin/JVM programs inside Minecraft's JVM through a controlled scripting API.

**Current release: KCraft 0.2 Alpha: *KotlinCraft***

## Computers

The Computer Block is the centre of KCraft's programmable systems.

Each computer has its own server-authoritative runtime and persistent filesystem, with an interactive display accessible in-game.

Computers currently provide:

- A persistent filesystem
- KSh, KCraft's shell and orchestration language
- Pico, KCraft's built-in text editor
- Runtime compilation and execution of Kotlin scripts (`.kts`)
- Concurrent Kotlin processes
- Process-local working directories and environments
- Compiler diagnostics and runtime error reporting
- Controlled access from Kotlin programs to KCraft and Minecraft functionality
- A client/server networking layer for input and display synchronisation

The Minecraft GUI acts primarily as a remote display and input device. Programs, files and computer state live on the server.

Multiple players viewing the same computer interact with the same underlying runtime.

## Kotlin Scripting

Kotlin scripts (`.kts`) are KCraft's main programming environment.

Programs can be written entirely inside Minecraft using Pico:

```text
> pico hello.kts
```

For example:

```kotlin
terminal.println("Hello from Kotlin!")
```

Save the file, exit Pico, and run it:

```text
> run hello.kts
Started process 1
Hello from Kotlin!
```

These are real Kotlin/JVM programs compiled at runtime using Kotlin's embedded scripting compiler.

KCraft does not implement a separate Kotlin-like language or virtual machine.

### KCraft API

Scripts interact with their computer through a deliberately small API.

Currently available capabilities include:

```kotlin
terminal.println("Hello!")

files.write("message.txt", "Hello from Kotlin!")
val message = files.read("message.txt")

val user = env["USER"]
val cwd = env["PWD"]
val pid = env["PID"]

val here = computer.position
val block = world.blockAt(here)

terminal.println(block.id)
```

The current API exposes:

```text
terminal    Terminal output
files       Persistent computer filesystem
env         Process environment
computer    Information about the current computer
world       Controlled access to Minecraft world state
```

Minecraft implementation classes such as `ServerLevel`, `BlockState`, Fabric APIs, KCraft runtime internals and other implementation details are not part of the scripting API.

### Minecraft World Access

Kotlin programs can query the Minecraft world in real time.

For example:

```kotlin
val here = computer.position

val below = KCraftPosition(
    here.x,
    here.y - 1,
    here.z
)

terminal.println(
    world.blockAt(below).id
)
```

`world.blockAt(...)` performs a live query. The returned `KCraftBlock` is an immutable snapshot of the block at that position.

World access is executed through KCraft's server-thread request system rather than exposing Minecraft objects directly to script threads.

This is the foundation for future redstone, inventory, peripheral, networking and golem APIs.

## Processes

Kotlin programs execute as KCraft processes.

Running a script:

```text
> run worker.kts
Started process 4
```

does not block the shell. Multiple programs may run concurrently.

Processes have:

- A PID
- A program path
- A working directory inherited at launch
- An environment inherited at launch
- A lifecycle state
- Independent execution

Running processes can be inspected with:

```text
> ps
PID STATE PROGRAM
1 FINISHED /home/test.kts
4 RUNNING /home/worker.kts
```

and stopped with:

```text
> kill 4
Stopped process 4
```

Current process states include:

```text
STARTING
RUNNING
FINISHED
FAILED
STOPPED
```

Process cancellation currently uses JVM thread interruption and is therefore cooperative; CPU-bound code that ignores interruption may continue running.

## Runtime and Concurrency

Kotlin programs execute away from Minecraft's server thread.

Scripts do not directly manipulate KCraft's filesystem or Minecraft world state from their worker threads. Instead, operations cross a request boundary:

```text
Kotlin process
      ↓
ComputerRequest
      ↓
thread-safe request queue
      ↓
ComputerRuntime tick
      ↓
authoritative operation
      ↓
result
      ↓
Kotlin process
```

Filesystem and world queries use request/response semantics, allowing scripts to use ordinary synchronous-looking Kotlin:

```kotlin
files.write("counter.txt", "5")

val value = files.read("counter.txt")

val block = world.blockAt(
    computer.position
)
```

without exposing the underlying threading model.

Each computer processes a limited number of requests per Minecraft tick so programs cannot force an unlimited amount of host work into a single tick.

## Scripting Classpath

KCraft's scripting API is built as a separate module from the main mod.

Kotlin scripts compile against the KCraft scripting API and Kotlin standard library rather than KCraft's entire implementation classpath.

This prevents scripts from directly depending on implementation classes such as:

```text
ComputerRuntime
KCraftProcessManager
Minecraft ServerLevel
Fabric internals
```

and allows KCraft's implementation to evolve without unnecessarily breaking player programs.

### Security Warning

**KCraft Kotlin scripting is currently trusted-code execution.**

The restricted scripting classpath is an API and compatibility boundary, **not a complete security sandbox**.

Kotlin programs still execute as JVM code inside Minecraft's JVM and currently retain access to JDK functionality. Do not run `.kts` programs from untrusted sources on a server.

Further isolation may be added in future versions.

## KSh

KSh is KCraft's shell and orchestration language.

Despite sharing the `.ksh` extension with KornShell, KSh is unrelated to KornShell.

KSh is not intended to replace Kotlin scripting. Kotlin scripts contain program logic; KSh launches, configures and coordinates programs and manages the computer environment.

Currently implemented commands include:

```text
echo
clear
help
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

run
ps
kill
```

For example:

```sh
source /etc/shell.kshrc
run /home/monitor.kts
```

As KCraft's process system develops, KSh will provide the orchestration layer around larger collections of Kotlin programs.

## Pico

Pico is KCraft's built-in text editor.

The name is intentional: it's smaller than Nano.

Pico allows Kotlin programs, KSh scripts and other text files to be edited directly inside a KCraft computer.

It currently supports:

- Character insertion and deletion
- Multiple lines
- Cursor movement
- Automatic viewport scrolling
- Opening existing files
- Creating new files
- Saving files
- Returning to the terminal

Together with the Kotlin compiler and diagnostics, Pico provides a complete in-game edit-run-debug loop:

```text
pico program.kts
      ↓
save
      ↓
run program.kts
      ↓
compiler/runtime diagnostics
      ↓
pico program.kts
```

## Filesystem

Every computer has its own persistent filesystem.

A new computer is populated from KCraft's bundled `rootfs`:

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
- Appending text
- Creating and deleting files
- Creating and deleting empty directories
- Directory listing
- Absolute and relative paths
- Path normalisation
- Process-local working directories
- Protection against escaping the computer's filesystem root

Each computer has a persistent UUID, allowing its files and programs to survive chunk, world and Minecraft restarts.

## Survival Progression

KCraft computers are obtainable in survival.

Rubies are KCraft's primary computing material and form part of the crafting progression towards computer chips and computers.

Ruby Ore generates naturally in Nether blackstone.

The basic progression is:

```text
Reach the Nether
       ↓
Find Ruby Ore
       ↓
Mine Rubies
       ↓
Craft a Computer Chip
       ↓
Craft a Computer
       ↓
Write (questionable?) Kotlin
```

## Planned Features

KCraft 0.2 Alpha establishes the Kotlin programming and process environment. Future development will expand what programs can control.

Planned systems include:

- Expanded Minecraft world APIs
- Redstone input and output
- Inventories and peripherals
- Programmable golems
- Computer-to-computer networking
- Wireless communication
- Expanded KSh syntax and process management
- Stronger script isolation
- An installable and upgradeable operating environment

### Golems

A major long-term goal is programmable golems with capabilities comparable to Minecraft Education Edition's Agent.

Golems will be controlled by Kotlin programs running on host computers and may communicate wirelessly when equipped with appropriate hardware, provided they remain in ticking chunks.

KSh will provide orchestration around those programs.

## Development

KCraft is written primarily in Kotlin and currently targets:

- Minecraft 26.2
- Fabric
- Fabric Language Kotlin
- Java 25

The project is divided so that the public scripting API is independent of Minecraft and Fabric implementation classes.

Several runtime-independent components, including the filesystem, lexer, and KSh interpreter, are unit tested separately from Minecraft.

## Requirements

KCraft 0.2 Alpha requires:

- Minecraft 26.2
- [Fabric Loader](https://fabricmc.net/)
- [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
- [Fabric Language Kotlin](https://www.curseforge.com/minecraft/mc-mods/fabric-language-kotlin)
- Java 25 (included with Minecraft 26.2)

## Status

**0.2 Alpha.**

KCraft currently provides survival-obtainable persistent computers with a Unix-inspired shell environment, in-game text editor, runtime Kotlin/JVM compilation, concurrent processes, persistent program storage and controlled access to live Minecraft world state.

APIs, filesystem formats, scripts, recipes, world data, networking protocols and basically anything else may change without backwards compatibility.

Do not entrust KCraft with the only copy of anything important.

Or do, for all I care.

Especially don't run random Kotlin from strangers yet.

## License

KCraft is licensed under the Mozilla Public License 2.0 (MPL-2.0).

See `LICENSE` for details.