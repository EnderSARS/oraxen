package io.th0rgal.oraxen.nms;

import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.IFurniturePacketManager;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface NMSHandler {
    default IFurniturePacketManager furniturePacketManager() {
        return new EmptyFurniturePacketManager();
    }

    boolean noteblockUpdatesDisabled();

    boolean tripwireUpdatesDisabled();

    /**
     * Copies over all NBT-Tags from oldItem to newItem
     * Useful for plugins that might register their own NBT-Tags outside
     * the ItemStacks PersistentDataContainer
     *
     * @param oldItem The old ItemStack to copy the NBT-Tags from
     * @param newItem The new ItemStack to copy the NBT-Tags to
     * @return The new ItemStack with the copied NBT-Tags
     */
    ItemStack copyItemNBTTags(@NotNull ItemStack oldItem, @NotNull ItemStack newItem);

    /**
     * Corrects the BlockData of a placed block.
     * Mainly fired when placing a block against an OraxenNoteBlock due to vanilla behaviour requiring Sneaking
     *
     * @param player          The player that placed the block
     * @param slot            The hand the player placed the block with
     * @param itemStack       The ItemStack the player placed the block with
     * @return The enum interaction result
     */
    @Nullable Enum<InteractionResult> correctBlockStates(Player player, EquipmentSlot slot, ItemStack itemStack);
    BlockHitResult getPlayerPOVHitResult(Level world, net.minecraft.world.entity.player.Player player, ClipContext.Fluid fluidHandling);

    /**Removes mineable/axe tag from noteblocks for custom blocks */
    void customBlockDefaultTools(Player player);

    /**
     * Keys that are used by vanilla Minecraft and should therefore be skipped
     * Some are accessed through API methods, others are just used internally
     */
    Set<String> vanillaKeys = Set.of("PublicBukkitValues", "display", "CustomModelData", "Damage", "AttributeModifiers",
            "Unbreakable", "CanDestroy", "slot", "count", "HideFlags", "CanPlaceOn", "Enchantments", "StoredEnchantments",
            "RepairCost", "CustomPotionEffects", "Potion", "CustomPotionColor", "Trim", "EntityTag",
            "pages", "filtered_pages", "filtered_title", "resolved", "generation", "author", "title",
            "BucketVariantTag", "Items", "LodestoneTracked", "LodestoneDimension", "LodestonePos",
            "ChargedProjectiles", "Charged", "DebugProperty", "Fireworks", "Explosion", "Flight",
            "map", "map_scale_direction", "map_to_lock", "Decorations", "SkullOwner", "Effects", "BlockEntityTag", "BlockStateTag");

    void setupNmsGlyphs();

    void inject(Player player);
    void uninject(Player player);

    default boolean getSupported() {
        return false;
    }
}
