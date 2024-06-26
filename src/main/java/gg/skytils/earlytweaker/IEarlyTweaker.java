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

import net.minecraft.launchwrapper.ITweaker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface IEarlyTweaker extends ITweaker {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface EarlyOrder {
        int value();
    }

    @Override
    default void acceptOptions(java.util.List<String> args, java.io.File gameDir, java.io.File assetsDir, String profile) {

    }

    @Override
    default void injectIntoClassLoader(net.minecraft.launchwrapper.LaunchClassLoader classLoader) {

    }

    @Override
    default String[] getLaunchArguments() {
        return new String[0];
    }

    @Override
    default String getLaunchTarget() {
        throw new UnsupportedOperationException("EarlyTweaker should not be the primary tweaker");
    }
}
