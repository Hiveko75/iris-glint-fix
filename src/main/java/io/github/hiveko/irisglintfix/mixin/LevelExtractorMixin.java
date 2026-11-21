package io.github.hiveko.irisglintfix.mixin;

import io.github.hiveko.irisglintfix.ImprovedTransparencyRestore;
import net.minecraft.client.renderer.extract.LevelExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Switches Improved Transparency back on at the start of a renderer reload once shaders are off
 * (the same two places where Iris switches it off).
 */
@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
    @Inject(method = "allChanged", at = @At("HEAD"))
    private void irisGlintFix$restoreOnAllChanged(CallbackInfo ci) {
        ImprovedTransparencyRestore.beforeRendererReload();
    }

    @Inject(method = "onResourceManagerReload", at = @At("HEAD"))
    private void irisGlintFix$restoreOnResourceReload(CallbackInfo ci) {
        ImprovedTransparencyRestore.beforeRendererReload();
    }
}
