package dev.nukepay;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.OptionalDouble;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class NukePayClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Draw the effects on top of the chat layer.
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("nukepay", "overlay"),
                (graphics, deltaTracker) -> AlertManager.render(graphics));

        // Server messages (system/game chat), e.g. "Steve paid you $5M"
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            String text = message.getString();
            Payments.parse(text).ifPresent(p -> Minecraft.getInstance().execute(() -> AlertManager.trigger(p)));
        });

        // /nukepay test <amount>   e.g. /nukepay test 5b
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("nukepay")
                        .then(ClientCommandManager.literal("test")
                                .then(ClientCommandManager.argument("amount", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String raw = StringArgumentType.getString(ctx, "amount");
                                            OptionalDouble amount = Payments.parseAmount(raw);
                                            if (amount.isEmpty()) {
                                                ctx.getSource().sendError(Component.literal("Couldn't read that amount. Try 5m, 2.5b, 100b."));
                                                return 0;
                                            }
                                            if (Tier.of(amount.getAsDouble()) == Tier.NONE) {
                                                ctx.getSource().sendFeedback(Component.literal("Below the lowest tier (1M), so nothing would play."));
                                                return 0;
                                            }
                                            AlertManager.trigger(new Payments.Payment("TestPlayer", amount.getAsDouble()));
                                            return 1;
                                        })))));
    }
}
