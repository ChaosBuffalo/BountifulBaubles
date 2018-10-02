package cursedflames.bountifulbaubles.baubleeffect;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import baubles.api.BaublesApi;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import cursedflames.bountifulbaubles.BountifulBaubles;
import cursedflames.bountifulbaubles.item.IItemAttributeModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BaubleAttributeModifierHandler {
	public static final List<UUID> UUIDs = Arrays.asList(
			UUID.fromString("2a111cc4-2bbe-44b3-829a-743d0dbe3cdd"),
			UUID.fromString("3fee9d69-c684-4899-ba19-471028b6dada"),
			UUID.fromString("9c65c059-741d-4be9-8b59-927fe3e17d21"),
			UUID.fromString("1cfce6d0-ea70-4c59-be25-8edae075bebb"),
			UUID.fromString("052eece9-5e65-4955-a279-33f22a16ecdb"),
			UUID.fromString("829d9a80-a0fc-4b17-90b3-71ec7cc91d47"),
			UUID.fromString("86c0b8cf-648b-4d98-8207-9d44bb660c9d"));

	public static void baubleModified(ItemStack stack, EntityLivingBase entity, boolean equip) {
		if (stack.isEmpty()||!(entity instanceof EntityPlayer))
			return;
		BountifulBaubles.logger.info("bauble modified");
		IItemAttributeModifier item = null;
		EntityPlayer player = (EntityPlayer) entity;
		Map<IAttribute, AttributeModifier> itemMods = null;
		IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
		if (!stack.hasTagCompound()||!stack.getTagCompound().hasKey("baubleModifier"))
			EnumBaubleModifier.generateModifier(stack);
		EnumBaubleModifier mod = EnumBaubleModifier
				.get(stack.getTagCompound().getString("baubleModifier"));
		BountifulBaubles.logger.info(mod);
		boolean modifier = stack.getItem() instanceof IItemAttributeModifier;
		if (modifier) {
			item = (IItemAttributeModifier) stack.getItem();
			itemMods = item.getModifiers(stack, player);
		}
		if (equip) {
			if (modifier) {
				for (IAttribute a : itemMods.keySet()) {
					if (!player.getEntityAttribute(a).hasModifier(itemMods.get(a)))
						player.getEntityAttribute(a).applyModifier(itemMods.get(a));
				}
			}
			if (mod!=null&&mod!=EnumBaubleModifier.NONE) {
				// player.getEntityAttribute(mod.attribute).applyModifier(new
				// AttributeModifier());
				int i = 0;
				for (; i<=baubles.getSlots(); i++) {
					if (i==baubles.getSlots())
						break;
					ItemStack stack1 = baubles.getStackInSlot(i);
					if (stack1==stack)
						break;
				}
				addModifier(player, mod, i);
			}
		} else {
			if (modifier) {
				for (IAttribute a : itemMods.keySet()) {
					UUID id = itemMods.get(a).getID();
					boolean hasAttribute = false;
					for (int i = 0; i<baubles.getSlots(); i++) {
						ItemStack stack1 = baubles.getStackInSlot(i);
						if (stack1.getItem() instanceof IItemAttributeModifier) {
							IItemAttributeModifier item1 = (IItemAttributeModifier) stack1
									.getItem();
							Map<IAttribute, AttributeModifier> item1Mods = item1
									.getModifiers(stack1, player);
							if (item1Mods.containsKey(a)&&item1Mods.get(a).getID().equals(id)) {
								hasAttribute = true;
							}
						}
					}
					if (!hasAttribute) {
						player.getEntityAttribute(a).removeModifier(id);
					}
				}
			}
			if (mod!=null&&mod!=EnumBaubleModifier.NONE) {
				for (int i = 0; i<baubles.getSlots(); i++) {
					if (baubles.getStackInSlot(i).isEmpty()) {
						removeModifier(player, mod, i);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerCraft(PlayerEvent.ItemCraftedEvent event) {
		ItemStack stack = event.crafting;
		if (stack!=null&&!stack.isEmpty()
				&&stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null)) {
			EnumBaubleModifier.generateModifier(stack);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onItemTooltip(ItemTooltipEvent event) {
		// TODO use capabilities for modifiers instead
		if (event.getEntity()==null||event.getEntity().world==null
				||!event.getEntity().world.isRemote)
			return;
		ItemStack stack = event.getItemStack();
		if (!stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null))
			return;
		if (stack.hasTagCompound()&&stack.getTagCompound().hasKey("baubleModifier")) {
			String mod = stack.getTagCompound().getString("baubleModifier");
			if (!mod.equals("none")) {
				event.getToolTip().add(BountifulBaubles.proxy
						.translate(BountifulBaubles.MODID+".modifier."+mod+".info"));
				String modName = BountifulBaubles.proxy
						.translate(BountifulBaubles.MODID+".modifier."+mod+".name");
				String name = event.getToolTip().get(0);
				event.getToolTip().set(0, modName+" "+name);
			}
		}
	}

	public static void removeModifier(EntityPlayer player, EnumBaubleModifier mod, int slot) {
		player.getEntityAttribute(mod.attribute).removeModifier(UUIDs.get(slot));
	}

	public static void addModifier(EntityPlayer player, EnumBaubleModifier mod, int slot) {
		if (player.getEntityAttribute(mod.attribute).getModifier(UUIDs.get(slot))==null) {
			player.getEntityAttribute(mod.attribute)
					.applyModifier(new AttributeModifier(UUIDs.get(slot),
							"Bauble slot "+String.valueOf(slot)+" modifier", mod.amount,
							mod.operation));
		}
	}

//	private class ContainerListener implements IContainerListener {
//		private EntityPlayer player;
//		private EnumBaubleModifier[] mods = new EnumBaubleModifier[7];
//
//		@Override
//		public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
//		}
//
//		@Override
//		public void sendSlotContents(Container container, int ind, ItemStack stack) {
//			// TODO what are the indexes? bauble inv is extended vanilla inv
//			BountifulBaubles.logger.info(""+ind+" "+stack.getItem().getUnlocalizedName());
//			Slot slot = container.getSlot(ind);
////			if (ind<7&&player!=null&&!player.isDead) {
////				EnumBaubleModifier old = mods[ind];
////				EnumBaubleModifier cur = EnumBaubleModifier
////						.get(stack.getTagCompound().getString("baubleModifier"));
////				mods[ind] = cur;
////				if (old!=cur) {
////					if (old!=null&&old!=EnumBaubleModifier.NONE) {
////						removeModifier(player, old, ind);
////					}
////					if (cur!=null&&cur!=EnumBaubleModifier.NONE) {
////						addModifier(player, cur, ind);
////					}
////				}
////			}
//		}
//
//		@Override
//		public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
//		}
//
//		@Override
//		public void sendAllWindowProperties(Container containerIn, IInventory inventory) {
//		}
//	}
//
//	@SubscribeEvent
//	public static void onContainerOpen(PlayerContainerEvent.Open event) {
////		if (event.getContainer() instanceof ContainerPlayerExpanded) {
////
////		}
//	}
}
