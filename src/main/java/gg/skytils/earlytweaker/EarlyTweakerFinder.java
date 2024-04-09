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

import gg.skytils.earlytweaker.utils.Utils;
import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public class EarlyTweakerFinder {
    public static final File earlyTweakersConfigDir = new File("./config/earlytweaker/" + Constants.MC_VERSION);
    public static final File earlyTweakers = new File(earlyTweakersConfigDir, "/tweakers");

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

    public static void findTweakersFromFile() throws IOException {
        if (!earlyTweakers.exists()) {
            earlyTweakers.mkdirs();
            return;
        }
        try (Stream<Path> paths = Files.find(earlyTweakers.toPath(), 1, (p, a) -> !a.isDirectory())) {
            paths.forEach(path -> {
                File file = path.toFile();
                String name = file.getName();
                Constants.log.info(String.format("Considering early tweaker: %s", name));
                try {
                    Class<?> clazz = Class.forName(name, false, Launch.classLoader.getClass().getClassLoader());
                    if (!IEarlyTweaker.class.isAssignableFrom(clazz)) {
                        Constants.log.error(String.format("Failed to load early tweaker: %s - Not an early tweaker", name));
                        file.deleteOnExit();
                    } else {
                        //noinspection unchecked
                        EarlyTweakerRegistry.registerTweaker((Class<IEarlyTweaker>) clazz);
                        Constants.log.info(String.format("Loaded early tweaker: %s", name));
                    }
                } catch (ClassNotFoundException e) {
                    Constants.log.error(String.format("Failed to load early tweaker: %s", name));
                    file.deleteOnExit();
                }
            });
        }
    }

    public static void saveTweaker(Class<? extends IEarlyTweaker> tweakerClass) {
        try {
            if (earlyTweakers.exists() || earlyTweakers.mkdirs()) {
                File file = new File(earlyTweakers, tweakerClass.getName());

                if (file.createNewFile()) {
                    Constants.log.info(String.format("Saved new tweaker %s", tweakerClass.getName()));
                } else {
                    Constants.log.warn(String.format("Requested to save tweaker %s for loading, but %s already exists", tweakerClass.getName(), file));
                }
            }
        } catch (IOException e) {
            Constants.log.fatal(String.format("Failed to save %s.", tweakerClass.getName()), e);
            Utils.makeCrashReport(new RuntimeException(e), "Failed to save %s.");
        }
    }
}
