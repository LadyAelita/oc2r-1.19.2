package li.cil.oc2.common.config.common;

import li.cil.oc2.common.config.Config;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.TierSortingRegistry;

public class GameplaySpec {
    public final ForgeConfigSpec.EnumValue<Tiers> blockOperationsModuleToolTier;
    public final ForgeConfigSpec.LongValue soundCardCoolDownSeconds;
    public final ForgeConfigSpec.BooleanValue robotMobilityRequiresModule;

    GameplaySpec(ForgeConfigSpec.Builder builder) {
        blockOperationsModuleToolTier = builder.comment(
            "The mining tool equivalent of the block operations module"
        ).defineEnum("blockOperationsModuleToolTier", Tiers.DIAMOND);

        soundCardCoolDownSeconds = builder.comment(
            "The number of seconds between sound card uses, to prevent spam/abuse"
        ).defineInRange("soundCardCoolDownSeconds", 2, 1, Long.MAX_VALUE);

        robotMobilityRequiresModule = builder.comment(
            "Whether robots require the appropriate module item to be able to move"
        ).define("robotMobilityRequiresModule", false);
    }

    public void loadValues() {
        Config.blockOperationsModuleToolTier = TierSortingRegistry.getName(blockOperationsModuleToolTier.get());
        Config.soundCardCoolDownSeconds = soundCardCoolDownSeconds.get();
        Config.robotMobilityRequiresModule = robotMobilityRequiresModule.get();
    }
}
