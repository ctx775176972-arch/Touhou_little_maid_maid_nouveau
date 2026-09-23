# Touhou Little Maid: Maid Nouveau

<p align="center">
  <img src="artwork/mod-description-icon.png" alt="Touhou Little Maid: Maid Nouveau" width="720">
</p>

An integration mod for **Touhou Little Maid** and **Ars Nouveau** on NeoForge.

The main goal of this mod is to let maids properly benefit from passive Ars Nouveau equipment attributes such as Spell Damage and Mana Regeneration, while allowing their normal combat AI to trigger spells scribed onto Enchanter's Swords, Spell Bows, and Spell Crossbows. It also adds maid automation for the Enchanting Apparatus, Potion Jars, and Ritual Brazier.

## Supported Versions

| Component | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| Mod loader | NeoForge 21.1.219 or newer |
| Touhou Little Maid | 1.4.3 or newer |
| Ars Nouveau | 5.10.0 or newer |
| Current mod version | 0.1.0 |

Touhou Little Maid and Ars Nouveau are both required. This mod must be installed on both the client and server.

## Features

### Maid Equipment and Spell Attributes

- Maids inherit Spell Damage, Mana Regeneration, Warding, Feather, Wixie, and related Ars Nouveau equipment attributes.
- Favorability grants an additional Spell Damage bonus of +3, +5, or +7 at the three progression tiers.
- Maids do not receive a separate mana pool. Spell reuse is controlled by weapon behavior and cooldown systems supplied by compatible mods.
- Mana Regeneration on equipped items periodically repairs damaged Ars Nouveau equipment worn or carried by the maid.
- The Mana Regeneration status effect provides additional repair power.
- Successfully blocking with an Enchanter's Shield temporarily grants Mana Regeneration and Spell Damage, improving both repair and damage output.

### Enchanter Weapon Support

- Enchanter's Swords trigger their scribed spell when used by a maid in melee combat.
- Spell Bows and Spell Crossbows work with the Archer and Crossbowman combat modes from Touhou Little Maid.
- A maid can fire a zero-damage carrier arrow when no ammunition is available, allowing the scribed spell to remain the primary source of damage.
- If normal or Ars Nouveau arrows are present in the maid's inventory, real ammunition is preferred and consumed normally.
- Ars Nouveau arrow effects such as Split and Pierce are supported.

### Maid Automation

The mod adds three maid work modes, each using its corresponding Ars Nouveau block as the task icon.

1. **Enchanting Apparatus Task**
   - The item in the maid's main hand acts as the output template and is not consumed.
   - Recipe ingredients are taken from the maid's inventory.
   - The maid fills the surrounding Arcane Pedestals first, inserts the reagent into the Enchanting Apparatus last, waits for completion, and collects the result.

2. **Potion Jar Task**
   - The maid fills Glass Bottles or Potion Flasks from a Potion Jar within the work area.

3. **Ritual Task**
   - The maid holds a Ritual Tablet in her main hand and carries ritual offerings in her inventory.
   - She locates a Ritual Brazier within the work area, starts the ritual, and supplies matching offerings.

Each task can be enabled or disabled independently in the maid work configuration. Its cooldown can be set from 5 to 3600 seconds, making long intervals suitable for scheduled rituals.

## Workstation Manager

The Workstation Manager binds a maid to a selected Ars Nouveau workstation. Its appearance is based on a variant of the Dominion Wand.

Crafting ingredients:

- 1 Stick
- 2 Source Gems
- 1 Gold Ingot

It is crafted through the Touhou Little Maid altar and requires 0 Power Points.

Basic setup:

1. Assign the maid to the desired automation work mode.
2. Use the Workstation Manager to select an Enchanting Apparatus, Potion Jar, or Ritual Brazier.
3. Place the output template or Ritual Tablet in the maid's main hand and put the required materials in her inventory.
4. Adjust the search range, operation interval, and task cooldown in the work configuration.

## Configuration

The common configuration file provides the following options:

- Workstation search radius: default 16, range 4–64.
- Maid movement speed: default 0.6, range 0.1–2.0.
- Operation interval for each automation task: default 20 ticks, range 1–1200 ticks.

Each maid stores her own task enable state and 5–3600 second cooldown settings through the in-game work configuration.

## Installation

1. Install Minecraft 1.21.1 and NeoForge.
2. Install Touhou Little Maid and Ars Nouveau.
3. Place this mod's jar in the mods folder of the client or server instance.
4. Use the same mod version on the client and server.

## Notes

- Arrows fired by Spell Bows and Spell Crossbows act as carriers for spell impacts. Zero physical damage does not mean that the spell deals no damage.
- Block breaking performed by a maid or FakePlayer remains subject to Ars Nouveau behavior, claim protection, and server permission rules.
- Automation requires the correct workstation layout, pedestals, Source, and recipe ingredients within the configured work area.
- Other spell compatibility mods are optional. Mods that replace mana costs with cooldowns naturally complement the decision not to give maids a separate mana pool.

## Building from Source

Run the following command from the repository root:

    .\gradlew.bat build

The built jar will be available in build/libs/.

## License

This project is licensed under the [MIT License](LICENSE).
