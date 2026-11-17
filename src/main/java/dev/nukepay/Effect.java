package dev.nukepay;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.GuiGraphics;

/** A full-screen effect. render() is stateless: everything is a function of t (seconds since start). */
abstract class Effect {
    record Cue(float time, String sound, float volume, float pitch) {}

    final float duration;
    final List<Cue> cues = new ArrayList<>();
    final Random rnd = new Random();

    Effect(float duration) {
        this.duration = duration;
    }

    /** Schedule a vanilla sound, e.g. cue(0f, "entity.generic.explode", 1.5f, 0.5f). */
    void cue(float time, String sound, float volume, float pitch) {
        cues.add(new Cue(time, sound, volume, pitch));
    }

    abstract void render(GuiGraphics g, float t, int w, int h);

    /** Screen-shake amplitude in GUI pixels at time t. */
    float shake(float t) {
        return 0f;
    }

    static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    /** 1 until the last outTime seconds, then fades to 0. */
    float fade(float t, float outTime) {
        return t > duration - outTime ? clamp((duration - t) / outTime) : 1f;
    }
}
