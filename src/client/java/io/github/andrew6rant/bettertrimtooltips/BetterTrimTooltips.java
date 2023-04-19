package io.github.andrew6rant.bettertrimtooltips;

import net.fabricmc.loader.api.FabricLoader;

public class BetterTrimTooltips {
    public static final Boolean isStackedTrimsEnabled = FabricLoader.getInstance().isModLoaded("stacked_trims");

}
