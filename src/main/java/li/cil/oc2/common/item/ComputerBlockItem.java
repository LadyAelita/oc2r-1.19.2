package li.cil.oc2.common.item;

import li.cil.oc2.common.block.ComputerBlock;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ComputerBlockItem extends ModBlockItem {
	public ComputerBlockItem(final Block block) {
		super(block);
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
		super.fillItemCategory(tab, items);
		if (this.allowedIn(tab)) {
			items.add(ComputerBlock.getComputerWithFlash());
			items.add(ComputerBlock.getPreconfiguredComputer());
		}
	}
}
