package li.cil.oc2.common.bus.device.provider.item;

import java.util.Optional;

import li.cil.oc2.api.bus.device.ItemDevice;
import li.cil.oc2.api.bus.device.provider.ItemDeviceQuery;
import li.cil.oc2.common.bus.device.provider.util.AbstractItemDeviceProvider;
import li.cil.oc2.common.bus.device.unlocks.item.RobotMobilityModuleDevice;
import li.cil.oc2.common.capabilities.Capabilities;
import li.cil.oc2.common.config.Config;
import li.cil.oc2.common.item.Items;

public class RobotMobilityModuleDeviceProvider extends AbstractItemDeviceProvider {
	public RobotMobilityModuleDeviceProvider() {
		super(Items.ROBOT_MOBILITY_MODULE);
	}

	@Override
	protected Optional<ItemDevice> getItemDevice(ItemDeviceQuery query) {
		return query.getContainerEntity().flatMap(entity ->
			entity.getCapability(Capabilities.robot()).map(robot ->
				new RobotMobilityModuleDevice()
			)
		);
	}

	@Override
	protected int getItemDeviceEnergyConsumption(ItemDeviceQuery query) {
		return Config.robotMobilityEnergyPerTick;
	}
}
