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
package net.topsekret.oversized.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.topsekret.oversized.screen.OversizedCraftingScreenHandler;

public class OversizedCraftingScreen extends HandledScreen<OversizedCraftingScreenHandler> implements RecipeBookProvider {
    private static final Identifier TEXTURE = new Identifier("textures/gui/container/crafting_table.png");
    private static final Identifier RECIPE_BUTTON_TEXTURE = new Identifier("textures/gui/recipe_button.png");
    private final RecipeBookWidget recipeBook = new RecipeBookWidget();
    private boolean narrow;

    public OversizedCraftingScreen(OversizedCraftingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    protected void init() {
        this.backgroundWidth = this.handler.getScreenWidth();
        this.backgroundHeight = this.handler.getScreenHeight();
        super.init();
        this.narrow = this.width < (325 + 18 * Math.max(3, this.handler.getCraftingWidth()));
        this.recipeBook.initialize(this.width, this.height, this.client, this.narrow, this.handler);
        this.x = findLeftEdge(this.recipeBook);
        this.addDrawableChild(new TexturedButtonWidget(this.x + 5, this.height / 2 - 76 + this.handler.getCraftingHeight() * 9, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (button) -> {
            this.recipeBook.toggleOpen();
            this.x = findLeftEdge(this.recipeBook);
            ((TexturedButtonWidget) button).setPos(this.x + 5, this.height / 2 - 76 + this.handler.getCraftingHeight() * 9);
        }));
        this.addSelectableChild(this.recipeBook);
        this.setInitialFocus(this.recipeBook);
        this.titleX = 29;

        this.playerInventoryTitleX = 8 + this.handler.getInventoryHorizontalPadding();
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    private int findLeftEdge(RecipeBookWidget recipeBook) {
        if (recipeBook.isOpen() && !this.narrow)
            return 1 + (width - 24) / 2;
        else
            return (width - backgroundWidth) / 2;
    }

    public void handledScreenTick() {
        super.handledScreenTick();
        this.recipeBook.update();
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        if (this.recipeBook.isOpen() && this.narrow) {
            this.drawBackground(matrices, delta, mouseX, mouseY);
            this.recipeBook.render(matrices, mouseX, mouseY, delta);
        } else {
            this.recipeBook.render(matrices, mouseX, mouseY, delta);
            super.render(matrices, mouseX, mouseY, delta);
            this.recipeBook.drawGhostSlots(matrices, this.x, this.y, true, delta);
        }

        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
        this.recipeBook.drawTooltip(matrices, this.x, this.y, mouseX, mouseY);
    }

    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        // Build frame
        int x0 = this.x;
        int y0 = (this.height - this.backgroundHeight) / 2;
        int x1 = x0 + this.backgroundWidth;
        int y1 = y0 + this.backgroundHeight;

        // Background
        fill(matrices, x0 + 4, y0 + 4, x1 - 4, y1 - 4, 0xffc6c6c6);

        // Top left corner
        this.drawTexture(matrices, x0, y0, 0, 0, 4, 4);
        // Top right corner
        this.drawTexture(matrices, x1 - 4, y0, 172, 0, 4, 4);
        // Bottom left corner
        this.drawTexture(matrices, x0, y1 - 4, 0, 162, 4, 4);
        // Bottom right corner
        this.drawTexture(matrices, x1 - 4, y1 - 4, 172, 162, 4, 4);

        for (int y = y0 + 4; y < y1 - 4; y += 158) {
            // Left side
            this.drawTexture(matrices, x0, y, 0, 4, 4, Math.min(158, y1 - 4 - y));
            // Right side
            this.drawTexture(matrices, x1 - 4, y, 172, 4, 4, Math.min(158, y1 - 4 - y));
        }

        for (int x = x0 + 4; x < x1 - 4; x += 168) {
            // Top side
            this.drawTexture(matrices, x, y0, 4, 0, Math.min(168, x1 - 4 - x), 4);
            // Bottom side
            this.drawTexture(matrices, x, y1 - 4, 4, 162, Math.min(168, x1 - 4 - x), 4);
        }

        // Slots
        for (int x = 0; x < this.handler.getCraftingWidth(); ++x) {
            for (int y = 0; y < this.handler.getCraftingHeight(); ++y) {
                this.drawTexture(matrices, x0 + 29 + x * 18, y0 + 16 + y * 18, 29, 16, 18, 18);
            }
        }

        // Arrow and output
        this.drawTexture(matrices, x0 + 29 + this.handler.getCraftingWidth() * 18, y0 + 16 + this.handler.getCraftingHeight() * 9 - 13, 83, 30, 62, 26);

        // Inventory
        this.drawTexture(matrices, x0 + 7 + this.handler.getInventoryHorizontalPadding(), y0 + this.handler.getInventoryY(), 7, 83, 162, 76);
    }

    protected boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        return (!this.narrow || !this.recipeBook.isOpen()) && super.isPointWithinBounds(x, y, width, height, pointX, pointY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.recipeBook.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.recipeBook);
            return true;
        } else {
            return this.narrow && this.recipeBook.isOpen() ? true : super.mouseClicked(mouseX, mouseY, button);
        }
    }

    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        boolean bl = mouseX < (double) left || mouseY < (double) top || mouseX >= (double) (left + this.backgroundWidth) || mouseY >= (double) (top + this.backgroundHeight);
        return this.recipeBook.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, this.backgroundWidth, this.backgroundHeight, button) && bl;
    }

    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        super.onMouseClick(slot, slotId, button, actionType);
        this.recipeBook.slotClicked(slot);
    }

    public void refreshRecipeBook() {
        this.recipeBook.refresh();
    }

    public void removed() {
        this.recipeBook.close();
        super.removed();
    }

    public RecipeBookWidget getRecipeBookWidget() {
        return this.recipeBook;
    }
}
