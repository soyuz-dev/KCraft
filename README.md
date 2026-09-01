# KCraft

KCraft is a Minecraft mod that brings programmable computers to the game.

Unlike traditional computer mods that emulate a virtual CPU, KCraft exposes the Minecraft runtime through a safe Kotlin API, allowing scripts to interact with the world directly.

## Planned Features

### Computers
- Placeable computer blocks
- Terminal-style interface
- Boot process and operating systems
- Persistent filesystem
- Kotlin scripting support (`.kts`)

### Automation
- Programmable golems
- Remote control through wireless modules
- Task automation
- Event-driven scripting

### Hardware
- Computer Chips
- Wireless Modules
- Storage devices
- Expandable peripherals

## Philosophy

KCraft aims to feel like programming a real computer while remaining deeply integrated with Minecraft.

Instead of inventing a fictional machine, scripts run against an abstraction of Minecraft itself. This allows you to write Kotlin code to automate farms, control golems, monitor the world, and build larger systems.

## Example

```kotlin
val miner = golems["miner-1"]

miner.mineForward(64)
miner.returnHome()

println("Mining complete!")
```

## Planned Features

- [x] Ruby
- [x] Ruby Block
- [x] Computer Block
- [ ] Computer GUI
- [x] Computer Chip
- [ ] Filesystem
- [ ] Bootloader
- [ ] Operating System
- [ ] Kotlin scripting
- [ ] Golem API
- [ ] Wireless networking
- [ ] Peripheral API
- [ ] Event system
- [ ] Persistent programs

## Building

```bash
./gradlew build
```

## License
LGPL 3.0
