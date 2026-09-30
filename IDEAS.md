# KCraft Ideas

> Ideas, directions, and design notes for KCraft development.
>
> This is not a compatibility promise or a strict roadmap. Some ideas
> here are already partially implemented on `main`; others are
> deliberately left as future work.

KCraft 0.2 Alpha established the programmable-computer environment:
persistent computers, KSh, Pico, runtime Kotlin/JVM scripting,
concurrent processes, a persistent filesystem, controlled world access,
and the request boundary between script threads and Minecraft's server
thread.

The current 0.3 preview is expanding that environment into the Minecraft
world, especially through redstone and Ruby Golems.

## Ruby Golems

Ruby Golems are KCraft's physical automation workers.

> **A Ruby Golem is an Allay operating a larger body made from ruby
> machinery.**

The floating head is the Allay-like part of the creature; the ruby
chassis gives it the ability to do heavier physical work. This is both
lore and an animation rule.

Or, less formally:

> **cute red Allay mech go brrr**

### Lifecycle

The current design is:

``` text
Craft a Ruby Golem Chassis
        ↓
Use the chassis on an Allay
        ↓
The Allay enters the chassis
        ↓
Receive a Ruby Golem item
        ↓
Insert it into a KCraft computer
        ↓
Deploy
        ↓
Ruby Golem entity associated with that computer
        ↓
Recall
        ↓
Ruby Golem returns to item form
```

Creation and deployment are now present in the 0.3 preview. If the Allay
has a custom name, that name is preserved by the Ruby Golem item and
restored when deployed.

Computers have a dedicated persistent Ruby Golem slot. The initial
design is **one Ruby Golem per computer**.

Recall is the missing half of the lifecycle: a deployed golem should be
able to return to item form and re-enter its parent computer without
losing its identity.

### Computer Control

Ruby Golems are controlled through KCraft's scripting API rather than by
exposing Minecraft entity objects to scripts.

Movement is the first implemented command:

``` kotlin
val gerald = computer.golems.first()

gerald.moveTo(
    KCraftPosition(100, 64, 200)
)
```

Movement uses Minecraft pathfinding. If the requested block cannot be
reached, the scripting API throws `UnreachablePositionException` rather
than silently accepting the nearest partial path.

The intended boundary remains:

``` text
Kotlin program
      ↓
KCraft scripting API
      ↓
ComputerRequest
      ↓
server-authoritative runtime
      ↓
Ruby Golem
      ↓
Minecraft world
```

Larger task APIs should be added as the corresponding gameplay systems
actually exist rather than designing a large speculative API first.

## Personality and Default Behaviour

Ruby Golems should be useful machines without behaving like emotionless
robots. Their default behaviour comes from the Allay side of the design:
curious, helpful, social, and somewhat cowardly.

### Curiosity

Idle Ruby Golems already have procedural attention behaviour. A nearby
living entity can become an interesting target. KCraft calculates its
relative yaw and pitch, while the authored `idle.look_at` animation
decides how the golem reacts.

### Social Behaviour

Idle Ruby Golems should be interested in other Ruby Golems. A
lightweight version is enough: nearby golems may face one another and
use complementary body language that makes them appear to communicate.
No simulated dialogue is required.

### Helpful Item Cleanup

A Ruby Golem may notice a loose item entity, walk over, and pick it up.
Its default instinct should **not** remove items from arbitrary
inventories.

After picking up a loose item, it should use storage behaviour inspired
by Copper Golems: search a bounded set of nearby storage destinations
and place the item somewhere sensible.

``` text
Default instinct:
floor item → Ruby Golem → suitable nearby storage

Programmed logistics:
specific inventory → explicit rules → specific inventory
```

### Self-Preservation

Ruby Golems are not intended to behave like miniature Iron Golems when
attacked.

``` text
attacked
   ↓
react
   ↓
attack once
   ↓
flee
   ↓
reach a safe distance
   ↓
turn back and stare at the attacker
```

A Ruby Golem should remain wary for a short period afterwards. If the
attacker approaches again, it can flee farther.

The intended personality is slightly catlike: capable of defending
itself, but much more interested in escaping and judging the attacker
from safety than fighting continuously.

### Behaviour Priority

``` text
computer-directed task
        ↓
immediate danger / self-preservation
        ↓
carrying item → find storage
        ↓
loose item → helpful cleanup
        ↓
social curiosity
        ↓
visual curiosity
        ↓
ordinary idle
```

The important rule is that personality should not make programmable
automation unreliable.

## Animation

Ruby Golem animation follows one central rule:

> **The head leads; the body follows.**

The head is the light, flying, expressive part. The ruby chassis is
heavier and reacts afterwards.

``` text
Looking:
head notices and turns
→ body follows slightly

Starting to walk:
head moves first
→ body commits
→ legs establish the gait

Stopping:
body stops
→ head continues forwards
→ head corrects and settles

Interacting:
head investigates
→ body approaches
→ limb performs the action

Taking damage:
head reacts first
→ chassis responds afterwards
```

The aim is to get some of the lively, reactive feel of animation packs
such as Actions & Stuff without requiring an enormous catalogue of
bespoke animations.

### Current Animation Set

``` text
animation.ruby_golem.idle.stationary
animation.ruby_golem.idle.look_at

animation.ruby_golem.walk.start
animation.ruby_golem.walk.continuous
animation.ruby_golem.walk.end
```

`idle.look_at` is procedural: world logic supplies yaw and pitch while
the animation supplies anticipation, head motion, roll, body
follow-through, and return to idle.

Walking is split into start, continuous, and end phases. The animations
are driven by actual entity movement rather than being purely
decorative.

## Sound

A possible Ruby Golem sound direction is a quiet mechanical/flying
**whirr** rather than constant cartoon robot noises.

The reference sound discussed is similar to whistling into a running
table fan: a steady tone with a rapid flutter. It can be produced with a
low vocal hum and simultaneous mouth-whistle, giving it a slightly
organic imperfection that suits an Allay inside a machine.

Possible uses include subtle idle whirring, a brief rise when the golem
notices something, a spool-up when walking begins, and a settling sound
when it stops. The sound should remain restrained; animation should
continue to carry most of the personality.

## Planned Work Capabilities

### Mining

A programmed Ruby Golem should be able to perform deliberate mining
work. The computer decides what should be mined and where.

### Farming

Farming may include approaching crops, harvesting, replanting,
collecting drops, and moving results as directed.

### Inventory Work

Ruby Golems are intended to become KCraft's physical interface to
inventories. Default instincts are limited to loose-item cleanup and
sensible placement; precise extraction, transfer, and routing belong to
programmed behaviour.

### Combat

Combat may become a programmed capability of the Ruby chassis. This is
distinct from default self-preservation: deliberate combat would be an
explicit computer-directed task.

## Redstone

KCraft's four-direction redstone API is now part of the 0.3 preview.

``` kotlin
val input = redstone.read(KCraftDirection.WEST)
redstone.write(KCraftDirection.EAST, input)
```

Signals use Minecraft's normal `0..15` range. No vertical `UP` or `DOWN`
interface is planned for the basic computer redstone API.

## Processes and `kill`

KCraft processes are intentionally separated from authoritative computer
and Minecraft state. Script threads request filesystem and world
operations rather than mutating those systems directly.

A possible stronger `kill` model is **logical process death** rather
than unsafe asynchronous JVM thread termination:

``` text
kill
 ↓
mark KCraft process STOPPED
 ↓
revoke its ability to submit new KCraft requests
 ↓
interrupt the worker thread
 ↓
if it cooperates: exits
if it does not: detach and ignore it
```

Requests already accepted by the main thread may still complete even if
the originating process no longer exists. Requests attempted after the
process is stopped should be rejected.

`Thread.stop()` should not be used.

This still would not make KCraft a security sandbox: a rogue JVM thread
could consume CPU, allocate memory, or otherwise misuse JDK
functionality. Strong physical termination would require a stronger
isolation boundary than an in-JVM thread.

## Wider KCraft Systems

Other directions already discussed include:

-   expanded Minecraft world APIs;
-   inventories and peripherals;
-   computer-to-computer networking;
-   wireless communication;
-   programmable Ruby Golems communicating with their host computers;
-   expanded KSh syntax and process management;
-   stronger script isolation;
-   an installable and upgradeable operating environment.

Wireless golem control should require appropriate hardware and still
obey Minecraft's ticking/chunk constraints.

## Design Boundaries

### Scripts see KCraft, not Minecraft internals

The scripting API should expose KCraft types and capabilities rather
than `ServerLevel`, Fabric APIs, runtime internals, or other
implementation objects.

### Server authority remains authoritative

World operations, redstone, filesystem access, and golem actions should
cross KCraft's controlled request boundary where necessary rather than
allowing worker script threads to manipulate Minecraft state directly.

### Identity is preserved across forms

An Allay that becomes a Ruby Golem item and is later deployed should
remain recognisably the same individual. Custom names already survive
the Allay → item → entity path; future persistent golem state should
follow the same principle where appropriate.

### Personality is not task scheduling

Ruby Golem instincts exist to make the creature feel alive and provide a
small amount of natural helpfulness. Mining, farming, deliberate
logistics, construction, and other substantial work remain
computer-directed.

### The head is the creature; the body is the machine

This is both a lore concept and an animation rule.

> **cute red Allay mech go brrr**
