/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package gg.skytils.earlytweaker.utils;

import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import static gg.skytils.earlytweaker.Constants.log;

public class Utils {
    public static void makeCrashReport(RuntimeException e, String message) {
        try {
            File crashReports = new File("./crash-reports");
            Class<?> clazz = Launch.classLoader.loadClass("b");
            Method makeCrashReport = clazz.getDeclaredMethod("a", Throwable.class, String.class);
            makeCrashReport.setAccessible(true);
            Object crashReport = makeCrashReport.invoke(null, e, message);
            Method saveToFile = clazz.getDeclaredMethod("a", File.class);
            saveToFile.setAccessible(true);
            File reportFile = new File(crashReports, "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-client.txt");
            saveToFile.invoke(crashReport, reportFile);
            System.out.println("#@!@# Game crashed! Crash report saved to: #@!@# " + reportFile.getAbsolutePath());
            exitJava(-1);
        } catch (Throwable t) {
            log.error("Failed to create crash report", t);
            System.out.println("#@?@# Game crashed! Crash report could not be saved. #@?@#");
            exitJava(-2);
        }
    }

    public static void exitJava(int code) {
        try {
            Class<?> shutdownClazz = Class.forName("java.lang.Shutdown");
            Method exitMethod = shutdownClazz.getDeclaredMethod("exit", int.class);
            exitMethod.setAccessible(true);
            exitMethod.invoke(null,  code);
        } catch (Throwable t) {
            log.error("Failed to exit Java", t);
            throw new RuntimeException(t);
        }
    }
}
