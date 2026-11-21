package io.github.hiveko.irisglintfix.mixin;

import io.github.hiveko.irisglintfix.ImprovedTransparencyRestore;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Notices when Iris switches Improved Transparency off because shaders are enabled. */
@Mixin(OptionInstance.class)
public abstract class OptionInstanceMixin {
    @Inject(method = "set", at = @At("HEAD"))
    private void irisGlintFix$beforeSet(Object value, CallbackInfo ci) {
        ImprovedTransparencyRestore.beforeSet((OptionInstance<?>) (Object) this, value);
    }
}
