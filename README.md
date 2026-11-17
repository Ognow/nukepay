# NukePay (Fabric, Minecraft 1.21.11, client-only)

Big screen effects when someone pays you in chat. Only you see/hear them.

| Amount | Effect |
|--------|--------|
| 1M+    | Coin Burst |
| 10M+   | Fireworks |
| 100M+  | Lightning Strike |
| 1B+    | Nuke (flash, shockwave, mushroom cloud, screen shake) |
| 10B+   | Orbital Laser |
| 100B+  | God Tier supernova |

## Build
1. Install JDK 21 and open this folder in IntelliJ IDEA (or any Gradle setup).
   If there is no `gradlew`, run `gradle wrapper` once, or generate a fresh template at
   https://fabricmc.net/develop (1.21.11, Mojang mappings) and copy `src/` plus these build files into it.
2. Run `./gradlew build`. The jar is in `build/libs/` (use the one without `-sources`).
3. Drop it in `mods/` with Fabric API for 1.21.11.

## Test it
In-game: `/nukepay test 5m`, `/nukepay test 2b`, `/nukepay test 100b`

## Tweak
- Thresholds: `Tier.java`
- Look/sound/duration of each effect: `Effects.java`
- Message wording the mod reacts to: the `PAID` regex in `Payments.java`
