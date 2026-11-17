package dev.nukepay;

import java.util.Random;
import net.minecraft.client.gui.GuiGraphics;

/** The effect library. Tweak durations, colors and sounds here. */
final class Effects {
    private Effects() {}

    private static final float TAU = (float) (Math.PI * 2);

    // ------------------------------------------------------------------ 1M: COIN BURST
    static final class CoinBurst extends Effect {
        static final int N = 70;
        final float[] vx = new float[N], vy = new float[N], spin = new float[N], size = new float[N];

        CoinBurst() {
            super(3.6f);
            for (int i = 0; i < N; i++) {
                vx[i] = (rnd.nextFloat() - 0.5f) * 0.8f;
                vy[i] = -(0.95f + rnd.nextFloat() * 0.8f);
                spin[i] = 6f + rnd.nextFloat() * 10f;
                size[i] = 4f + rnd.nextFloat() * 4f;
            }
            cue(0f, "entity.player.levelup", 0.8f, 1.3f);
            for (int i = 0; i < 6; i++) cue(0.1f + i * 0.12f, "entity.experience_orb.pickup", 0.9f, 0.9f + i * 0.08f);
        }

        @Override
        void render(GuiGraphics g, float t, int w, int h) {
            float f = fade(t, 0.6f);
            for (int i = 0; i < N; i++) {
                float x = w * (0.5f + vx[i] * t);
                float y = h * (1.05f + vy[i] * t + 0.95f * t * t);
                if (y > h + 20) continue;
                float sw = Math.max(1f, size[i] * Math.abs((float) Math.cos(t * spin[i])));
                float sh = size[i];
                g.fill((int) (x - sw), (int) (y - sh), (int) (x + sw), (int) (y + sh), Draw.argb(0xE8A800, f));
                g.fill((int) (x - sw * 0.5f), (int) (y - sh * 0.5f), (int) (x + sw * 0.5f), (int) (y + sh * 0.5f), Draw.argb(0xFFF0A0, f));
            }
        }
    }

    // ------------------------------------------------------------------ 10M: FIREWORKS
    static final class Fireworks extends Effect {
        static final int[] PAL = {0xFF4040, 0xFFD040, 0x40FF70, 0x40C8FF, 0xC060FF, 0xFF80D0, 0xFFFFFF};
        static final int BURSTS = 8, SPARKS = 36;
        final float[] bx = new float[BURSTS], by = new float[BURSTS], bt = new float[BURSTS];
        final int[] bc = new int[BURSTS];

        Fireworks() {
            super(6.0f);
            cue(0f, "entity.player.levelup", 0.8f, 1.1f);
            for (int i = 0; i < BURSTS; i++) {
                bx[i] = 0.12f + rnd.nextFloat() * 0.76f;
                by[i] = 0.14f + rnd.nextFloat() * 0.40f;
                bt[i] = 0.8f + i * 0.5f;
                bc[i] = PAL[rnd.nextInt(PAL.length)];
                cue(bt[i] - 0.75f, "entity.firework_rocket.launch", 0.8f, 1f);
                cue(bt[i], "entity.firework_rocket.large_blast", 1f, 0.9f + rnd.nextFloat() * 0.3f);
                cue(bt[i] + 0.15f, "entity.firework_rocket.twinkle", 0.8f, 1f);
            }
        }

        @Override
        void render(GuiGraphics g, float t, int w, int h) {
            float f = fade(t, 0.5f);
            float life = 1.5f;
            for (int i = 0; i < BURSTS; i++) {
                float cx = bx[i] * w, cy = by[i] * h;
                float launch = bt[i] - 0.75f;
                if (t >= launch && t < bt[i]) {
                    float p = (t - launch) / 0.75f;
                    float e = 1 - (1 - p) * (1 - p);
                    float y = h + (cy - h) * e;
                    g.fill((int) cx - 1, (int) y, (int) cx + 1, (int) y + 10, Draw.argb(0xFFE9A0, f));
                    g.fill((int) cx, (int) y + 10, (int) cx + 1, (int) y + 24, Draw.argb(0xFF9030, 0.6f * f));
                } else if (t >= bt[i]) {
                    float tb = t - bt[i];
                    if (tb >= life) continue;
                    if (tb < 0.12f) Draw.disc(g, cx, cy, 6 + 30 * (1 - tb / 0.12f), Draw.argb(0xFFFFFF, 0.8f * f));
                    float a = (1 - tb / life) * f;
                    float reach = Math.min(w, h) * 0.42f;
                    for (int k = 0; k < SPARKS; k++) {
                        float ang = k * TAU / SPARKS;
                        float s = 0.55f + 0.45f * (((k * 7) % 5) / 4f);
                        float r = reach * s * (1f - (float) Math.exp(-3.5 * tb));
                        float x = cx + (float) Math.cos(ang) * r;
                        float y = cy + (float) Math.sin(ang) * r + 55f * tb * tb;
                        int col = (k % 3 == 0) ? 0xFFFFFF : bc[i];
                        g.fill((int) x - 2, (int) y - 2, (int) x + 2, (int) y + 2, Draw.argb(col, a));
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ 100M: LIGHTNING
    static final class Lightning extends Effect {
        static final int STRIKES = 3, SEG = 14;
        final float[] st = {0.3f, 1.15f, 1.9f};
        final float[] sx = new float[STRIKES];
        final long[] seed = new long[STRIKES];

        Lightning() {
            super(3.8f);
            for (int i = 0; i < STRIKES; i++) {
                sx[i] = 0.2f + rnd.nextFloat() * 0.6f;
                seed[i] = rnd.nextLong();
                cue(st[i], "entity.lightning_bolt.thunder", 1.6f, 0.9f + 0.1f * i);
                cue(st[i] + 0.05f, "entity.lightning_bolt.impact", 1.2f, 1f);
            }
        }

        @Override
        float shake(float t) {
            float s = 0f;
            for (int i = 0; i < STRIKES; i++) {
                float tb = t - st[i];
                if (tb > 0 && tb < 0.5f) s = Math.max(s, 7f * (1 - tb / 0.5f));
            }
            return s;
        }

        @Override
        void render(GuiGraphics g, float t, int w, int h) {
            float f = fade(t, 0.4f);
            g.fill(0, 0, w, h, Draw.argb(0x0A1230, 0.35f * f * clamp(t / 0.3f)));
            for (int i = 0; i < STRIKES; i++) {
                float tb = t - st[i];
                if (tb < 0 || tb > 1.0f) continue;
                float flash = 0.6f * (float) Math.exp(-tb * 7);
                if (flash > 0.01f) g.fill(0, 0, w, h, Draw.argb(0xDDE8FF, flash * f));
                if (tb < 0.45f && ((int) (tb * 40)) % 7 != 3) bolt(g, w, h, sx[i], seed[i], f);
            }
        }

        private void seg(GuiGraphics g, float x1, float y1, float x2, float y2, float scale, float f) {
            Draw.line(g, x1, y1, x2, y2, 12 * scale, Draw.argb(0x6F90FF, 0.25f * f));
            Draw.line(g, x1, y1, x2, y2, 5 * scale, Draw.argb(0xBBD0FF, 0.55f * f));
            Draw.line(g, x1, y1, x2, y2, 2 * scale, Draw.argb(0xFFFFFF, f));
        }

        private void bolt(GuiGraphics g, int w, int h, float x0, long seed, float f) {
            Random r = new Random(seed);
            float px = x0 * w, py = 0f, endY = h * 0.92f;
            for (int j = 1; j <= SEG; j++) {
                float ny = endY * j / SEG;
                float nx = px + (r.nextFloat() - 0.5f) * w * 0.09f;
                seg(g, px, py, nx, ny, 1f, f);
                if (j >= 3 && j <= 10 && r.nextFloat() < 0.35f) {
                    float bx = nx, by = ny, dir = r.nextBoolean() ? 1f : -1f;
                    for (int k = 0; k < 4; k++) {
                        float bx2 = bx + dir * (10 + r.nextFloat() * w * 0.04f);
                        float by2 = by + 8 + r.nextFloat() * h * 0.06f;
                        seg(g, bx, by, bx2, by2, 0.5f, f);
                        bx = bx2;
                        by = by2;
                    }
                }
                px = nx;
                py = ny;
            }
            Draw.disc(g, px, endY, 26, Draw.argb(0xBBD0FF, 0.35f * f));
            Draw.disc(g, px, endY, 12, Draw.argb(0xFFFFFF, 0.8f * f));
        }
    }

    // ------------------------------------------------------------------ 1B: NUKE
    static final class Nuke extends Effect {
        static final int EMBERS = 90;
        final float[] ex = new float[EMBERS], ev = new float[EMBERS], ep = new float[EMBERS];

        Nuke() {
            super(8.0f);
            for (int i = 0; i < EMBERS; i++) {
                ex[i] = rnd.nextFloat() * 2f - 1f;
                ev[i] = 0.15f + rnd.nextFloat() * 0.45f;
                ep[i] = rnd.nextFloat() * TAU;
            }
            cue(0f, "entity.generic.explode", 2f, 0.55f);
            cue(0.2f, "entity.wither.break_block", 1.5f, 0.6f);
            cue(0.25f, "entity.generic.explode", 2f, 0.4f);
            cue(0.9f, "entity.lightning_bolt.thunder", 1.5f, 0.5f);
            cue(2.5f, "entity.generic.explode", 1f, 0.3f);
        }

        @Override
        float shake(float t) {
            return t < 4f ? 16f * (float) Math.exp(-t / 1.1f) : 0f;
        }

        @Override
        void render(GuiGraphics g, float t, int w, int h) {
            float f = fade(t, 1.4f);
            float gy = h * 0.86f;
            float cx = w / 2f;

            // red-brown sky tint
            g.fill(0, 0, w, h, Draw.argb(0x3A0A00, 0.45f * f));

            // glow along the horizon
            float glow = Math.min(1f, t * 2f);
            for (int i = 0; i < 14; i++) {
                int y2 = (int) (gy - i * h * 0.015f);
                int y1 = (int) (gy - (i + 1) * h * 0.015f);
                g.fill(0, y1, w, y2, Draw.argb(0xFF7A1A, 0.22f * (1 - i / 14f) * f * glow));
            }

            // shockwave
            float sr = (t - 0.1f) * w * 0.85f;
            if (t > 0.1f && sr < w * 1.2f) {
                float a = Math.max(0f, 1f - sr / (w * 1.1f));
                Draw.ring(g, cx, gy, sr, 6 + sr * 0.04f, Draw.argb(0xFFE0A0, a * f));
            }

            // mushroom cloud
            float p = clamp((t - 0.2f) / 3.2f);
            float e = 1f - (1f - p) * (1f - p);
            if (p > 0f) {
                float height = h * 0.52f * e;
                float cool = clamp((t - 0.8f) / 5f);
                int hot = 0xFFB030, mid = 0xE05010, smoke = 0x5A4A42;

                float sw = w * 0.035f * (1f + 0.8f * e);
                for (int y = 0; y < (int) height; y += 3) {
                    float k = y / Math.max(1f, height);
                    float wob = (float) Math.sin(y * 0.045f + t * 2.5f) * sw * 0.25f;
                    float half = sw * (1.3f - 0.5f * k) + wob;
                    int col = Draw.lerp(Draw.lerp(hot, mid, k), smoke, clamp(cool * 1.3f - (1f - k) * 0.3f));
                    g.fill((int) (cx - half), (int) (gy - y - 3), (int) (cx + half), (int) (gy - y), Draw.argb(col, 0.95f * f));
                }

                float R = h * 0.2f * e;
                float cy = gy - height - R * 0.15f;
                int capCol = Draw.lerp(Draw.lerp(hot, mid, 0.5f), smoke, cool);
                for (int i = 0; i < 9; i++) {
                    float ang = i * TAU / 9f + t * 0.3f;
                    float lx = cx + (float) Math.cos(ang) * R * 1.05f;
                    float ly = cy + (float) Math.sin(ang) * R * 0.55f;
                    float lr = R * 0.5f * (0.85f + 0.15f * (float) Math.sin(t * 2f + i));
                    Draw.disc(g, lx, ly, lr, Draw.argb(Draw.lerp(capCol, smoke, 0.25f * (i % 3)), 0.92f * f));
                }
                Draw.ellipse(g, cx, cy, R * 1.1f, R * 0.7f, Draw.argb(capCol, 0.95f * f));
                Draw.ellipse(g, cx, cy + R * 0.15f, R * 0.6f, R * 0.38f,
                        Draw.argb(Draw.lerp(0xFFF0B0, mid, cool * 1.5f), (1f - cool) * 0.9f * f));
                Draw.ellipse(g, cx, cy + R * 0.55f, R * 1.5f, R * 0.18f, Draw.argb(Draw.lerp(mid, smoke, cool), 0.75f * f));
            }

            // embers
            float tb = t - 0.15f;
            if (tb > 0f) {
                for (int i = 0; i < EMBERS; i++) {
                    float x = cx + ex[i] * w * 0.22f * Math.min(1f, tb * 0.8f) + (float) Math.sin(tb * 3f + ep[i]) * 8f;
                    float y = gy - ev[i] * h * tb;
                    if (y < 0) continue;
                    float a = clamp(1f - tb / 6f) * f;
                    g.fill((int) x, (int) y, (int) x + 3, (int) y + 3, Draw.argb(i % 2 == 0 ? 0xFFD060 : 0xFF6A10, a));
                }
            }

            // ground
            g.fill(0, (int) gy, w, h, Draw.argb(0x120804, 0.92f * f));

            // initial white-out
            float fl = t < 0.15f ? 1f : Math.max(0f, 1f - (t - 0.15f) / 1.0f);
            if (fl > 0.01f) g.fill(0, 0, w, h, Draw.argb(0xFFFFFF, fl));
        }
    }

    // ------------------------------------------------------------------ 10B: ORBITAL LASER
    static final class OrbitalLaser extends Effect {
        static final int SPARKS = 40;
        final float[] sa = new float[SPARKS], ss = new float[SPARKS], sph = new float[SPARKS];

        OrbitalLaser() {
            super(6.5f);
            for (int i = 0; i < SPARKS; i++) {
                sa[i] = (float) (-Math.PI * (0.05 + 0.9 * rnd.nextFloat()));
                ss[i] = 150f + rnd.nextFloat() * 300f;
                sph[i] = rnd.nextFloat();
            }
            cue(0f, "block.beacon.power_select", 1.5f, 0.6f);
            cue(0.8f, "block.beacon.activate", 1.5f, 0.7f);
            cue(1.6f, "entity.warden.sonic_boom", 2f, 0.8f);
            cue(1.7f, "entity.generic.explode", 1.5f, 0.6f);
            cue(5.0f, "block.beacon.deactivate", 1.5f, 0.8f);
        }

        @Override
        float shake(float t) {
            if (t < 1.6f) return 0f;
            return (t < 5f ? 4f : 0f) + 12f * (float) Math.exp(-(t - 1.6f) * 4f);
        }

        @Override
        void render(GuiGraphics g, float t, int w, int h) {
            float f = fade(t, 1.3f);
            float cx = w / 2f, gy = h * 0.82f;

            g.fill(0, 0, w, h, Draw.argb(0x000814, 0.35f * f * clamp(t / 0.5f)));

            // targeting reticle
            if (t < 1.7f) {
                float lock = clamp(t / 1.5f);
                boolean on = ((int) (t * 7)) % 2 == 0 || t > 1.2f;
                float a = (on ? 0.8f : 0.3f) * f;
                for (int i = 0; i < 3; i++) {
                    float r = 40 + (1 - lock) * (220 + i * 90) + i * 28;
                    Draw.ring(g, cx, gy, r, 3, Draw.argb(0xFF4040, a));
                }
                Draw.line(g, cx - 90, gy, cx + 90, gy, 2, Draw.argb(0xFF4040, a));
                Draw.line(g, cx, gy - 90, cx, gy + 90, 2, Draw.argb(0xFF4040, a));
            }

            // beam
            if (t >= 1.6f && t < 5.2f) {
                float bt = t - 1.6f;
                float grow = clamp(bt / 0.25f);
                float shrink = t > 4.6f ? clamp((5.2f - t) / 0.6f) : 1f;
                float bw = w * 0.045f * grow * shrink * (1f + 0.08f * (float) Math.sin(t * 60f));
                float[] mult = {3.2f, 2.0f, 1.2f, 0.5f};
                int[] cols = {0x2060FF, 0x40C0FF, 0xB0F0FF, 0xFFFFFF};
                float[] alp = {0.25f, 0.4f, 0.7f, 1f};
                for (int i = 0; i < 4; i++) {
                    float lw = bw * mult[i] / 2f;
                    g.fill((int) (cx - lw), 0, (int) (cx + lw), (int) gy, Draw.argb(cols[i], alp[i] * f));
                }
                Draw.disc(g, cx, gy, bw * 3.5f, Draw.argb(0x60D0FF, 0.35f * f * shrink));
                Draw.disc(g, cx, gy, bw * 2f, Draw.argb(0xFFFFFF, 0.6f * f * shrink));
                for (int k = 0; k < 4; k++) {
                    float phase = ((bt + k * 0.45f) % 1.8f) / 1.8f;
                    Draw.ring(g, cx, gy, phase * w * 0.5f, 5, Draw.argb(0x80E0FF, (1f - phase) * 0.7f * shrink * f));
                }
                for (int i = 0; i < SPARKS; i++) {
                    float ph = (bt * 0.9f + sph[i]) % 1f;
                    float x = cx + (float) Math.cos(sa[i]) * ss[i] * ph;
                    float y = gy + (float) Math.sin(sa[i]) * ss[i] * ph + 260f * ph * ph;
                    g.fill((int) x, (int) y, (int) x + 3, (int) y + 3, Draw.argb(0xCFF6FF, (1f - ph) * shrink * f));
                }
                float fl = clamp(1f - bt / 0.35f);
                if (fl > 0.01f) g.fill(0, 0, w, h, Draw.argb(0xFFFFFF, 0.8f * fl));
            }

            g.fill(0, (int) gy, w, h, Draw.argb(0x04101C, 0.6f * f));
        }
    }

    // ------------------------------------------------------------------ 100B: GOD TIER SUPERNOVA
    static final class Supernova extends Effect {
        static final int N = 130;
        final float[] ai = new float[N], si = new float[N], r0 = new float[N], ph = new float[N];

        Supernova() {
            super(10.0f);
            for (int i = 0; i < N; i++) {
                ai[i] = rnd.nextFloat() * TAU;
                si[i] = 0.5f + rnd.nextFloat();
                r0[i] = 250f + rnd.nextFloat() * 400f;
                ph[i] = rnd.nextFloat();
            }
            cue(0f, "block.end_portal.spawn", 1.2f, 0.6f);
            cue(0f, "block.beacon.power_select", 1f, 0.5f);
            cue(2.6f, "entity.generic.explode", 2f, 0.35f);
            cue(2.6f, "entity.ender_dragon.death", 1.2f, 0.8f);
            cue(2.9f, "ui.toast.challenge_complete", 1.5f, 1f);
            cue(3.4f, "entity.player.levelup", 1f, 0.7f);
            cue(4.5f, "entity.firework_rocket.twinkle", 1f, 1f);
        }

        @Override
        float shake(float t) {
            if (t < 2.6f) return 3f * clamp(t / 2.6f);
            return 18f * (float) Math.exp(-(t - 2.6f) / 1.4f);
        }

        @Override
        void render(GuiGraphics g, float t, int w, int h) {
            float cx = w / 2f, cy = h / 2f;
            float maxd = Math.max(w, h);

            if (t < 2.6f) {
                float pre = t / 2.6f;
                g.fill(0, 0, w, h, Draw.argb(0x000000, 0.55f * pre));
                for (int i = 0; i < N; i++) {
                    float frac = (pre * (0.7f + 0.6f * si[i]) * 2.2f + ph[i]) % 1f;
                    float r = r0[i] * (1f - frac);
                    float x = cx + (float) Math.cos(ai[i]) * r;
                    float y = cy + (float) Math.sin(ai[i]) * r;
                    g.fill((int) x, (int) y, (int) x + 3, (int) y + 3, Draw.argb(Draw.lerp(0xFFD040, 0xFFFFFF, frac), frac));
                }
                float cr = 6 + pre * pre * 70 + (float) Math.sin(t * 28) * 3 * pre;
                Draw.disc(g, cx, cy, cr * 2.2f, Draw.argb(0xFFC030, 0.25f));
                Draw.disc(g, cx, cy, cr * 1.4f, Draw.argb(0xFFE070, 0.5f));
                Draw.disc(g, cx, cy, cr, Draw.argb(0xFFFFFF, 0.95f));
                return;
            }

            float tb = t - 2.6f;
            float f = fade(t, 1.6f);

            g.fill(0, 0, w, h, Draw.argb(0xFFB000, 0.22f * Math.max(0f, 1f - tb / 5f) * f));

            // rotating rainbow rays
            int rays = 18;
            for (int i = 0; i < rays; i++) {
                float ang = i * TAU / rays + tb * 0.35f;
                float len = Math.min(1f, tb * 1.1f) * maxd * 0.75f;
                int col = Draw.hsv(i / (float) rays + tb * 0.15f, 0.55f, 1f);
                float a = 0.5f * f * clamp(1.2f - tb / 7f);
                float c = (float) Math.cos(ang), s = (float) Math.sin(ang);
                Draw.line(g, cx + c * 60, cy + s * 60, cx + c * len, cy + s * len, 10f * (1f - 0.5f * clamp(tb / 6f)), Draw.argb(col, a));
            }

            // shockwave rings
            for (int k = 0; k < 3; k++) {
                float r = tb * maxd * (0.45f + 0.22f * k);
                if (r < maxd * 1.1f) {
                    float a = clamp(1f - r / (maxd * 1.1f));
                    Draw.ring(g, cx, cy, r, 10 + k * 7, Draw.argb(Draw.hsv(k * 0.3f + tb * 0.2f, 0.5f, 1f), a * 0.8f * f));
                }
            }

            // star field flying outward
            for (int i = 0; i < N; i++) {
                float dist = tb * si[i] * maxd * 0.28f + 20f;
                float x = cx + (float) Math.cos(ai[i]) * dist;
                float y = cy + (float) Math.sin(ai[i]) * dist;
                float a = clamp(1f - tb / 6.5f) * f;
                g.fill((int) x, (int) y, (int) x + 3, (int) y + 3, Draw.argb(Draw.hsv(ph[i] + tb * 0.3f, 0.4f, 1f), a));
            }

            // pulsing core
            float pulse = 1f + 0.12f * (float) Math.sin(tb * 9f);
            float cr = (70f + 40f * (float) Math.exp(-tb * 0.8f)) * pulse;
            Draw.disc(g, cx, cy, cr * 2.4f, Draw.argb(0xFFB000, 0.22f * f));
            Draw.disc(g, cx, cy, cr * 1.5f, Draw.argb(0xFFE070, 0.5f * f));
            Draw.disc(g, cx, cy, cr, Draw.argb(0xFFFFFF, 0.95f * f));

            float fl = Math.max(0f, 1f - tb / 1.0f);
            if (fl > 0.01f) g.fill(0, 0, w, h, Draw.argb(0xFFFFFF, fl));
        }
    }
}
