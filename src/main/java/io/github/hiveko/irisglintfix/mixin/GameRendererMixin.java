package io.github.hiveko.irisglintfix.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Iris 1.11.7 looks up the enchantment glint textures while it binds the samplers of a glint
 * shader, which happens inside an open render pass. If a glint texture is not loaded yet,
 * TextureManager.getTexture creates and uploads it right there, and 26.3 does not allow
 * uploads while a render pass is open ("Close the existing render pass before performing
 * additional commands"). Looking both textures up at the start of every frame, before any
 * render pass, makes sure they are always loaded by the time Iris needs them. When they are
 * already loaded this is just two map lookups.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void irisGlintFix$loadGlintTextures(CallbackInfo ci) {
        TextureManager textures = Minecraft.getInstance().getTextureManager();
        textures.getTexture(ItemFeatureRenderer.ENCHANTED_GLINT_ITEM);
        textures.getTexture(ItemFeatureRenderer.ENCHANTED_GLINT_ARMOR);
    }
}
