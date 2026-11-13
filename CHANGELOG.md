# Changelog

## 1.5.0

### Added

- Reskillable: a player's skill levels give the stamina stats by skill rules in the new `reskillable.skill_rules` list, `<skill id>=<stat>,<amount per level>`, counted for each level above the first. Default: each Agility level above the first gives 1 Maximum Stamina. Any skill registered in Reskillable can have rules; the stat names are those of the Statusworks types without `jassintegrations:`. The bonus follows every level change Reskillable makes at once.
- Config: `reskillable.enabled` and `reskillable.skill_rules`. `debug_logging` also logs Reskillable stamina bonus changes and the skill rules in use.

### Compatibility

- Optional Reskillable 1.13.0 or later (tested 1.13.0); with an older Reskillable the integration is off with one warning. A rule for a skill that is missing or switched off in Reskillable is skipped with one warning. Requirements unchanged: JASS 1.2.0 or later and MixinBooter 10.7 or later.

## 1.4.0

### Added

- CraftTweaker: the class `mods.jassintegrations.Stamina` for scripts. Checks (`getStamina`, `getVisibleStamina`, `getDebt`, `getMaxStamina`, `getRegeneration`, `getRegenerationDelay`, `getEfficiency`, `getWeight`, `isSpendingPaused`), direct changes (`setStamina`, `restoreStamina`, `removeStamina`) and simulated spending (`spendStamina` under every JASS rule, with an optional weight multiplier, and `canStartAction`). Server side only; invalid arguments change nothing and log one warning.
- Statusworks: the stamina stats as contribution types for modpack statuses, pools and marks, `jassintegrations:max_stamina`, `jassintegrations:stamina_regeneration`, `jassintegrations:regeneration_delay_reduction`, `jassintegrations:stamina_efficiency`, `jassintegrations:shield_stability` and `jassintegrations:weight_reduction`, with a `_percent` type where the stat has a percent form. Contributions add up and count while Statusworks keeps them on the player.
- Config: `crafttweaker.enabled` and `statusworks.enabled` switches. `debug_logging` also logs script changes and spends and Statusworks stamina bonus changes.

### Compatibility

- Optional CraftTweaker 4.1.20 or later (tested 4.1.20.718) and Statusworks 1.2.0 or later (tested 1.2.0); with an older Statusworks that integration turns off with one warning. Requirements unchanged: JASS 1.2.0 or later and MixinBooter 10.7 or later.

## 1.3.0

### Added

- Two stats from JASS 1.2.0 for Quality Tools qualities, JAFS foods and Setworks set bonuses: Shield Stability (`jassintegrations:shield_stability`, flat; quality attribute `jassintegrations.shield_stability`) adds Stability to every shield block, and Weight Reduction (`jassintegrations:weight_reduction`, flat or percent; quality attributes `jassintegrations.weight_reduction_flat` and `jassintegrations.weight_reduction_percent`) lowers the player's weight. Tooltips and generated set bonus descriptions name both.

### Changed

- Setworks: a bonus that ends at a respawn or dimension change now ends through Setworks 1.1.0 itself; this mod's own check one tick later is removed.

### Compatibility

- Requires JASS 1.2.0 or later (was 1.0.0). The Setworks integration needs Setworks 1.1.0 or later (tested 1.1.0); with Setworks 1.0.0 it turns off with one warning. MixinBooter 10.7 or later, unchanged.

## 1.2.0

### Added

- Setworks: the four stamina stats (`jassintegrations:max_stamina`, `jassintegrations:stamina_regeneration`, `jassintegrations:regeneration_delay_reduction`, `jassintegrations:stamina_efficiency`) as effects for modpack set bonuses, with the same units, ranges and names as the JAFS properties. A bonus lasts while Setworks counts it as active; generated bonus descriptions name each stat.
- Config: `setworks.enabled` switch.

### Compatibility

- Optional Setworks 1.0.0 or later (tested 1.0.0). Requirements unchanged: JASS 1.0.0 or later and MixinBooter 10.7 or later.

## 1.1.0

### Added

- Just Another Food System (JAFS): four stamina properties for modpack foods, `jassintegrations:max_stamina`, `jassintegrations:stamina_regeneration`, `jassintegrations:regeneration_delay_reduction`, and `jassintegrations:stamina_efficiency`, with the same units and signs as the Quality Tools attributes. A bonus lasts while JAFS keeps the food active. Food tooltips name each property.
- Config: `jafs.enabled` switch.

### Compatibility

- Optional JAFS 1.0.0 or later (tested 1.0.0). Requirements unchanged: JASS 1.0.0 or later and MixinBooter 10.7 or later.

## 1.0.0

### Added

- Spartan Weaponry: JASS swing cost, held weight, and draw drain for each weapon type, including weapons of add-ons that extend Spartan Weaponry's weapon classes.
- Spartan Weaponry: crossbow loading drains stamina, stops when stamina runs out, and cannot start at 0 stamina; aiming a loaded crossbow uses the cheaper crossbow drain.
- Spartan Shields: JASS Stability, melee and projectile block shares, and held weight for each shield, by material tier and shield kind.
- Spartan Shields: the shield bash costs stamina, can go into stamina debt, and is refused at 0 stamina or below.
- Quality Tools: seven `jassintegrations.*` stamina attributes for modpack qualities (Maximum Stamina, regeneration, regeneration delay, Stamina Efficiency).
- Quark: RotN Edition: JASS tooltip values shown as one icon row below Quark's attribute row; Shift shows the JASS text.
- Config `config/jassintegrations.cfg`: a switch per integration, values per weapon type, shield tier, and shield material, bash cost, crossbow loading drain, Quark row switch, and debug logging.
- One startup log line per integration: running, switched off in the config, or target mod not installed.

### Compatibility

- Requires JASS 1.0.0 or later and MixinBooter 10.7 or later.
- Tested with JASS 1.0.0 and 1.1.0, Spartan Weaponry 1.6.1, Spartan Shields 1.5.5, Quality Tools 1.0.7, Quark: RotN Edition r1.6-192, and the Spartan Netherite, Spartan and Fire, and Spartan Defiled add-ons.
