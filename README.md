# No Kebab

Prevents paintings with unknown IDs from being irremediably turned into kebabs.

In vanilla Minecraft, any painting bearing an ID unknown to the registry will be reverted to the `minecraft:kebab` variant. No questions asked.  
If you mess around with mods that add new paintings (especially user-defined paintings), any error may cause your existing paintings to be lost upon loading a world.

No-Kebab serves as a safeguard against that. Invalid paintings will still appear as Kebab, but they will not forget the painting ID that was originally present in their NBT data, and keep that ID when saving the game. The next time you load that world with the correct set of variants installed, those painting will be restored to their original appearance.

## Environment
Core functionalities are **fully server-side**.

Client-side installation only adds cosmetic changes to invalid painting, to makes them stand out and reveal their ID. Clients without the mod installed will still see invalid paintings as Kebabs.

## Command

The command `/nokebab migrate <mode> <source> <destination>` can be used to change the variant of existing paintings in bulk. It requires a permisssion level of 3 (Admin).

**This will only change the variant of placed paintings in currently loaded chunks** Paintings in item form or in unloaded chunks will not be affected.

If a migration would result in the painting no longer being able to fit, this painting will be skipped with an error message.

### Synopsis
`/nokebab migrate <mode> <source> <destination>`

`<source>` is the variant of paintings that should be migrated. `<destination>` is the variant they will be replaced with. The specifics vary depending on the mode.

`<mode>` can be either `literal` or `regex`.
"Literal" will seek paintings that exactly match the source, and change them all to the same variant.
"Regex" allows the use of Regular Expressions, so the source can match multiple variants, and the destination can use substitution variables.

For example, the command to change the namespace of multiple paintings would be:
```
/nokebab migrate regex "oldspace:(.*)" "newspace:$1"
```

## Compatibility

No-Kebab relies on and preserves the vanilla variant system; it will only work with mods that do not implement their own variant system.  
This should also be compatible with mods that lets you change the variant of an already placed painting.

No-Kebab is based off a feature from my experimental [Datapack-driven](https://modrinth.com/mod/dataified-paintings) paintings Mod, but was rewritten from the ground up into something much more stable. 
Dataified-Paintings 1.0 will likeky not be compatible with it, so long as both mods include this feature.
