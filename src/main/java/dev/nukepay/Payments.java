package dev.nukepay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Detects "<name> paid you $<amount>" style messages. Edit PAID if the server's wording differs. */
final class Payments {
    private Payments() {}

    record Payment(String name, double amount) {}

    private static final Pattern PAID = Pattern.compile(
            "^\\W{0,4}(?:\\[[^\\]]{0,40}\\]\\s*)?(?<name>[.*]?\\w{1,16})\\s+(?:has\\s+|just\\s+)?paid\\s+you\\s*:?\\s*\\$?\\s*"
                    + "(?<amount>\\d[\\d,]*(?:\\.\\d+)?)\\s*(?<suffix>thousand|mil(?:lion)?|bil(?:lion)?|tril(?:lion)?|[kmbt])?(?![a-z0-9])",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PLAIN_AMOUNT = Pattern.compile(
            "^\\$?\\s*(\\d[\\d,]*(?:\\.\\d+)?)\\s*(thousand|mil(?:lion)?|bil(?:lion)?|tril(?:lion)?|[kmbt])?$",
            Pattern.CASE_INSENSITIVE);

    static Optional<Payment> parse(String message) {
        if (message == null) return Optional.empty();
        Matcher m = PAID.matcher(message.strip());
        if (!m.find()) return Optional.empty();
        OptionalDouble amount = toAmount(m.group("amount"), m.group("suffix"));
        if (amount.isEmpty()) return Optional.empty();
        return Optional.of(new Payment(m.group("name"), amount.getAsDouble()));
    }

    /** Parses things like "5b", "$2.5M", "750k", "1,000,000". Used by /nukepay test. */
    static OptionalDouble parseAmount(String text) {
        Matcher m = PLAIN_AMOUNT.matcher(text.strip());
        if (!m.matches()) return OptionalDouble.empty();
        return toAmount(m.group(1), m.group(2));
    }

    private static OptionalDouble toAmount(String number, String suffix) {
        try {
            double v = new BigDecimal(number.replace(",", "")).doubleValue();
            double mult = 1;
            if (suffix != null && !suffix.isEmpty()) {
                String s = suffix.toLowerCase();
                if (s.startsWith("th")) mult = 1e3;
                else switch (s.charAt(0)) {
                    case 'k' -> mult = 1e3;
                    case 'm' -> mult = 1e6;
                    case 'b' -> mult = 1e9;
                    case 't' -> mult = 1e12;
                    default -> mult = 1;
                }
            }
            double total = v * mult;
            return Double.isFinite(total) ? OptionalDouble.of(total) : OptionalDouble.empty();
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }

    static String compact(double v) {
        String[] suffix = {"", "K", "M", "B", "T"};
        int i = 0;
        double x = v;
        while (x >= 1000 && i < suffix.length - 1) {
            x /= 1000;
            i++;
        }
        String n = BigDecimal.valueOf(x).setScale(2, RoundingMode.DOWN).stripTrailingZeros().toPlainString();
        return n + suffix[i];
    }
}
