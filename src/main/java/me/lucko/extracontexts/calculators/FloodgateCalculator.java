package me.lucko.extracontexts.calculators;

import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

public class FloodgateCalculator implements ContextCalculator<Player> {
    private static final String KEY = "floodgate";

    @Override
    public void calculate(@NotNull Player target, @NotNull ContextConsumer consumer) {
        consumer.accept(KEY, String.valueOf(FloodgateApi.getInstance().isFloodgateId(target.getUniqueId())));
    }

    @Override
    public @NotNull ContextSet estimatePotentialContexts() {
        return ImmutableContextSet.builder()
                .add(KEY, "true")
                .add(KEY, "false")
                .build();
    }

}
