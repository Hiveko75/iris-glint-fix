# Iris Glint Fix

A small Fabric mod for Minecraft 26.3 that fixes [Iris](https://modrinth.com/mod/iris) 1.11.7 sometimes
crashing while it loads the enchantment glint texture
([IrisShaders/Iris#3390](https://github.com/IrisShaders/Iris/issues/3390)):

```
java.lang.IllegalStateException: Close the existing render pass before performing additional commands
```

## Why it crashes

When Iris draws something enchanted, it binds the glint texture by looking it up in Minecraft's
texture manager (`ExtendedShader.getGlintSamplerFor`). If the texture is not loaded yet, the texture
manager creates and uploads it on the spot. Iris does this while a render pass is open, and 26.3 does not
allow uploads then.

## What this mod does

At the start of every frame, before any render pass, it looks up the item and armor glint textures
(`GameRenderer.render`, one mixin). They are then always loaded when Iris binds them. When they are
already loaded this is just two map lookups. Iris itself is not changed.

Needs Iris; Minecraft 26.3; Java 25. License: MIT.
