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

## Improved Transparency after shaders (1.1.0)

Iris turns Minecraft's *Improved Transparency* off whenever a shader pack is enabled, but does not turn
it back on when the shader pack is disabled again. This mod notices when Iris switches it off (only a
change made while shaders are enabled counts, since the player cannot change the option then) and
switches it back on at the next renderer reload without shaders, which Iris starts itself when shaders
are disabled. If Improved Transparency was off before shaders were enabled, nothing changes.

The flag is kept in `config/iris-glint-fix.properties`, so this also works when the game is restarted
while shaders are on.

Needs Iris; Minecraft 26.3; Java 25. License: MIT.
