package tk.estecka.nokebab.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
@Mixin(SpriteAtlasHolder.class)
public interface ISpriteAtlasHolderMixin 
{
	@Invoker("getSprite")
	Sprite GetSpriteFromID(Identifier objectId);
}
