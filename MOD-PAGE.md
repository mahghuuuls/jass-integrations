Adds [Just Another Stamina System](https://www.curseforge.com/minecraft/mc-mods/just-another-stamina-system) (JASS) support to other mods, so their weapons, shields, items and tooltips work with JASS's stamina system. For Quality Tools, Just Another Food System, Setworks and Statusworks it adds stamina stats that modpack creators put on their own qualities, foods, set bonuses and statuses. With Reskillable, skill levels give stamina stats; by default each Agility level above the first adds 1 Maximum Stamina. With CraftTweaker, scripts can check, change and spend stamina. Each integration runs only when its mod is installed.

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

### [Statusworks](https://www.curseforge.com/minecraft/mc-mods/statusworks)

Needs Statusworks 1.2.0 or newer. With an older Statusworks this integration stays off, with a warning in the log.

JASS Integrations adds no statuses of its own. It adds the stamina stats (see Stamina Stats below) as contribution types that modpack creators and other mods can name in their Statusworks statuses, pools and marks, in CraftTweaker scripts or in Java. The flat names are the JAFS property names; each percent form has its own name ending in `_percent`:

- `jassintegrations:max_stamina` and `jassintegrations:max_stamina_percent`
- `jassintegrations:stamina_regeneration` (per second) and `jassintegrations:stamina_regeneration_percent`
- `jassintegrations:regeneration_delay_reduction` (seconds) and `jassintegrations:regeneration_delay_reduction_percent`
- `jassintegrations:stamina_efficiency`
- `jassintegrations:shield_stability`
- `jassintegrations:weight_reduction` and `jassintegrations:weight_reduction_percent`

For example, a status that gives 10% more Maximum Stamina per stack:

```zenscript
import mods.statusworks.Statuses;

Statuses.define("mypack:vigor")
    .limit(5)
    .expiration("none")
    .contributeType("jassintegrations:max_stamina_percent", "per_stack", 10.0)
    .register();
```

A bonus counts while Statusworks keeps the status, pool or mark on the player, and it changes with the stacks, the pool's fill or the mark. Bonuses from several statuses, pools and marks add up. They affect players only.

### [CraftTweaker](https://www.curseforge.com/minecraft/mc-mods/crafttweaker)

Needs CraftTweaker 4.1.20 or newer. Scripts can check, change and spend a player's stamina with the class `mods.jassintegrations.Stamina`. Every method takes the player first:

- **Checks:** `getStamina`, `getVisibleStamina`, `getDebt`, `getMaxStamina`, `getRegeneration`, `getRegenerationDelay`, `getEfficiency`, `getWeight` and `isSpendingPaused` return what `/stamina inspect` shows. `getStamina` goes below 0 in stamina debt; `getVisibleStamina` does not. `isSpendingPaused` is true while JASS has paused spending.
- **Changes:** `setStamina(player, value)`, `restoreStamina(player, amount)` and `removeStamina(player, amount)` change stamina directly and return the change made.
- **Spending:** `spendStamina(player, action, cost)` charges the cost like a JASS action, with Stamina Efficiency, stamina debt and the refusal at zero stamina, and returns true when the action may happen. In Creative mode, or while JASS has paused spending, nothing is paid and the call returns true. Add `true` as a fourth argument to apply the player's weight, as for sprinting. `canStartAction(player, action)` asks without paying.

```zenscript
import crafttweaker.event.PlayerRightClickItemEvent;
import mods.jassintegrations.Stamina;

events.onPlayerRightClickItem(function(event as PlayerRightClickItemEvent) {
    if (isNull(event.item)) {
        return;
    }
    if (event.item.definition.id != "minecraft:feather") {
        return;
    }
    if (Stamina.spendStamina(event.player, "mypack:dash", 10)) {
        event.player.sendChat("Dash!");
    }
});
```

The methods act on the server; on the client they return 0 or false and nothing changes. The action id is your own lower-case `namespace:path` name for the action, such as `mypack:dash`. A value that is not allowed, such as a negative amount, changes nothing and writes a warning to the CraftTweaker log. The class is not available to `#loader preinit` scripts.

### [Reskillable](https://www.curseforge.com/minecraft/mc-mods/reskillable)

Needs Reskillable 1.13.0 or newer. With an older Reskillable this integration stays off, with a warning in the log.

A player's skill levels give stamina stats (see Stamina Stats below) through skill rules, one per line in the `skill_rules` list of the `reskillable` section of `config/jassintegrations.cfg`. A rule names a skill, a stat and an amount for each level above the first, so level 1 gives nothing. The default rule gives 1 Maximum Stamina per Agility level: with JASS's default Maximum Stamina of 60, a player at Agility 11 has 70, and at Agility 32 (Reskillable's default cap) 91. For example, the default rule and a Mining rule that adds 2% Stamina Regeneration per level:

```
reskillable:agility=max_stamina,1
reskillable:mining=stamina_regeneration_percent,2
```

Stats: `max_stamina`, `stamina_regeneration`, `regeneration_delay_reduction`, `weight_reduction` (each also with `_percent`), `stamina_efficiency` and `shield_stability`. Any skill registered in Reskillable can have rules, including skills that other mods add. Rules add up, and a negative amount is a penalty. A rule for a skill that does not exist, or that is switched off in Reskillable's config, is skipped with a warning in the log. The bonus changes as soon as Reskillable changes a level. Changing the rules needs a restart.

## Stamina Stats

Qualities, foods, set bonuses, Reskillable skill levels, and Statusworks statuses, pools and marks can give these stats. A positive amount helps the player and a negative amount hurts.

- **Maximum Stamina:** points or percent.
- **Stamina Regeneration:** points per second or percent.
- **Regeneration Delay Reduction:** shortens the wait before stamina starts to recharge (JASS default 0.75 seconds), in seconds or percent.
- **Stamina Efficiency:** points, flat only. With JASS's default settings every cost is multiplied by 100 / (100 + efficiency), so 100 halves every cost.
- **Shield Stability:** points added to the Stability of every shield block, flat only. More Stability makes blocks cost less.
- **Weight Reduction:** weight points or percent. A lighter player spends less stamina on sprinting, jumping and dodging.

Only food and set bonus values have allowed ranges, listed in the README linked below.

## Configuration

`config/jassintegrations.cfg` has a section for each integration, with a switch to turn it off and the values it uses, including every weapon type and shield tier. A JASS config entry for a single item wins over the weapon and shield values here. Switching an integration or the Quark row on or off needs a restart.

In multiplayer the server's values decide the costs. Give players the same config file so their tooltips show the same numbers.

## Installation

Install on the server and on every client. Requires JASS 1.2.0 or newer. Made and tested on Cleanroom, which includes MixinBooter; on standard Forge, also install [MixinBooter](https://www.curseforge.com/minecraft/mc-mods/mixin-booter) 10.7 or newer. The Setworks integration needs Setworks 1.1.0 or newer, the Statusworks integration Statusworks 1.2.0 or newer, the CraftTweaker integration CraftTweaker 4.1.20 or newer, and the Reskillable integration Reskillable 1.13.0 or newer.

Source code and full reference (every setting, stat ranges, config line formats, the CraftTweaker class, the Statusworks types, the skill rules, and the tested versions): [github.com/mahghuuuls/jass-integrations](https://github.com/mahghuuuls/jass-integrations#readme)
