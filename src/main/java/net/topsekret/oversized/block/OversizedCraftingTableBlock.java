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
package net.topsekret.oversized.block;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.topsekret.oversized.screen.OversizedCraftingScreenHandler;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

public class OversizedCraftingTableBlock extends Block {
    private static final Text TITLE = Text.translatable("container.crafting");
    private final TriFunction<BlockState, World, BlockPos, Pair<Integer, Integer>> dimensionsProvider;

    public OversizedCraftingTableBlock(Settings settings, TriFunction<BlockState, World, BlockPos, Pair<Integer, Integer>> dimensionsProvider) {
        super(settings);
        this.dimensionsProvider = dimensionsProvider;
    }

    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            player.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
            return ActionResult.CONSUME;
        }
    }

    @SuppressWarnings("deprecation")
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        var coords = dimensionsProvider.apply(state, world, pos);
        return new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                buf.writeInt(Registry.BLOCK.getRawId(OversizedCraftingTableBlock.this));
                buf.writeInt(coords.getLeft());
                buf.writeInt(coords.getRight());
            }

            @Override
            public Text getDisplayName() {
                return TITLE;
            }

            @Nullable
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
                return new OversizedCraftingScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos), OversizedCraftingTableBlock.this, coords.getLeft(), coords.getRight());
            }
        };
    }

    public static TriFunction<BlockState, World, BlockPos, Pair<Integer, Integer>> constantDimensions(int width, int height) {
        var pair = new Pair<>(width, height);
        return (state, world, pos) -> pair;
    }
}
