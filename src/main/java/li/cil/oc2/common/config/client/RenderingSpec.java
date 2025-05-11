package li.cil.oc2.common.config.client;

import li.cil.oc2.common.config.Config;
import net.minecraftforge.common.ForgeConfigSpec;

public class RenderingSpec {
	public final ForgeConfigSpec.DoubleValue screenDetailRenderDistance;

	public RenderingSpec(final ForgeConfigSpec.Builder builder) {
		screenDetailRenderDistance = builder.comment(
			"Maximum distance at which computer terminal text is rendered in full detail.",
			"Beyond this distance, a static texture is shown instead."
		).defineInRange("screenDetailRenderDistance", 6.0, 0.0, 256.0);
	}

	public void loadValues() {
		Config.screenDetailRenderDistance = screenDetailRenderDistance.get();
	}
}
