# Oof Tracker: A Spigot Damage API
![Version: Commons](https://img.shields.io/badge/Version-1.0-blue?style=for-the-badge)
![Badge: Java](https://img.shields.io/badge/Java-8-red?style=for-the-badge)
![Supports: Spigot](https://img.shields.io/badge/Spigot-1.16.5-orange?style=for-the-badge)

In summary, **OofTracker** is a highly-customizable damage manager for **Spigot** with a few immediately useful features to server owners, and a useful **API** for plugin developers to add more complex damage-related systems! An equal amount of effort is put into each side to ensure this plugin is the last damage manager you need!

A big part of this plugin is that it's not intrusive to the Bukkit's already existing damage logic and events system. It builds on top of it, offering its own events for developers to use the more advanced features, along with being as compatible as possible with other damage-related plugins. In the case that you don't want any extra features other than damage logs, you can disable it all in the config!

## Development

### Planned Non-API features:
_These were added early on in development. Most of these are in 1.0._

- [ ] Deaths Plus
  - [x] Assist Tag (on Vanilla + Custom death messages)
  - [ ] Custom Death Messages
  - [x] Ping on kill/assist.
- [x] Visual Health-bars
  - [x] A variety of options? :O
- [x] Damage indicators (Particle-like Text appearing after each hit)
- [ ] In-Chat log viewing.
  - Post PvP, this may be nice to check.

### Planned API Features:
_As of 1.0, these are not implemented yet:_

- [ ] Extra entity damage events
  - [ ] EntityDamagedByEntityEvent (Same as vanilla + root cause + other damage features)
  - [ ] EntityBurnedByEntityEvent (Who placed the fire source/lava)
  - [ ] EntityExplodedByEntityEvent
  - [ ] EntityMagicDamageByEntityEvent (Who threw the potion? Who poisoned them??)
- [ ] Managers + DamageTraces for the above events.
- [ ] Assist Utilities (Getting all the current assists)