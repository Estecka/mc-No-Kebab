# No Kebab
## Overview
In vanilla Minecraft, when a painting is loaded with an ID that doesn't match any existing variant, it will be automatically replaced with another one. No questions asked. If you mess around with data-driven paintings, any error in your datapacks may cause already-placed paintings to be lost upon loading a world.

No-Kebab serves as a safeguard against that. Invalid paintings will still fall back to a different variant, but they will remember the variant ID that was originally present in their NBT data, and keep that ID when saving the game. The next time you load that world with the correct set of variants installed, those painting will be restored to their original appearance.

Trivia: Prior to MC 1.21, invalid paintings would invariably be reverted to the `minecraft:kebab` variant, hence the mod's name.

## Environment
Core functionalities are **fully server-side**.

Client-side is required only if the **Missingno Display** feature is enabled.

## Missingno Display
This changes the appearence of invalid paintings, making them stand out, and revealing their ID.

By default, it will be **disabled on dedicated server**, and **enabled on integrated servers** (singleplayer and LA?). 
This can be toggled in the config file `nokebab.properties`, with the option called `customTracker`. This requires a full restart to take effect.

## Migration
The command `/nokebab migrate` can be used to change the variant of existing paintings in bulk. It requires a permisssion level of 3 (Admin).

**This will only change the variant of placed paintings in currently loaded chunks** Paintings in item form or in unloaded chunks will not be affected.

If a migration would result in a painting no longer being able to fit its wall, this painting will be skipped with an error message.

### Synopsis
`/nokebab migrate <mode> <source> <destination>`

`<source>` is the variant of paintings that should be migrated. `<destination>` is the variant they will be replaced with. Their specifics vary depending on the mode.

`<mode>` can be either:
- `Literal`, will seek paintings that exactly match the source, and change them all to the same variant.
- `Regex`, allows the use of Regular Expressions, so `<source>` can match multiple variants, and `<destination>` can use substitution variables.

For example, the command to change the namespace of multiple paintings would be:
```
/nokebab migrate regex "oldspace:(.*)" "newspace:$1"
```
