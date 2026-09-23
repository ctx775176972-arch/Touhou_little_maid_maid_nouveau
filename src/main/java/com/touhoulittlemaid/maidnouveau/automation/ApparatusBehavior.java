package com.touhoulittlemaid.maidnouveau.automation;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.hollingsworth.arsnouveau.api.ANFakePlayer;
import com.hollingsworth.arsnouveau.api.ArsNouveauAPI;
import com.hollingsworth.arsnouveau.api.util.SourceUtil;
import com.hollingsworth.arsnouveau.common.block.ArcaneCore;
import com.hollingsworth.arsnouveau.common.block.tile.ArcanePedestalTile;
import com.hollingsworth.arsnouveau.common.block.tile.EnchantingApparatusTile;
import com.hollingsworth.arsnouveau.common.crafting.recipes.EnchantingApparatusRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.IEnchantingRecipe;
import com.touhoulittlemaid.maidnouveau.MaidNouveau;
import com.touhoulittlemaid.maidnouveau.config.MaidNouveauConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class ApparatusBehavior extends AbstractWorkstationBehavior<EnchantingApparatusTile> {
    private String lastState = "";

    public ApparatusBehavior() {
        super(WorkstationType.APPARATUS, MaidNouveauConfig.APPARATUS_WORK_INTERVAL);
    }

    @Override
    protected WorkResult performWork(ServerLevel level, EntityMaid maid, EnchantingApparatusTile apparatus) {
        if (apparatus.isCrafting) {
            reportState(maid, apparatus, "waiting_for_craft");
            return WorkResult.NONE;
        }
        if (!validStructure(level, apparatus)) {
            reportState(maid, apparatus, "invalid_arcane_core_structure");
            return WorkResult.NONE;
        }

        IItemHandler backpack = maid.getAvailableBackpackInv();
        if (!apparatus.getStack().isEmpty()) {
            ItemStack output = apparatus.getStack().copy();
            if (ItemHandlerHelper.insertItemStacked(backpack, output, true).isEmpty()) {
                apparatus.setStack(ItemStack.EMPTY);
                ItemHandlerHelper.insertItemStacked(backpack, output, false);
                reportState(maid, apparatus, "collected_output");
                return WorkResult.COMPLETE;
            }
            reportState(maid, apparatus, "backpack_full");
            return WorkResult.NONE;
        }

        ItemStack targetOutput = maid.getMainHandItem();
        if (targetOutput.isEmpty()) {
            reportState(maid, apparatus, "main_hand_empty");
            return WorkResult.NONE;
        }
        Player fakePlayer = ANFakePlayer.getPlayer(level, maid.getUUID());
        fakePlayer.setPos(maid.getX(), maid.getY(), maid.getZ());

        List<ArcanePedestalTile> pedestals = pedestals(apparatus);
        List<ItemStack> placedItems = pedestals.stream()
                .map(ArcanePedestalTile::getStack)
                .filter(stack -> !stack.isEmpty())
                .toList();
        for (RecipeHolder<? extends IEnchantingRecipe> holder : ArsNouveauAPI.getInstance().getEnchantingApparatusRecipes(level)) {
            if (!(holder.value() instanceof EnchantingApparatusRecipe recipe)
                    || !ItemStack.isSameItem(recipe.result(), targetOutput)
                    || recipe.pedestalItems().size() > pedestals.size()) {
                continue;
            }

            boolean[] matchedIngredients = matchPlacedItems(placedItems, recipe.pedestalItems());
            if (matchedIngredients == null) {
                continue;
            }
            List<Ingredient> missingIngredients = new ArrayList<>();
            for (int i = 0; i < recipe.pedestalItems().size(); i++) {
                if (!matchedIngredients[i]) {
                    missingIngredients.add(recipe.pedestalItems().get(i));
                }
            }

            // When the pedestal setup is already complete, place the reagent last.
            if (missingIngredients.isEmpty()) {
                List<Integer> reagentSlot = matchIngredients(backpack, List.of(recipe.reagent()));
                if (reagentSlot == null) {
                    reportState(maid, apparatus, "missing_reagent");
                    return WorkResult.NONE;
                }
                if (recipe.consumesSource() && !SourceUtil.hasSourceNearby(apparatus.getBlockPos(), level, 10, recipe.sourceCost())) {
                    reportState(maid, apparatus, "waiting_for_source");
                    return WorkResult.NONE;
                }
                if (startCraft(backpack, reagentSlot.getFirst(), apparatus, fakePlayer)) {
                    reportState(maid, apparatus, "craft_started");
                    return WorkResult.PROGRESS;
                }
                reportState(maid, apparatus, "apparatus_rejected_reagent");
                return WorkResult.NONE;
            }

            // Reserve one matching reagent while assigning the pedestal materials so
            // the maid cannot accidentally spend the last reagent on a pedestal.
            List<Ingredient> requiredItems = new ArrayList<>();
            requiredItems.add(recipe.reagent());
            requiredItems.addAll(missingIngredients);
            List<Integer> slots = matchIngredients(backpack, requiredItems);
            if (slots == null) {
                continue;
            }
            List<ArcanePedestalTile> emptyPedestals = pedestals.stream()
                    .filter(pedestal -> pedestal.getStack().isEmpty())
                    .toList();
            if (missingIngredients.size() > emptyPedestals.size()) {
                continue;
            }
            for (int i = 0; i < missingIngredients.size(); i++) {
                emptyPedestals.get(i).setStack(backpack.extractItem(slots.get(i + 1), 1, false));
            }
            // Pedestal writes are immediately visible to the apparatus on the server.
            // Start in this same AI activation so idle movement cannot interrupt the
            // sequence between preparing pedestals and inserting the reagent.
            if (recipe.consumesSource() && !SourceUtil.hasSourceNearby(apparatus.getBlockPos(), level, 10, recipe.sourceCost())) {
                reportState(maid, apparatus, "pedestals_prepared_waiting_for_source");
                return WorkResult.PROGRESS;
            }
            if (startCraft(backpack, slots.getFirst(), apparatus, fakePlayer)) {
                reportState(maid, apparatus, "pedestals_prepared_and_craft_started");
                return WorkResult.PROGRESS;
            }
            reportState(maid, apparatus, "pedestals_prepared_but_apparatus_rejected_reagent");
            return WorkResult.NONE;
        }
        reportState(maid, apparatus, placedItems.isEmpty() ? "no_recipe_or_missing_materials" : "pedestal_items_do_not_match");
        return WorkResult.NONE;
    }

    private static boolean startCraft(IItemHandler backpack, int reagentSlot, EnchantingApparatusTile apparatus, Player player) {
        ItemStack one = backpack.getStackInSlot(reagentSlot).copyWithCount(1);
        if (!apparatus.attemptCraft(one, player)) {
            return false;
        }
        apparatus.setStack(backpack.extractItem(reagentSlot, 1, false));
        return true;
    }

    private static boolean validStructure(ServerLevel level, EnchantingApparatusTile tile) {
        BlockState state = tile.getBlockState();
        if (!state.hasProperty(BlockStateProperties.FACING)) {
            return false;
        }
        Direction facing = state.getValue(BlockStateProperties.FACING);
        BlockState core = level.getBlockState(tile.getBlockPos().relative(facing.getOpposite()));
        return core.getBlock() instanceof ArcaneCore && core.hasProperty(BlockStateProperties.FACING)
                && core.getValue(BlockStateProperties.FACING).getAxis().test(facing);
    }

    private static List<ArcanePedestalTile> pedestals(EnchantingApparatusTile apparatus) {
        List<ArcanePedestalTile> result = new ArrayList<>();
        for (BlockPos pos : apparatus.pedestalList()) {
            BlockEntity entity = apparatus.getLevel().getBlockEntity(pos);
            if (entity instanceof ArcanePedestalTile pedestal) {
                result.add(pedestal);
            }
        }
        return result;
    }

    private static List<Integer> matchIngredients(IItemHandler inventory, List<Ingredient> ingredients) {
        int[] remaining = new int[inventory.getSlots()];
        for (int i = 0; i < remaining.length; i++) {
            remaining[i] = inventory.getStackInSlot(i).getCount();
        }
        List<Integer> result = new ArrayList<>();
        return match(inventory, ingredients, 0, remaining, result) ? result : null;
    }

    private static boolean match(IItemHandler inventory, List<Ingredient> ingredients, int index, int[] remaining, List<Integer> result) {
        if (index == ingredients.size()) {
            return true;
        }
        Ingredient ingredient = ingredients.get(index);
        for (int slot = 0; slot < remaining.length; slot++) {
            if (remaining[slot] > 0 && ingredient.test(inventory.getStackInSlot(slot))) {
                remaining[slot]--;
                result.add(slot);
                if (match(inventory, ingredients, index + 1, remaining, result)) {
                    return true;
                }
                result.remove(result.size() - 1);
                remaining[slot]++;
            }
        }
        return false;
    }

    private static boolean[] matchPlacedItems(List<ItemStack> placedItems, List<Ingredient> ingredients) {
        if (placedItems.size() > ingredients.size()) {
            return null;
        }
        boolean[] used = new boolean[ingredients.size()];
        return matchPlaced(placedItems, ingredients, 0, used) ? used : null;
    }

    private static boolean matchPlaced(List<ItemStack> placedItems, List<Ingredient> ingredients, int index, boolean[] used) {
        if (index == placedItems.size()) {
            return true;
        }
        ItemStack stack = placedItems.get(index);
        for (int ingredient = 0; ingredient < ingredients.size(); ingredient++) {
            if (!used[ingredient] && ingredients.get(ingredient).test(stack)) {
                used[ingredient] = true;
                if (matchPlaced(placedItems, ingredients, index + 1, used)) {
                    return true;
                }
                used[ingredient] = false;
            }
        }
        return false;
    }

    private void reportState(EntityMaid maid, EnchantingApparatusTile apparatus, String state) {
        if (state.equals(lastState)) {
            return;
        }
        lastState = state;
        MaidNouveau.LOGGER.info("Enchanting apparatus task for maid {} at {}: {}", maid.getUUID(), apparatus.getBlockPos(), state);
    }

}
