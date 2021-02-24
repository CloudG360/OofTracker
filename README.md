# Oof Tracker: A Spigot Damage Log API
![Badge: Java](https://img.shields.io/badge/Java-8-red?style=for-the-badge)
![Version: Commons](https://img.shields.io/badge/Version-1.0-blue?style=for-the-badge)
![Supports: Spigot](https://img.shields.io/badge/Spigot-1.16.5-orange?style=for-the-badge)

**Oof Tracker** creates _Damage Lists_ for each entity that is damaged on the server (or queried via the API) which keeps a track of the properties for each damage event that occurs. This allows plugins using it to check for **Assist Kills** from other players easily or even award bonus points for how much damage a player has taken vs how long they have survived!

The Damage Lists are populated with **Damage Traces** which are accessible in the API via two forms of interface. Damage data within these traces can be accessed by generically using keys against the raw data, or by utilising subclasses that provide methods to easily read data. Damage Traces can hold different data depending on the type of damage that is dealt.

## Non-API features:

- [ ] Extra Death Messages (Assits, variety, etc)
  - [ ] Configurable?
- [ ] Instant Respawning.
- [ ] Extra entity damage events
- [ ] In-Chat log viewing.
  - Post PvP, this may be nice to check.