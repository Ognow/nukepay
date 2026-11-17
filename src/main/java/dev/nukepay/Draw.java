package dev.nukepay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Tiny 2D drawing toolkit built only on GuiGraphics.fill + the pose stack. */
final class Draw {
    private Draw() {}

    /** rgb (0xRRGGBB) + alpha 0..1 -> 0xAARRGGBB */
    static int argb(int rgb, float alpha) {
        int a = Math.max(0, Math.min(255, (int) (alpha * 255f)));
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    static int lerp(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (((c1 >> 16) & 0xFF) * (1 - t) + ((c2 >> 16) & 0xFF) * t);
        int g = (int) (((c1 >> 8) & 0xFF) * (1 - t) + ((c2 >> 8) & 0xFF) * t);
        int b = (int) ((c1 & 0xFF) * (1 - t) + (c2 & 0xFF) * t);
        return (r << 16) | (g << 8) | b;
    }

    static int hsv(float h, float s, float v) {
        h = h - (float) Math.floor(h);
        int i = (int) (h * 6);
        float f = h * 6 - i, p = v * (1 - s), q = v * (1 - f * s), t = v * (1 - (1 - f) * s);
        float r, g, b;
        switch (i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    static void ellipse(GuiGraphics g, float cx, float cy, float rx, float ry, int color) {
        if (rx < 1 || ry < 1) return;
        int step = Math.max(1, (int) (ry / 50f));
        for (int dy = -(int) ry; dy < (int) ry; dy += step) {
            float k = dy / ry;
            float w = rx * (float) Math.sqrt(Math.max(0f, 1f - k * k));
            g.fill((int) (cx - w), (int) (cy + dy), (int) (cx + w), (int) (cy + dy + step), color);
        }
    }

    static void disc(GuiGraphics g, float cx, float cy, float r, int color) {
        ellipse(g, cx, cy, r, r, color);
    }

    static void ring(GuiGraphics g, float cx, float cy, float r, float thick, int color) {
        float ro = r + thick / 2f;
        float ri = Math.max(0f, r - thick / 2f);
        if (ro < 1) return;
        int step = Math.max(1, (int) (ro / 70f));
        for (int dy = -(int) ro; dy < (int) ro; dy += step) {
            float wo = (float) Math.sqrt(Math.max(0f, ro * ro - (float) dy * dy));
            int y = (int) (cy + dy);
            if (Math.abs(dy) < ri) {
                float wi = (float) Math.sqrt(Math.max(0f, ri * ri - (float) dy * dy));
                g.fill((int) (cx - wo), y, (int) (cx - wi), y + step, color);
                g.fill((int) (cx + wi), y, (int) (cx + wo), y + step, color);
            } else {
                g.fill((int) (cx - wo), y, (int) (cx + wo), y + step, color);
            }
        }
    }

    /** One rotated fill = one cheap straight line / beam of any angle. */
    static void line(GuiGraphics g, float x1, float y1, float x2, float y2, float thick, int color) {
        float dx = x2 - x1, dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.5f) return;
        var pose = g.pose();
        pose.pushMatrix();
        pose.translate(x1, y1);
        pose.rotate((float) Math.atan2(dy, dx));
        int th = Math.max(1, Math.round(thick / 2f));
        g.fill(-th, -th, (int) len + th, th, color);
        pose.popMatrix();
    }

    static void text(GuiGraphics g, String s, float x, float y, float scale, int color) {
        Font font = Minecraft.getInstance().font;
        var pose = g.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(scale, scale);
        g.drawCenteredString(font, s, 0, -font.lineHeight / 2, color);
        pose.popMatrix();
    }
}
