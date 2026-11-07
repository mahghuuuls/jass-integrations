Adds [Just Another Stamina System](https://www.curseforge.com/minecraft/mc-mods/just-another-stamina-system) (JASS) support to other mods, so their weapons, shields, items and tooltips work with JASS's stamina system. For Quality Tools, Just Another Food System and Setworks it adds stamina stats that modpack creators put on their own qualities, foods and set bonuses. Each integration runs only when its mod is installed.

<span style="color:#d6a100">**AI usage disclaimer:** This mod was developed with AI-agent assistance using [this agent workflow](https://github.com/mahghuuuls/minecraft-1.12.2-mod-agent-workflow). The project owner reviewed the work during development.</span>

## Supported Mods

### [Spartan Weaponry](https://www.curseforge.com/minecraft/mc-mods/spartan-weaponry)

- Each weapon type has its own swing cost and held weight. By default a dagger costs 6 stamina per swing and weighs 1, a longsword costs 12 and weighs 3, and a greatsword costs 18 and weighs 6.
- Drawing a longbow drains 10 stamina per second.
- Loading a crossbow drains 10 stamina per second. When stamina runs out, loading stops and the crossbow stays empty. Loading cannot start at zero stamina or below.
- Aiming a loaded crossbow drains 3 stamina per second.
- Add-ons that build their weapons on Spartan Weaponry's, such as [Spartan Netherite](https://www.curseforge.com/minecraft/mc-mods/spartan-netherite), [Spartan and Fire](https://www.curseforge.com/minecraft/mc-mods/spartan-and-fire), and [Spartan Defiled](https://www.curseforge.com/minecraft/mc-mods/spartan-defiled), get the same values for each weapon type.

### [Spartan Shields](https://www.curseforge.com/minecraft/mc-mods/spartan-shields)

- Each shield gets JASS Stability and held weight from its material tier and its kind. Better materials have more Stability, so blocks cost less. Tower shields have more Stability than basic shields and weigh more.
- A shield bash costs 8 stamina. Like a JASS swing, it can go into stamina debt, and it cannot be used at zero stamina or below.

### [Quark: RotN Edition](https://www.curseforge.com/minecraft/mc-mods/quark-rotn-edition)

With Quark's Visual Stat Display on, the JASS values in item tooltips show as one row of icons and numbers, right below Quark's own row. Hold Shift to see the JASS text instead.

### [Quality Tools](https://www.curseforge.com/minecraft/mc-mods/quality-tools)

JASS Integrations adds no qualities of its own. It adds the stamina stats (see Stamina Stats below) as attributes that modpack creators can use in their own qualities. Attribute names use a dot, and stats with both forms have separate flat and percent names:

- `jassintegrations.max_stamina_flat` and `jassintegrations.max_stamina_percent`
- `jassintegrations.regeneration_flat` (per second) and `jassintegrations.regeneration_percent`
- `jassintegrations.delay_reduction_flat` (seconds) and `jassintegrations.delay_reduction_percent`
- `jassintegrations.stamina_efficiency`
- `jassintegrations.shield_stability`
- `jassintegrations.weight_reduction_flat` and `jassintegrations.weight_reduction_percent`

Only operation `0` counts; use the `_percent` attributes for percentages. An entry with another operation is ignored, with a warning in the log. For example:

```json
{ "name": "jassintegrations.max_stamina_percent", "amount": 10, "operation": 0 }
```

The tooltip shows this line as "+10 % Maximum Stamina".

### [Just Another Food System](https://www.curseforge.com/minecraft/mc-mods/just-another-food-system)

JASS Integrations adds no foods of its own. It adds the stamina stats (see Stamina Stats below) as properties that modpack creators can put on their JAFS foods in `config/jafs/foods.json`:

- `jassintegrations:max_stamina` (flat or percent)
- `jassintegrations:stamina_regeneration` (flat per second, or percent)
- `jassintegrations:regeneration_delay_reduction` (flat seconds, or percent)
- `jassintegrations:stamina_efficiency` (flat)
- `jassintegrations:shield_stability` (flat)
- `jassintegrations:weight_reduction` (flat or percent)

Use them like any JAFS property in a food's modifiers:

```json
{ "property": "jassintegrations:max_stamina", "operation": "percent", "amount": 10 }
```

The bonus lasts while the food is active. The food tooltip shows this line as "+10% Maximum Stamina".

### [Setworks](https://www.curseforge.com/minecraft/mc-mods/setworks)

Needs Setworks 1.1.0 or newer. With an older Setworks this integration stays off, and Setworks turns off the sets that use these effects.

JASS Integrations adds no sets of its own. It adds the stamina stats (see Stamina Stats below) as effects that modpack creators can put on set bonuses in their Setworks scripts (written for [CraftTweaker](https://www.curseforge.com/minecraft/mc-mods/crafttweaker)), with the same names as the JAFS properties:

- `jassintegrations:max_stamina` (flat or percent)
- `jassintegrations:stamina_regeneration` (flat per second, or percent)
- `jassintegrations:regeneration_delay_reduction` (flat seconds, or percent)
- `jassintegrations:stamina_efficiency` (flat)
- `jassintegrations:shield_stability` (flat)
- `jassintegrations:weight_reduction` (flat or percent)

Use them in a bonus with `operation` and `amount`:

```zenscript
#loader setworks
import mods.setworks.Setworks;

Setworks.bonus("mypack:iron_stamina").requires("mypack:iron_armor", 2)
    .effect("jassintegrations:max_stamina", {operation: "percent", amount: "10"});
```

The bonus lasts while the set bonus is active. A bonus without its own description shows this line as "+10% Maximum Stamina". A value Setworks cannot use, such as an amount out of range, turns off that bonus and its set, and the game log says why.

## Stamina Stats

Qualities, foods and set bonuses can give these stats. A positive amount helps the player and a negative amount hurts.

- **Maximum Stamina:** points or percent.
- **Stamina Regeneration:** points per second or percent.
- **Regeneration Delay Reduction:** shortens the wait before stamina starts to recharge (JASS default 0.75 seconds), in seconds or percent.
- **Stamina Efficiency:** points, flat only. With JASS's default settings every cost is multiplied by 100 / (100 + efficiency), so 100 halves every cost.
- **Shield Stability:** points added to the Stability of every shield block, flat only. More Stability makes blocks cost less.
- **Weight Reduction:** weight points or percent. A lighter player spends less stamina on sprinting, jumping and dodging.

The allowed range of each food and set bonus value is in the README linked below.

## Configuration

`config/jassintegrations.cfg` has a section for each integration, with a switch to turn it off and the values it uses, including every weapon type and shield tier. A JASS config entry for a single item wins over the weapon and shield values here. Switching an integration on or off needs a restart.

In multiplayer the server's values decide the costs. Give players the same config file so their tooltips show the same numbers.

## Installation

Install on the server and on every client. Requires JASS 1.2.0 or newer and [MixinBooter](https://www.curseforge.com/minecraft/mc-mods/mixin-booter) 10.7 or newer (Cleanroom already includes MixinBooter). The Setworks support needs Setworks 1.1.0 or newer.

Source code and full reference (every setting, stat ranges and config line formats): [github.com/mahghuuuls/jass-integrations](https://github.com/mahghuuuls/jass-integrations#readme)
