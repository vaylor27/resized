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
package net.topsekret.oversized.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.topsekret.oversized.Oversized;
import net.topsekret.oversized.client.gui.screen.OversizedCraftingScreen;

public class OversizedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(Oversized.OVERSIZED_CRAFTING_SCREEN_HANDLER_TYPE, OversizedCraftingScreen::new);
    }
}
