This mod connects [Just Another Stamina System](https://www.curseforge.com/minecraft/mc-mods/just-another-stamina-system) (JASS) with other mods. Each integration gives one mod's weapons, shields, item qualities, or tooltips stamina values that fit them. An integration runs only when its mod is installed.

<span style="color:#d6a100">**AI usage disclaimer:** This mod was developed with AI-agent assistance using [this agent workflow](https://github.com/mahghuuuls/minecraft-1.12.2-mod-agent-workflow). The project owner reviewed the work during development.</span>

## [Spartan Weaponry](https://www.curseforge.com/minecraft/mc-mods/spartan-weaponry)

- Each weapon type has its own swing cost and held weight. A dagger costs 6 stamina per swing and weighs 1, a longsword costs 12 and weighs 3, and a greatsword costs 18 and weighs 6.
- Drawing a longbow drains 10 stamina per second.
- Loading a crossbow drains 10 stamina per second. When stamina runs out, loading stops and the crossbow stays empty. Loading cannot start at zero stamina.
- Aiming a loaded crossbow drains 3 stamina per second.
- Add-ons that build their weapons on Spartan Weaponry's, such as [Spartan Netherite](https://www.curseforge.com/minecraft/mc-mods/spartan-netherite), [Spartan and Fire](https://www.curseforge.com/minecraft/mc-mods/spartan-and-fire), and [Spartan Defiled](https://www.curseforge.com/minecraft/mc-mods/spartan-defiled), get the same values for each weapon type.

## [Spartan Shields](https://www.curseforge.com/minecraft/mc-mods/spartan-shields)

- Each shield gets JASS Stability, block shares, and held weight from its material tier and its kind. Better materials have more Stability, so blocks cost less. Tower shields have more Stability than basic shields and weigh more.
- A shield bash costs 8 stamina. Like a JASS swing, it can go into stamina debt, and it is refused at zero stamina or below.

## [Quality Tools](https://www.curseforge.com/minecraft/mc-mods/quality-tools)

This mod adds no qualities. It adds stamina attributes that modpack creators can use in their own qualities:

- `jassintegrations.max_stamina_flat` and `jassintegrations.max_stamina_percent`
- `jassintegrations.regeneration_flat` and `jassintegrations.regeneration_percent`
- `jassintegrations.delay_reduction_flat` (seconds) and `jassintegrations.delay_reduction_percent`
- `jassintegrations.stamina_efficiency`

Use them like any attribute in a quality, with operation `0`:

```json
{ "name": "jassintegrations.max_stamina_percent", "amount": 10, "operation": 0 }
```

A positive amount helps the player and a negative amount hurts. The tooltip shows the line as "+10 % Maximum Stamina".

## [Quark: RotN Edition](https://www.curseforge.com/minecraft/mc-mods/quark-rotn-edition)

With Quark's Visual Stat Display on, the JASS values in item tooltips show as one row of icons and numbers, right below Quark's own row. Hold Shift to see the JASS text instead.

## Configuration

`config/jassintegrations.cfg` can switch each integration on or off and set the values per weapon type, per shield tier, and per shield material, the shield bash cost, and the crossbow loading drain. A JASS config entry for a single item always wins over these values. Switching an integration on or off needs a restart.

## Installation

Install on the server and on every client. Requires JASS and [MixinBooter](https://www.curseforge.com/minecraft/mc-mods/mixin-booter) 10.7 or newer (Cleanroom already includes MixinBooter).

Source code: [github.com/mahghuuuls/jass-integrations](https://github.com/mahghuuuls/jass-integrations)
