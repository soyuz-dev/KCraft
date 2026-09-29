# KCraft Development Roadmap

> This document collects the features and design directions discussed
> for KCraft after **0.2 Alpha: *KotlinCraft***. It is a possible development
> roadmap, not a compatibility promise: details may change as the
> systems are implemented and tested.

## Current Direction

KCraft 0.2 established the programmable-computer environment: persistent
computers, KSh, Pico, runtime Kotlin/JVM scripting, concurrent
processes, a persistent filesystem, controlled world access, and the
request boundary between script threads and Minecraft's server thread.

Development towards **KCraft 0.3** expands that environment into the
Minecraft world. The areas discussed so far are bidirectional redstone
I/O; Ruby Golems as physical workers controlled by KCraft computers; a
small set of Allay-derived default behaviours; Kotlin APIs for
commanding those golems; and later expansion into mining, farming,
inventory work, combat, peripherals, networking, and wireless control.

The current `main` branch may contain preview work that has not been
packaged as a GitHub release.

## Redstone

Four-direction redstone I/O is the first major post-0.2 world-control
feature.

KCraft computers can read and write redstone on their four horizontal
sides using KCraft's own API types rather than exposing Minecraft
implementation objects directly. Signals use Minecraft's normal analogue
range of `0..15`.

``` kotlin
val input = redstone.read(KCraftDirection.WEST)
redstone.write(KCraftDirection.EAST, input)
```

No vertical `UP` or `DOWN` interface is planned for the basic computer
redstone API.

# Ruby Golems

Ruby Golems are KCraft's planned physical automation workers.

The core concept is:

> **A Ruby Golem is an Allay operating a larger body made from ruby
> machinery.**

The former Allay is represented by the Ruby Golem's floating head. The
ruby chassis gives it the physical ability to perform tasks that an
ordinary Allay cannot. This concept informs both gameplay and animation.

## Creation

The planned survival process is:

``` text
Craft a Ruby Golem Chassis
        ↓
Use the chassis on an Allay
        ↓
The Allay enters the chassis
        ↓
Receive a Ruby Golem item
```

The interaction is intended to resemble Create's Blaze/Burner-style
capture interaction rather than an ordinary crafting-table recipe.

If the Allay has a custom name, the resulting Ruby Golem item should
preserve that name. Deploying the item should preserve the identity of
the original Allay rather than treating it as a disposable crafting
ingredient.

## Computer Slot, Deployment and Recall

A KCraft computer is planned to have a dedicated slot for a Ruby Golem.

``` text
Ruby Golem item
        ↓
insert into computer
        ↓
computer deploys Ruby Golem
        ↓
Ruby Golem exists as an entity associated with that computer
        ↓
computer recalls Ruby Golem
        ↓
Ruby Golem returns to item form
```

The initial design is **one Ruby Golem per computer**. Ruby Golems
already use persistent parent-computer ownership while deployed; the
item/deployment system is intended to build on that relationship.

## Personality and Default Behaviour

Ruby Golems should be useful machines without behaving like emotionless
robots. Their default behaviour comes from the Allay side of the design:
curious, helpful, social, and somewhat cowardly. These instincts should
remain small enough that they do not compete with explicit computer
commands.

### Curiosity

While idle, a Ruby Golem can notice nearby entities and look towards
them. The current animation system supports a procedural `look_at`
animation driven by relative yaw and pitch. The world chooses what is
interesting and supplies the direction; the animation determines how the
golem reacts.

### Social Behaviour

Idle Ruby Golems should be interested in other Ruby Golems. Two golems
may face one another and play complementary movements that make them
appear to communicate. No simulated dialogue or semantic communication
is required.

### Helpful Item Cleanup

A Ruby Golem may notice a loose item entity, walk over, and pick it up.
Its default instinct should **not** remove items from arbitrary
inventories.

After picking up a loose item, the golem should use storage behaviour
inspired by Copper Golems: search a bounded set of nearby storage
destinations and place the item somewhere sensible, such as compatible
existing storage or available space.

``` text
Default instinct:
floor item → Ruby Golem → suitable nearby storage

Programmed logistics:
specific inventory → explicit rules → specific inventory
```

This makes an idle Ruby Golem useful for tidying dropped items without
allowing it to reorganise a player's storage system on its own.

### Self-Preservation

Ruby Golems are not intended to behave like miniature Iron Golems when
attacked.

``` text
attacked
   ↓
react to attacker
   ↓
attack once
   ↓
flee
   ↓
reach a safe distance
   ↓
turn back and watch the attacker
```

A Ruby Golem should not instantly forget the threat after reaching
safety. For a short period after fleeing, it should remain wary of the
attacker. If the attacker approaches again, the golem can flee farther.

This is intended to give Ruby Golems a slightly catlike form of
cowardice: capable of defending themselves, but much more interested in
getting away and judging the attacker from a safe distance than fighting
continuously.

## Behaviour Priority

The discussed behaviour model is roughly:

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

The exact implementation may evolve, but personality should not make
programmable automation unreliable.

# Ruby Golem Animation

Ruby Golem animation follows one central rule:

> **The head leads; the body follows.**

The head represents the flying Allay-like part of the creature. It is
light, curious, and expressive. The ruby chassis is heavier and responds
afterwards.

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

The intent is to capture some of the lively, reactive feel of animation
packs such as Actions & Stuff without requiring an enormous catalogue of
bespoke animations.

## Current Animation Set

``` text
animation.ruby_golem.idle.stationary
animation.ruby_golem.idle.look_at

animation.ruby_golem.walk.start
animation.ruby_golem.walk.continuous
animation.ruby_golem.walk.end
```

`idle.stationary` uses small irregular vertical movement and tiny yaw
drift. `idle.look_at` is procedural: KCraft supplies relative yaw and
pitch while the authored animation supplies anticipation, roll, body
follow-through, and return to idle.

Walking is split into three animations because starting and stopping are
character moments. `walk.start` has the head initiate movement before
the chassis fully commits. `walk.continuous` provides the normal gait.
`walk.end` has the chassis stop while the head briefly continues
forwards, corrects, and returns to idle.

The locomotion animations are driven by actual entity movement rather
than being purely decorative.

# Computer Control

Ruby Golems are intended to be controlled by Kotlin programs running on
their parent computers.

Existing golem work establishes persistent entities, persistent
parent-computer association, computer-side discovery of associated
golems, Minecraft pathfinding/navigation, and animated locomotion tied
to actual movement.

The next control layer is to expose those capabilities through KCraft's
scripting API and request system:

``` text
Kotlin program
      ↓
KCraft scripting API
      ↓
request boundary
      ↓
server-authoritative computer/runtime
      ↓
Ruby Golem
      ↓
Minecraft world
```

Scripts should not receive raw Minecraft entity objects.

The exact final golem API has not yet been fixed. Movement is the
immediate capability being developed; larger task APIs will be designed
as the corresponding gameplay systems exist.

# Planned Work Capabilities

The Ruby chassis is intended to scale the Allay's instinct to help into
more substantial physical work.

## Mining

A programmed Ruby Golem should be able to perform deliberate mining
work. The computer decides what should be mined and where.

## Farming

Farming is another intended physical task: approaching crops,
harvesting, replanting, collecting drops, and moving the results as
directed.

## Inventory Work

Ruby Golems are intended to become KCraft's physical interface to
inventories. This is deliberately different from giving computer scripts
arbitrary direct inventory access.

Default instincts are limited to loose-item cleanup and sensible
placement. Precise extraction, transfer, and routing belong to
programmed behaviour.

## Combat

Combat may become a programmed capability of the Ruby chassis. This is
distinct from default self-preservation: an idle golem is cowardly and
prefers to escape; deliberate combat would be an explicit task.

# Wider KCraft Systems

The following broader systems have also been discussed as future KCraft
work:

-   expanded Minecraft world APIs;
-   inventories and peripherals;
-   computer-to-computer networking;
-   wireless communication;
-   programmable Ruby Golems communicating with their host computers;
-   expanded KSh syntax and process management;
-   stronger script isolation;
-   an installable and upgradeable operating environment.

Wireless golem control is intended to require appropriate hardware and
still obey Minecraft's ticking/chunk constraints.

# Design Boundaries

## Scripts see KCraft, not Minecraft internals

The scripting API should expose KCraft types and capabilities rather
than `ServerLevel`, Fabric APIs, runtime internals, or other
implementation objects.

## Server authority remains authoritative

World operations, redstone, filesystem access, and future golem actions
should cross KCraft's controlled request boundary where necessary rather
than allowing worker script threads to manipulate Minecraft state
directly.

## Personality is not task scheduling

Ruby Golem instincts exist to make the creature feel alive and provide a
small amount of natural helpfulness. Mining, farming, deliberate
logistics, construction, and other substantial work remain
computer-directed.

## The head is the creature; the body is the machine

This is both a lore concept and an animation rule.

Or, less formally:

> **cute red Allay mech go brrr**
