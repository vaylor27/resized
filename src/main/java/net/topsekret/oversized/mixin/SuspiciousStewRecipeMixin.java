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

import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SuspiciousStewRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SuspiciousStewRecipe.class)
public abstract class SuspiciousStewRecipeMixin extends SpecialCraftingRecipe {

    private SuspiciousStewRecipeMixin() {
        super(null);
    }

    /**
     * @author Jakub Kaszycki
     * @reason Allow crafting a suspicious stew in a nonstandard grid. The only actual requirement is that it has at
     * least four slots.
     */
    @Override
    @Overwrite
    public boolean fits(int width, int height) {
        return width * height >= 4;
    }
}
