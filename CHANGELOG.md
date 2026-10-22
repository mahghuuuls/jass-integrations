# Changelog

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
