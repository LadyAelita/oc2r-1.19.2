package li.cil.oc2.common.config.client;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientSpec {
    public static final ForgeConfigSpec CLIENT_CONFIG_SPEC;
    private static final GUISpec guiSpec;
    private static final RenderingSpec renderingSpec;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // GUI CONFIGURATION //
        builder.push("gui");
        guiSpec = new GUISpec(builder);
        builder.pop();

        // RENDERING CONFIG //
        builder.push("rendering");
        renderingSpec = new RenderingSpec(builder);
        builder.pop();

        CLIENT_CONFIG_SPEC = builder.build();
    }

    public static void loadValues() {
        // GUI CONFIGURATION //
        guiSpec.loadValues();
        // RENDERING CONFIG //
        renderingSpec.loadValues();
    }
}
