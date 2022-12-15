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
package net.topsekret.oversized.mixin;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Items;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.TippedArrowRecipe;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(TippedArrowRecipe.class)
public abstract class TippedArrowRecipeMixin extends SpecialCraftingRecipe {
    private TippedArrowRecipeMixin() {
        super(null);
    }

    /**
     * @author Jakub Kaszycki
     * @reason Allow crafting a tipped arrow in a nonstandard grid. The only actual requirement is that the grid fits
     * a 3x3 subgrid, but the vanilla algorithm refuses to cooperate on grids other than actual 3x3.
     */
    @Override
    @Overwrite
    public boolean matches(CraftingInventory craftingInventory, World world) {
        if (craftingInventory.getWidth() < 3 || craftingInventory.getHeight() < 3)
            return false;

        IntIntPair lingeringPotion = null;
        for (int i = 1; i < craftingInventory.getWidth() - 1; ++i) {
            for (int j = 1; j < craftingInventory.getHeight() - 1; ++j) {
                if (craftingInventory.getStack(i + j * craftingInventory.getWidth()).isOf(Items.LINGERING_POTION)) {
                    if (lingeringPotion != null)
                        return false;
                    lingeringPotion = IntIntPair.of(i, j);
                }
            }
        }
        if (lingeringPotion == null)
            return false;

        for (int i = -1; i <= 1; ++i)
            for (int j = -1; j <= 1; ++j)
                if ((i != 0 || j != 0) && !craftingInventory.getStack(lingeringPotion.firstInt() + i + (lingeringPotion.secondInt() + j) * craftingInventory.getWidth()).isOf(Items.ARROW))
                    return false;
        return true;
    }

    /**
     * @author Jakub Kaszycki
     * @reason Allow crafting a tipped arrow in a nonstandard grid. This actually strengthens the requirement, so that
     * no unnecessary calls to {@link #matches(CraftingInventory, World)} are made.
     */
    @Override
    @Overwrite
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }
}
