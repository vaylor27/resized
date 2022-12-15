/*
 * Copyright (C) 2021 Jakub Kaszycki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.topsekret.oversized.screen;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.topsekret.oversized.Oversized;

public class OversizedCraftingScreenHandler extends AbstractRecipeScreenHandler<CraftingInventory> {
    private final Block expectedBlock;
    private final CraftingInventory input;
    private final CraftingResultInventory result;
    private final ScreenHandlerContext context;
    private final PlayerEntity player;
    private final int screenWidth;
    private final int screenHeight;
    private final int inventoryHorizontalPadding;
    private final int inventoryY;

    public static OversizedCraftingScreenHandler read(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        Block expectedBlock = Registry.BLOCK.get(buf.readInt());
        int width = buf.readInt();
        int height = buf.readInt();
        return new OversizedCraftingScreenHandler(syncId, playerInventory, expectedBlock, width, height);
    }

    public OversizedCraftingScreenHandler(int syncId, PlayerInventory playerInventory, Block expectedBlock, int width, int height) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY, expectedBlock, width, height);
    }

    public OversizedCraftingScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, Block expectedBlock, int width, int height) {
        super(Oversized.OVERSIZED_CRAFTING_SCREEN_HANDLER_TYPE, syncId);
        this.expectedBlock = expectedBlock;
        this.input = new CraftingInventory(this, width, height);
        this.result = new CraftingResultInventory();
        this.context = context;
        this.player = playerInventory.player;
        this.addSlot(new CraftingResultSlot(playerInventory.player, this.input, this.result, 0, 70 + width * 18, 8 + height * 9));

        this.screenWidth = Math.max(176, 29 + width * 18 + 62 + 12 + 4);
        this.screenHeight = 16 + Math.max(26, height * 18) + 13 + 83;
        this.inventoryHorizontalPadding = (Math.max(this.screenWidth, 176) - 176) / 2;
        this.inventoryY = this.screenHeight - 83;

        // Slots
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                this.addSlot(new Slot(this.input, x + y * width, 30 + x * 18, 17 + y * 18));
            }
        }

        // Inventory
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, this.inventoryHorizontalPadding + 8 + column * 18, this.inventoryY + 1 + row * 18));
            }
        }

        // Hotbar
        for (int slot = 0; slot < 9; ++slot) {
            this.addSlot(new Slot(playerInventory, slot, this.inventoryHorizontalPadding + 8 + slot * 18, this.inventoryY + 1 + 58));
        }

    }

    public int getInventoryHorizontalPadding() {
        return inventoryHorizontalPadding;
    }

    public int getInventoryY() {
        return inventoryY;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    protected static void updateResult(ScreenHandler handler, World world, PlayerEntity player, CraftingInventory craftingInventory, CraftingResultInventory resultInventory) {
        if (!world.isClient) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
            ItemStack result = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftingInventory, world).filter(recipe -> resultInventory.shouldCraftRecipe(world, serverPlayerEntity, recipe)).map(recipe -> recipe.craft(craftingInventory)).orElse(ItemStack.EMPTY);

            resultInventory.setStack(0, result);
            handler.setPreviousTrackedSlot(0, result);
            serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, result));
        }
    }

    public void onContentChanged(Inventory inventory) {
        this.context.run((world, pos) -> {
            updateResult(this, world, this.player, this.input, this.result);
        });
    }

    public void populateRecipeFinder(RecipeMatcher finder) {
        this.input.provideRecipeInputs(finder);
    }

    public void clearCraftingSlots() {
        this.input.clear();
        this.result.clear();
    }

    public boolean matches(Recipe<? super CraftingInventory> recipe) {
        return recipe.matches(this.input, this.player.world);
    }

    public void close(PlayerEntity player) {
        super.close(player);
        this.context.run((world, pos) -> {
            this.dropInventory(player, this.input);
        });
    }

    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, expectedBlock);
    }

    public ItemStack transferSlot(PlayerEntity player, int slotIndex) {
        int craftingSlotCount = getCraftingSlotCount();
        ItemStack result = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(slotIndex);
        if (slot != null && slot.hasStack()) {
            ItemStack original = slot.getStack();
            result = original.copy();
            if (slotIndex == 0) {
                this.context.run((world, pos) -> {
                    original.getItem().onCraft(original, world, player);
                });
                if (!this.insertItem(original, craftingSlotCount, craftingSlotCount + 36, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickTransfer(original, result);
            } else if (slotIndex >= craftingSlotCount && slotIndex < craftingSlotCount + 36) {
                if (!this.insertItem(original, 1, craftingSlotCount, false)) {
                    if (slotIndex < craftingSlotCount + 27) {
                        if (!this.insertItem(original, craftingSlotCount + 27, craftingSlotCount + 36, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.insertItem(original, craftingSlotCount, craftingSlotCount + 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.insertItem(original, craftingSlotCount, craftingSlotCount + 36, false)) {
                return ItemStack.EMPTY;
            }

            if (original.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (original.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, original);
            if (slotIndex == 0) {
                player.dropItem(original, false);
            }
        }

        return result;
    }

    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.result && super.canInsertIntoSlot(stack, slot);
    }

    public int getCraftingResultSlotIndex() {
        return 0;
    }

    public int getCraftingWidth() {
        return this.input.getWidth();
    }

    public int getCraftingHeight() {
        return this.input.getHeight();
    }

    public int getCraftingSlotCount() {
        return 1 + getCraftingWidth() * getCraftingHeight();
    }

    public RecipeBookCategory getCategory() {
        return RecipeBookCategory.CRAFTING;
    }

    public boolean canInsertIntoSlot(int index) {
        return index != this.getCraftingResultSlotIndex();
    }
}