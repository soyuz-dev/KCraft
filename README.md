# KCraft v0.2 Alpha

> *"I have learnt from my previous mistakes. Unfortunately, I am still
> me."*

KCraft is a programmable-computer and automation mod for Minecraft,
built with Kotlin and Fabric.

The goal is to provide computers that expose Minecraft functionality
through a Kotlin-oriented runtime, allowing players to write programs,
automate tasks, control physical workers, and build larger programmable
systems directly inside Minecraft.

Rather than implementing a separate virtual machine, KCraft runs real
Kotlin/JVM programs inside Minecraft's JVM through a deliberately
limited scripting API.

**Current release: KCraft 0.2 Alpha: *KotlinCraft***

**Development: KCraft 0.3 Preview**

The 0.3 preview on `main` is a development snapshot rather than a stable
release. Features, APIs, behaviour, recipes and world data may change
freely before 0.3 Alpha.

## 0.3 Preview

Post-0.2 development has moved KCraft beyond the computer itself and
into the Minecraft world.

Current additions include:

-   Four-direction analogue redstone input and output
-   Ruby Golems as persistent physical workers associated with a parent
    computer
-   A custom Ruby Golem model, texture and GeckoLib animation system
-   Procedural idle attention towards nearby entities
-   Animated pathfinding with dedicated walk-start, continuous-walk and
    walk-end states
-   Kotlin-side discovery and movement control of deployed Ruby Golems
-   `UnreachablePositionException` when a requested movement target
    cannot be reached
-   Ruby Golem Chassis items that convert Allays into Ruby Golem items
-   Preservation of an Allay's custom name through item form and
    deployment
-   A dedicated persistent Ruby Golem slot in each computer
-   Deployment of the stored Ruby Golem into the world

The current Ruby Golem lifecycle is:

``` text
Allay
  +
Ruby Golem Chassis
        ↓
Ruby Golem item
        ↓
Computer golem slot
        ↓
Deploy
        ↓
Ruby Golem entity
        ↓
Kotlin control
```

Recall back into item form is planned but not yet implemented.

## Computers

The Computer Block is the centre of KCraft's programmable systems.

Each computer has its own server-authoritative runtime and persistent
filesystem, with an interactive display accessible in-game.

Computers currently provide:

-   A persistent filesystem
-   KSh, KCraft's shell and orchestration language
-   Pico, KCraft's built-in text editor
-   Runtime compilation and execution of Kotlin scripts (`.kts`)
-   Concurrent Kotlin processes
-   Process-local working directories and environments
-   Compiler diagnostics and runtime error reporting
-   Controlled access from Kotlin programs to KCraft and Minecraft
    functionality
-   Four-direction redstone I/O
-   A persistent Ruby Golem slot
-   Deployment and control of an associated Ruby Golem
-   A client/server networking layer for input, display and computer
    actions

The Minecraft GUI acts primarily as a remote display and input device.
Programs, files, inventory state and the computer runtime remain
server-authoritative.

Multiple players viewing the same computer interact with the same
underlying runtime.

## Kotlin Scripting

Kotlin scripts (`.kts`) are KCraft's main programming environment.

Programs can be written entirely inside Minecraft using Pico:

``` text
> pico hello.kts
```

For example:

``` kotlin
terminal.println("Hello from Kotlin!")
```

Save the file, exit Pico, and run it:

``` text
> run hello.kts
Started process 1
Hello from Kotlin!
```

These are real Kotlin/JVM programs compiled at runtime using Kotlin's
embedded scripting compiler.

KCraft does not implement a separate Kotlin-like language or virtual
machine.

### KCraft API

Scripts interact with their computer through a deliberately small API.

``` kotlin
terminal.println("Hello!")

files.write("message.txt", "Hello from Kotlin!")
val message = files.read("message.txt")

val user = env["USER"]
val cwd = env["PWD"]
val pid = env["PID"]

val here = computer.position
val block = world.blockAt(here)

val input = redstone.read(KCraftDirection.WEST)
redstone.write(KCraftDirection.EAST, input)
```

The main scripting surfaces currently include:

``` text
terminal    Terminal output
files       Persistent computer filesystem
env         Process environment
computer    Current computer information and associated golems
world       Controlled access to Minecraft world state
redstone    Four-direction redstone input/output
```

Minecraft implementation classes such as `ServerLevel`, `BlockState`,
Fabric APIs, KCraft runtime internals and other implementation details
are not part of the scripting API.

### Minecraft World Access

Kotlin programs can query the Minecraft world in real time.

``` kotlin
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

`world.blockAt(...)` performs a live query. The returned `KCraftBlock`
is an immutable snapshot of the block at that position.

World access is executed through KCraft's server-thread request system
rather than exposing Minecraft objects directly to script threads.

### Redstone

Kotlin programs can read and write redstone signals on the four
horizontal sides of a computer.

``` kotlin
val input =
    redstone.read(KCraftDirection.WEST)

redstone.write(
    KCraftDirection.EAST,
    input
)
```

Signals use Minecraft's normal strength range of `0..15`.

Redstone access uses the same server-thread request system as other
Minecraft world operations.

### Ruby Golems

Ruby Golems are KCraft's programmable physical workers.

The basic idea is an Allay operating a larger ruby chassis: the floating
head is the expressive Allay-like part, while the body provides the
machinery needed for physical work.

In the 0.3 preview, a Ruby Golem begins with an actual Allay:

``` text
Ruby Golem Chassis + Allay
            ↓
      Ruby Golem item
            ↓
       computer slot
            ↓
          Deploy
            ↓
      Ruby Golem entity
```

If the Allay has a custom name, the Ruby Golem item preserves it and
deployment restores it to the entity.

Deployed Ruby Golems are associated with their parent computer and can
be discovered from Kotlin:

``` kotlin
computer.golems.forEach { golem ->
    terminal.println(
        "${golem.id}: ${golem.position}"
    )
}
```

They can also be instructed to move:

``` kotlin
val gerald = computer.golems.first()

gerald.moveTo(
    KCraftPosition(
        100,
        64,
        200
    )
)
```

`moveTo(...)` asks Minecraft's pathfinder for a route and begins
navigation if the requested position is reachable. If it is not, KCraft
throws `UnreachablePositionException`.

``` kotlin
try {
    gerald.moveTo(target)
} catch (e: UnreachablePositionException) {
    terminal.println(
        "Gerald cannot get there."
    )
}
```

Movement is animated from actual entity locomotion using separate start,
continuous and stop states.

Ruby Golem task APIs beyond movement are still under development. See
`IDEAS.md` for the design directions discussed so far.

## Processes

Kotlin programs execute as KCraft processes.

Running a script:

``` text
> run worker.kts
Started process 4
```

does not block the shell. Multiple programs may run concurrently.

Processes have:

-   A PID
-   A program path
-   A working directory inherited at launch
-   An environment inherited at launch
-   A lifecycle state
-   Independent execution

Running processes can be inspected with:

``` text
> ps
PID STATE PROGRAM
1 FINISHED /home/test.kts
4 RUNNING /home/worker.kts
```

and stopped with:

``` text
> kill 4
Stopped process 4
```

Current process states include:

``` text
STARTING
RUNNING
FINISHED
FAILED
STOPPED
```

Process cancellation currently uses JVM thread interruption and is
therefore cooperative; CPU-bound code that ignores interruption may
continue running.

## Runtime and Concurrency

Kotlin programs execute away from Minecraft's server thread.

Scripts do not directly manipulate KCraft's filesystem or Minecraft
world state from their worker threads. Instead, operations cross a
request boundary:

``` text
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

Filesystem, world, redstone and golem operations can therefore use
ordinary synchronous-looking Kotlin without exposing the underlying
threading model.

Each computer processes a limited number of requests per Minecraft tick
so programs cannot force an unlimited amount of host work into a single
tick.

## Scripting Classpath

KCraft's scripting API is built as a separate module from the main mod.

Kotlin scripts compile against the KCraft scripting API and Kotlin
standard library rather than KCraft's entire implementation classpath.

This prevents scripts from directly depending on implementation classes
such as:

``` text
ComputerRuntime
KCraftProcessManager
Minecraft ServerLevel
Fabric internals
```

and allows KCraft's implementation to evolve without unnecessarily
breaking player programs.

### Security Warning

**KCraft Kotlin scripting is currently trusted-code execution.**

The restricted scripting classpath is an API and compatibility boundary,
**not a complete security sandbox**.

Kotlin programs still execute as JVM code inside Minecraft's JVM and
currently retain access to JDK functionality. Do not run `.kts` programs
from untrusted sources on a server.

Further isolation may be added in future versions.

## KSh

KSh is KCraft's shell and orchestration language.

Despite sharing the `.ksh` extension with KornShell, KSh is unrelated to
KornShell.

KSh is not intended to replace Kotlin scripting. Kotlin scripts contain
program logic; KSh launches, configures and coordinates programs and
manages the computer environment.

Currently implemented commands include:

``` text
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

``` sh
source /etc/shell.kshrc
run /home/monitor.kts
```

As KCraft's process system develops, KSh will provide the orchestration
layer around larger collections of Kotlin programs.

## Pico

Pico is KCraft's built-in text editor.

The name is intentional: it's smaller than Nano.

Pico allows Kotlin programs, KSh scripts and other text files to be
edited directly inside a KCraft computer.

It currently supports:

-   Character insertion and deletion
-   Multiple lines
-   Cursor movement
-   Automatic viewport scrolling
-   Opening existing files
-   Creating new files
-   Saving files
-   Returning to the terminal

Together with the Kotlin compiler and diagnostics, Pico provides a
complete in-game edit-run-debug loop:

``` text
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

``` text
/
├── bin/
├── dev/
├── etc/
│   └── shell.kshrc
├── home/
└── tmp/
```

The filesystem supports:

-   Reading and writing files
-   Appending text
-   Creating and deleting files
-   Creating and deleting empty directories
-   Directory listing
-   Absolute and relative paths
-   Path normalisation
-   Process-local working directories
-   Protection against escaping the computer's filesystem root

Each computer has a persistent UUID, allowing its files, programs and
associated Ruby Golem state to survive chunk, world and Minecraft
restarts.

## Survival Progression

KCraft computers and Ruby Golems are intended to fit into ordinary
survival progression.

Rubies are KCraft's primary computing material. Ruby Ore generates
naturally in Nether blackstone.

The basic computer progression is:

``` text
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

Ruby Golems add a physical-worker path:

``` text
Ruby Golem Chassis
       +
     Allay
       ↓
Ruby Golem item
       ↓
Computer
       ↓
Deploy
       ↓
Write even more questionable Kotlin
```

## Planned Features

Development directions discussed so far include:

-   Ruby Golem recall
-   Helpful loose-item pickup and storage behaviour
-   Ruby Golem self-preservation and social behaviour
-   Mining, farming, inventory and possible combat tasks
-   Expanded Minecraft world APIs
-   Inventories and peripherals
-   Computer-to-computer networking
-   Wireless communication
-   Expanded KSh syntax and process management
-   Stronger script isolation
-   An installable and upgradeable operating environment

See `IDEAS.md` for the longer design notes.

## Development

KCraft is written primarily in Kotlin and currently targets:

-   Minecraft 26.2
-   Fabric
-   Fabric Language Kotlin
-   Java 25

The project is split so that the public scripting API is independent of
Minecraft and Fabric implementation classes.

Several runtime-independent components, including the filesystem, lexer
and KSh interpreter, are unit tested separately from Minecraft.

## Requirements

KCraft 0.2 Alpha requires:

-   Minecraft 26.2
-   Fabric Loader
-   Fabric API
-   Fabric Language Kotlin
-   Java 25 (included with Minecraft 26.2)

## Status

**0.2 Alpha released; 0.3 Preview in active development.**

KCraft currently provides survival-obtainable persistent computers with
a Unix-inspired shell environment, in-game text editor, runtime
Kotlin/JVM compilation, concurrent processes, persistent program
storage, controlled access to live Minecraft world state, redstone I/O,
and early programmable Ruby Golems.

APIs, filesystem formats, scripts, recipes, world data, networking
protocols and basically anything else may change without backwards
compatibility.

Do not entrust KCraft with the only copy of anything important.

Or do, for all I care.

Especially don't run random Kotlin from strangers yet.

## License

KCraft is licensed under the Mozilla Public License 2.0 (MPL-2.0).

See `LICENSE` for details.
