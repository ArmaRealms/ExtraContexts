package me.lucko.extracontexts.calculators;

import net.luckperms.api.context.Context;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.IWrappedStatusFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

// @Deprecated
public class WorldGuardFlagCalculator implements ContextCalculator<Player> {

    // calling worldGuard.queryApplicableFlags can sometimes cause Vault lookups, which
    // would make a recursive call to this calculator. this breaks the 3rd rule that
    // ContextCalculators should follow.
    //
    // see for more info: https://github.com/LuckPerms/ExtraContexts/issues/27
    //
    // the safest/best solution would be to remove this calculator entirely, but this
    // ThreadLocal hack is a good enough work around for users who already
    // depend on it.
    private static final ThreadLocal<Boolean> IN_PROGRESS = ThreadLocal.withInitial(() -> false);

    private static final String KEY = "worldguard:flag-";

    private final WorldGuardWrapper worldGuard = WorldGuardWrapper.getInstance();

    @Override
    public void calculate(@NotNull Player target, @NotNull ContextConsumer consumer) {
        if (IN_PROGRESS.get()) {
            return;
        }

        IN_PROGRESS.set(true);
        try {
            Map<IWrappedFlag<?>, Object> flags = this.worldGuard.queryApplicableFlags(target, target.getLocation());
            flags.forEach((flag, value) -> {
                if (invalidValue(value)) {
                    return;
                }
                consumer.accept(KEY + flag.getName(), value.toString());
            });
        } finally {
            IN_PROGRESS.set(false);
        }
    }

    @Override
    public @NotNull ContextSet estimatePotentialContexts() {
        ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
        for (World world : Bukkit.getWorlds()) {
            for (IWrappedRegion region : this.worldGuard.getRegions(world).values()) {
                Map<IWrappedFlag<?>, Object> flags = region.getFlags();
                flags.forEach((flag, value) -> {
                    if (flag instanceof IWrappedStatusFlag) {
                        for (WrappedState state : WrappedState.values()) {
                            builder.add(KEY + flag.getName(), state.toString());
                        }
                    } else {
                        if (!invalidValue(value)) {
                            builder.add(KEY + flag.getName(), value.toString());
                        }

                        Object defaultValue = flag.getDefaultValue().orElse(null);
                        if (!invalidValue(defaultValue)) {
                            builder.add(KEY + flag.getName(), defaultValue.toString());
                        }
                    }
                });
            }
        }
        return builder.build();
    }

    private static boolean invalidValue(Object value) {
        return value == null || value instanceof Location || value instanceof Vector || (value instanceof String && !Context.isValidValue(((String) value)));
    }

}
