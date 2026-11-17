package dev.nukepay;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/** Runs one effect at a time; extra payments wait in a short queue. */
final class AlertManager {
    private AlertManager() {}

    private static final int MAX_QUEUE = 6;
    private static final Random RND = new Random();

    private static final class Active {
        final Effect effect;
        final Tier tier;
        final Payments.Payment payment;
        final long startNanos = System.nanoTime();
        int nextCue = 0;

        Active(Effect effect, Tier tier, Payments.Payment payment) {
            this.effect = effect;
            this.tier = tier;
            this.payment = payment;
            effect.cues.sort((a, b) -> Float.compare(a.time(), b.time()));
        }
    }

    private static final Deque<Active> QUEUE = new ArrayDeque<>();
    private static Active current;

    static void trigger(Payments.Payment payment) {
        Tier tier = Tier.of(payment.amount());
        Effect effect = tier.create();
        if (effect == null) return;
        if (QUEUE.size() >= MAX_QUEUE) QUEUE.pollFirst();
        QUEUE.addLast(new Active(effect, tier, payment));
    }

    static void render(GuiGraphics g) {
        if (current == null) current = QUEUE.pollFirst();
        if (current == null) return;

        float t = (System.nanoTime() - current.startNanos) / 1_000_000_000f;
        if (t > current.effect.duration) {
            current = QUEUE.pollFirst();
            if (current == null) return;
            t = 0f;
        }

        // sound cues
        while (current.nextCue < current.effect.cues.size()
                && current.effect.cues.get(current.nextCue).time() <= t) {
            Effect.Cue cue = current.effect.cues.get(current.nextCue++);
            play(cue.sound(), cue.volume(), cue.pitch());
        }

        int w = g.guiWidth();
        int h = g.guiHeight();

        var pose = g.pose();
        pose.pushMatrix();
        float s = current.effect.shake(t);
        if (s > 0.01f) {
            pose.translate((RND.nextFloat() - 0.5f) * 2f * s, (RND.nextFloat() - 0.5f) * 2f * s);
        }
        current.effect.render(g, t, w, h);
        pose.popMatrix();

        banner(g, current, t, w, h);
    }

    private static void banner(GuiGraphics g, Active a, float t, int w, int h) {
        float in = Effect.clamp(t / 0.35f);
        float out = Effect.clamp((a.effect.duration - t) / 0.6f);
        float alpha = Math.min(in, out);
        if (alpha < 0.03f) return;

        float pop = 1f + 0.35f * (1f - in) * (1f - in);
        int color = a.tier == Tier.SUPERNOVA ? Draw.hsv((t * 0.4f) % 1f, 0.5f, 1f) : a.tier.color;
        String line = a.payment.name() + " paid you $" + Payments.compact(a.payment.amount());

        Draw.text(g, a.tier.label.toUpperCase(), w / 2f, h * 0.17f, 2.2f * pop, Draw.argb(color, alpha));
        Draw.text(g, line, w / 2f, h * 0.17f + 26f, 1.6f, Draw.argb(0xFFFFFF, alpha));
    }

    private static void play(String id, float volume, float pitch) {
        try {
            SoundEvent event = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.withDefaultNamespace(id));
            if (event != null) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, pitch, volume));
            }
        } catch (Exception ignored) {
            // never let a sound problem break the HUD
        }
    }
}
