# No Kebab

Prevents paintings with unknown id from being irremediably turned into kebabs.

In vanilla Minecraft, any painting bearing an ID unknown to the registry will be reverted to the `minecraft:kebab` variant. No questions asked.  
If you mess around with mods that add new paintings (especially user-defined paintings), any erroor may cause your existing paintings to be lost upon loading a world.

No-Kebab serves as a safeguard against that. Invalid painting will still appear as Kebab, but they will not forget the painting ID that was originally present in their NBT data, and keep that ID when saving the game. The next time you load that world with the correct set of variants installed, those painting will be restored to their original appearance.

## Compatibility

No-Kebab relies on and preserves the vanilla variant system; it will only work with mods that do not implement their own variant system.  
This should also be compatible with mods that lets you change the variant of an already placed painting; No-Kebab will remember the newly assigned ID so long as the variant is valid.

No-Kebab is inspired from a feature from my experimental [Datapack-driven](https://modrinth.com/mod/dataified-paintings) paintings Mod, but was rewritten from the ground up into something much more stable. 
Dataified-Paintings 1.0 will likeky not be compatible with it, so long as both mods include this feature.
