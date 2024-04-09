/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package gg.skytils.earlytweaker;

import java.util.ServiceLoader;

public class EarlyTweakerFinder {
    public static void findTweakersFromRegistrants() {
        ServiceLoader<IEarlyTweakerRegistrant> registrants = ServiceLoader.load(IEarlyTweakerRegistrant.class);
        for (IEarlyTweakerRegistrant registrant : registrants) {
            Constants.log.info(String.format("Considering early tweaker registrant: %s", registrant.getClass().getName()));
            registrant.getEarlyTweakers().forEach(t -> {
                EarlyTweakerRegistry.registerTweaker(t);
                Constants.log.info(String.format("Loaded early tweaker from registrant %s: %s", registrant.getClass().getName(), t.getName()));
            });
        }
    }
}
