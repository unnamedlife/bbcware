package bbcdevelopment.addon.bbcaddon.utils.world;

import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.ArrayList;

public class ItemHelper {
    public static ArrayList<Item> buttons = new ArrayList<Item>() {{add(Items.STONE_BUTTON);add(Items.POLISHED_BLACKSTONE_BUTTON);add(Items.OAK_BUTTON);add(Items.SPRUCE_BUTTON);add(Items.BIRCH_BUTTON);add(Items.JUNGLE_BUTTON);add(Items.ACACIA_BUTTON);add(Items.DARK_OAK_BUTTON);add(Items.CRIMSON_BUTTON);add(Items.WARPED_BUTTON);}};
    public static ArrayList<Item> wools = new ArrayList<>() {{add(Items.WHITE_WOOL);add(Items.ORANGE_WOOL);add(Items.MAGENTA_WOOL);add(Items.LIGHT_BLUE_WOOL);add(Items.YELLOW_WOOL);add(Items.LIME_WOOL);add(Items.PINK_WOOL);add(Items.GRAY_WOOL);add(Items.LIGHT_GRAY_WOOL);add(Items.CYAN_WOOL);add(Items.PURPLE_WOOL);add(Items.BLUE_WOOL);add(Items.BROWN_WOOL);add(Items.GREEN_WOOL);add(Items.RED_WOOL);add(Items.BLACK_WOOL);}};
    public static ArrayList<Item> planks = new ArrayList<>() {{add(Items.OAK_PLANKS); add(Items.SPRUCE_PLANKS); add(Items.BIRCH_PLANKS); add(Items.JUNGLE_PLANKS); add(Items.ACACIA_PLANKS); add(Items.DARK_OAK_PLANKS);}};
    public static ArrayList<Item> shulkers = new ArrayList<>() {{ add(Items.SHULKER_BOX); add(Items.BLACK_SHULKER_BOX);add(Items.BLUE_SHULKER_BOX); add(Items.BROWN_SHULKER_BOX); add(Items.GREEN_SHULKER_BOX); add(Items.RED_SHULKER_BOX);add(Items.WHITE_SHULKER_BOX); add(Items.LIGHT_BLUE_SHULKER_BOX); add(Items.LIGHT_GRAY_SHULKER_BOX); add(Items.LIME_SHULKER_BOX);add(Items.MAGENTA_SHULKER_BOX); add(Items.ORANGE_SHULKER_BOX); add(Items.PINK_SHULKER_BOX); add(Items.CYAN_SHULKER_BOX);add(Items.GRAY_SHULKER_BOX); add(Items.PURPLE_SHULKER_BOX); add(Items.YELLOW_SHULKER_BOX);}};
    public static ArrayList<Item> saplings = new ArrayList<>() {{add(Items.ACACIA_SAPLING); add(Items.BIRCH_SAPLING); add(Items.SPRUCE_SAPLING); add(Items.JUNGLE_SAPLING); add(Items.DARK_OAK_SAPLING); add(Items.OAK_SAPLING);}};
    public static ArrayList<Item> flowers = new ArrayList<>() {{add(Items.POPPY);add(Items.DANDELION);add(Items.BLUE_ORCHID);add(Items.ALLIUM);add(Items.AZURE_BLUET);add(Items.RED_TULIP);add(Items.PINK_TULIP);add(Items.ORANGE_TULIP);add(Items.WHITE_TULIP);add(Items.OXEYE_DAISY);add(Items.CORNFLOWER);add(Items.LILY_OF_THE_VALLEY);add(Items.WITHER_ROSE);add(Items.SUNFLOWER);add(Items.LILAC);add(Items.ROSE_BUSH);add(Items.PEONY);}};

    public static FindItemResult findCraftTable() { return InvUtils.findInHotbar(Blocks.CRAFTING_TABLE.asItem()); }
}
