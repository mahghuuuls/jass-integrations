# JASS Integrations

An add-on for [Just Another Stamina System](https://github.com/mahghuuuls/jass) (JASS) for Minecraft 1.12.2. It gives Spartan Weaponry and Spartan Shields items stamina values by weapon type and shield tier, makes the Spartan shield bash and crossbow loading cost stamina, adds stamina attributes for Quality Tools qualities and stamina properties for Just Another Food System (JAFS) foods and Setworks set bonuses, and shows JASS tooltip values as an icon row in Quark: RotN Edition's style. It uses only the public JASS API.

- Player-facing description: [MOD-PAGE.md](MOD-PAGE.md)
- Release history: [CHANGELOG.md](CHANGELOG.md)
- License: [MIT](LICENSE). Build tooling notices: [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md)

## Identity and installation

- Mod ID `jassintegrations`, version 1.3.0, Forge 1.12.2 artifact. Developed and tested on Cleanroom.
- Required on the server and on every client.
- Requires JASS 1.2.0 or later (tested 1.2.0) and MixinBooter 10.7 or later (Cleanroom includes it).
- Integrations, each active only when its target mod is present: Spartan Weaponry (tested 1.6.1), Spartan Shields (tested 1.5.5), Quality Tools (tested 1.0.7), Quark: RotN Edition (tested r1.6-192), JAFS (tested 1.0.0), Setworks 1.1.0 or later (tested 1.1.0). The startup log has one line per integration: running, switched off in the config, or target mod not installed. With Setworks 1.0.0 the Setworks integration is off with one warning and the others run.

## Configuration

All settings live in `config/jassintegrations.cfg`. A JASS config entry for a single item wins over the weapon and shield values here.

| Category | Keys |
| --- | --- |
| `general` | `debug_logging` (logs shield bash charges and refusals, crossbow loading, quality attribute changes, JAFS stamina bonus changes, and Setworks stamina bonus changes) |
| `spartan_weaponry` | `enabled`, `weapon_types` |
| `spartan_shields` | `enabled`, `shield_tiers`, `shield_materials`, `bash_cost` (default 8) |
| `crossbow` | `loading_drain` (default 10 per second) |
| `quality_tools` | `enabled` |
| `quark` | `quark_row` (client) |
| `jafs` | `enabled` |
| `setworks` | `enabled` |

Line formats, one entry per line:

- `weapon_types`: `type=swing_cost,held_weight,draw_per_second`, for example `dagger=6,1,6`. An empty field gives no value, so JASS uses its default.
- `shield_tiers`: `tier_<1-5>_<basic|tower>=stability,melee_block,projectile_block,held_weight`, for example `tier_3_tower=80,1.0,1.0,8`.
- `shield_materials`: `material=tier`, for example `iron=3`.

A rejected line logs one warning and keeps the default. The `enabled` switches and `quark_row` need a restart.

Weapon types are found from Spartan Weaponry's weapon classes, so add-ons that extend those classes (Spartan Netherite, Spartan and Fire, Spartan Defiled) get the values of their weapon type. A crossbow's aiming drain is the `draw_per_second` of the `crossbow` line; loading uses `loading_drain`. Loading cannot start at 0 stamina, even when `loading_drain` is 0.

## Quality attributes

These attributes are usable by name in the `attributes` list of any Quality Tools quality, operation `0` only:

| Attribute | Positive amount |
| --- | --- |
| `jassintegrations.max_stamina_flat` | adds Maximum Stamina |
| `jassintegrations.max_stamina_percent` | raises Maximum Stamina, in percent |
| `jassintegrations.regeneration_flat` | adds regeneration per second |
| `jassintegrations.regeneration_percent` | raises regeneration, in percent |
| `jassintegrations.delay_reduction_flat` | shortens the regeneration delay, in seconds |
| `jassintegrations.delay_reduction_percent` | shortens the regeneration delay, in percent |
| `jassintegrations.stamina_efficiency` | adds Stamina Efficiency |
| `jassintegrations.shield_stability` | adds Stability to every shield block (JASS's Stability Bonus) |
| `jassintegrations.weight_reduction_flat` | lowers the player's weight, in weight points |
| `jassintegrations.weight_reduction_percent` | lowers the player's weight, in percent |

A negative amount has the opposite effect. Percent amounts below -100 count as -100. JASS adds the Stability Bonus to the blocking item's Stability, and the Stability of a block never goes below 0. Weight changes only the cost of sprinting, jumping and dodging; a weight reduction of 100 percent or more leaves no weight. Several qualities add up. An entry with another operation is ignored with one warning. The values reach JASS through a contextual provider, `jassintegrations:quality_tools`, which `/stamina inspect` lists.

The attributes are registered on every player and saved with the player like any attribute. A world saved with them logs vanilla "Ignoring unknown attribute" warnings when it is loaded with the Quality Tools integration switched off or without this mod. The warnings are harmless.

## JAFS properties

These properties are registered with JAFS at startup and usable by id in the `modifiers` of any food in `config/jafs/foods.json`:

| Property | Operations | Positive amount | Range per entry |
| --- | --- | --- | --- |
| `jassintegrations:max_stamina` | flat, percent | raises Maximum Stamina | flat -1000 to 1000, percent -100 to 1000 |
| `jassintegrations:stamina_regeneration` | flat (per second), percent | raises regeneration | flat -1000 to 1000, percent -100 to 1000 |
| `jassintegrations:regeneration_delay_reduction` | flat (seconds), percent | shortens the regeneration delay | flat -60 to 60, percent -100 to 100 |
| `jassintegrations:stamina_efficiency` | flat | adds Stamina Efficiency | -1000 to 1000 |
| `jassintegrations:shield_stability` | flat | adds Stability to every shield block | -1000 to 1000 |
| `jassintegrations:weight_reduction` | flat (weight points), percent | lowers the player's weight | flat -100 to 100, percent -100 to 100 |

The units and signs are the same as the quality attributes. JAFS adds the amounts of all active foods, flat and percent separately, and checks each entry against its range when it loads the definitions; a percent total below -100 counts as -100. A bonus applies while JAFS keeps the food active, including after a reconnect and, with JAFS's `keepFoodsOnDeath`, after a respawn. This mod saves nothing; JAFS saves the foods. The values reach JASS through a contextual provider, `jassintegrations:jafs`, which `/stamina inspect` lists. With the JAFS integration switched off, the properties do not exist and JAFS reports foods that name them.

## Setworks effects

The same stats, registered with Setworks as effect kinds under the same ids and ranges as the JAFS properties. A set bonus names them with two parameters, `operation` (`flat` or `percent`; `stamina_efficiency` and `shield_stability` are flat only) and `amount` (a plain decimal number), for example `.effect("jassintegrations:max_stamina", {operation: "percent", amount: "10"})`.

- Setworks checks each effect when it loads the definitions. An unknown operation, a number it cannot read, or an amount outside the range disables the bonus and, by Setworks' rule, every set that bonus requires; the game log names the reason.
- Active bonuses add up, flat and percent separately; a percent total below -100 counts as -100.
- A bonus applies while Setworks counts it as active and ends when the bonus ends, also when it ends at a respawn or dimension change (Setworks 1.1.0 withdraws it then). Death ends every bonus; the respawn applies the active ones again.
- A bonus without its own description shows each effect by name, for example "+10% Maximum Stamina" (two decimal places at most).
- The values reach JASS through a contextual provider, `jassintegrations:setworks`. With the Setworks integration switched off, Setworks reports these effects as unknown and disables their bonuses and sets.

## Compatibility notes

- Spartan Shields: the bash cost is charged by a Mixin into Spartan Shields' bash packet handler; a client Mixin skips the arm swing when the bash will be refused. If either target method is missing, the bash cost stays off with one warning and the rest of the integration runs.
- JASS refuses a mouse attack aimed at an entity or at air at 0 stamina, so a bash in that case is refused by JASS before Spartan Shields sees it.
- Quark row: needs Quark's Visual Stat Display on. It replaces only the JASS value lines; JASS's modifier lines stay as text. With JASS's `item_tooltips=shift`, the row is not drawn and JASS's "Hold Shift" hint stays.
- Action ids for JASS: `jassintegrations:shield_bash`, `jassintegrations:crossbow_load`. Provider ids: `jassintegrations:spartan_weaponry`, `jassintegrations:spartan_shields`, `jassintegrations:quality_tools`, `jassintegrations:jafs`, `jassintegrations:setworks`.

## License

MIT. See [LICENSE](LICENSE). The icon atlas includes a copy of the JASS stamina bolt (same author, MIT).
