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
package net.topsekret.oversized;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.topsekret.oversized.screen.OversizedCraftingScreenHandler;

public class Oversized implements ModInitializer {
    public static final String NAMESPACE = "oversized";

    public static Identifier identifier(String path) {
        return new Identifier(NAMESPACE, path);
    }

    public static ScreenHandlerType<OversizedCraftingScreenHandler> OVERSIZED_CRAFTING_SCREEN_HANDLER_TYPE;

    @Override
    public void onInitialize() {
        OVERSIZED_CRAFTING_SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerExtended(identifier("crafting"), OversizedCraftingScreenHandler::read);
    }
}
