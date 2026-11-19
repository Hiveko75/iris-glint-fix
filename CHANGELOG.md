# Changelog

## 1.0.0 (2026-11-19, Minecraft 26.3)

Fixes Iris 1.11.7 on Minecraft 26.3 sometimes crashing while it loads the enchantment glint texture ([IrisShaders/Iris#3390](https://github.com/IrisShaders/Iris/issues/3390)):

```
java.lang.IllegalStateException: Close the existing render pass before performing additional commands
```

**Cause:** when Iris draws something enchanted, `ExtendedShader.getGlintSamplerFor` gets the glint texture from `TextureManager.getTexture`. If the texture is not loaded yet, the texture manager creates and uploads it right then. That happens while a render pass is open, and 26.3 does not allow uploads inside a render pass.

**Fix:** one mixin at the start of `GameRenderer.render` looks up `ENCHANTED_GLINT_ITEM` and `ENCHANTED_GLINT_ARMOR` every frame, before any render pass opens, so both textures are always loaded when Iris binds them. Iris itself is not changed. Once the textures are loaded, the per-frame cost is two map lookups.

Client-side only. Needs Minecraft 26.3, Iris 1.11.7, Fabric Loader 0.17+ and Java 25. MIT licensed.
