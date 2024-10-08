package me.lucko.extracontexts.calculators;

import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HasPlayedBeforeCalculator implements ContextCalculator<Player> {
    private static final String KEY = "has-played-before";

    @Override
    public void calculate(@NotNull Player target, @NotNull ContextConsumer consumer) {
        consumer.accept(KEY, String.valueOf(target.hasPlayedBefore()));
    }

    @Override
    public @NotNull ContextSet estimatePotentialContexts() {
        return ImmutableContextSet.builder()
                .add(KEY, "true")
                .add(KEY, "false")
                .build();
    }

}
