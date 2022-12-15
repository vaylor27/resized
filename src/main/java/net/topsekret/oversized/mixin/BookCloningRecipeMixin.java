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

import net.minecraft.recipe.BookCloningRecipe;
import net.minecraft.recipe.SpecialCraftingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BookCloningRecipe.class)
public abstract class BookCloningRecipeMixin extends SpecialCraftingRecipe {

    private BookCloningRecipeMixin() {
        super(null);
    }

    /**
     * @author Jakub Kaszycki
     * @reason Allow cloning a book in a nonstandard grid. The only actual requirement is that it has at least two slots.
     */
    @Override
    @Overwrite
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

}
