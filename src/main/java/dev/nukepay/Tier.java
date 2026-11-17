package dev.nukepay;

/** Which effect plays at which amount. Edit the thresholds here (amounts are in coins). */
enum Tier {
    NONE("", 0, 0xFFFFFF),
    COIN_BURST("Coin Burst", 1_000_000, 0xFFD54A),
    FIREWORKS("Fireworks", 10_000_000, 0xFF7AD9),
    LIGHTNING("Lightning Strike", 100_000_000, 0x9CC4FF),
    NUKE("Nuke", 1_000_000_000, 0xFF8A1F),
    ORBITAL_LASER("Orbital Laser", 10_000_000_000.0, 0x5CE1FF),
    SUPERNOVA("God Tier", 100_000_000_000.0, 0xFFFFFF);

    final String label;
    final double threshold;
    final int color;

    Tier(String label, double threshold, int color) {
        this.label = label;
        this.threshold = threshold;
        this.color = color;
    }

    static Tier of(double amount) {
        Tier best = NONE;
        for (Tier t : values()) {
            if (t != NONE && amount >= t.threshold) best = t;
        }
        return best;
    }

    Effect create() {
        return switch (this) {
            case COIN_BURST -> new Effects.CoinBurst();
            case FIREWORKS -> new Effects.Fireworks();
            case LIGHTNING -> new Effects.Lightning();
            case NUKE -> new Effects.Nuke();
            case ORBITAL_LASER -> new Effects.OrbitalLaser();
            case SUPERNOVA -> new Effects.Supernova();
            default -> null;
        };
    }
}
