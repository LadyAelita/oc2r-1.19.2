package li.cil.oc2.common.config.client;

import li.cil.oc2.common.config.Config;
import net.minecraftforge.common.ForgeConfigSpec;

public class RenderingSpec {
	public final ForgeConfigSpec.DoubleValue computerDetailRenderDistance;
	public final ForgeConfigSpec.DoubleValue monitorDetailRenderDistance;

	public RenderingSpec(final ForgeConfigSpec.Builder builder) {
		computerDetailRenderDistance = builder.comment(
			"Maximum distance at which computer terminal text is rendered in full detail.",
			"Beyond this distance, a static texture is shown instead."
		).defineInRange("computerDetailRenderDistance", 6.0, 0.0, 256.0);
		monitorDetailRenderDistance = builder.comment(
			"Maximum distance at which monitor terminal text is rendered in full detail.",
			"Beyond this distance, a static texture is shown instead."
		).defineInRange("monitorDetailRenderDistance", 6.0, 0.0, 256.0);
	}

	public void loadValues() {
		Config.computerDetailRenderDistance = computerDetailRenderDistance.get();
		Config.monitorDetailRenderDistance = monitorDetailRenderDistance.get();
	}
}
