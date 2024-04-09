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
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.*;

import static gg.skytils.earlytweaker.Constants.*;

public class EarlyTweakerLoader implements ITweaker {
    private static List<IEarlyTweaker> tweakers;

    public EarlyTweakerLoader() throws Throwable {
        Launch.blackboard.put(LOADED_KEY, true);
        Constants.log.info("Searching for tweak classes");
        EarlyTweakerFinder.findTweakersFromRegistrants();
        tweakers = EarlyTweakerRegistry.instantiateTweakers();
    }

    public static void ensureLoaded(Class<?> sourceClass) {
        if (!Launch.blackboard.containsKey(LOADED_KEY)) {
            try {
                ensureVersion(VERSION, sourceClass);
            } catch (IOException | URISyntaxException e) {
                Utils.makeCrashReport(new RuntimeException(e), "Failed to extract early tweaker jar");
            }
            Utils.makeCrashReport(new RuntimeException("EarlyTweaker not loaded"), "EarlyTweaker not loaded");
        }
    }

    public static void ensureTweakerLoaded(Class<? extends IEarlyTweaker> tweakerClass) throws RuntimeException {
        if (!Launch.blackboard.containsKey(LOADED_KEY)) {
            try {
                ensureVersion(VERSION, tweakerClass);
            } catch (IOException | URISyntaxException e) {
                Utils.makeCrashReport(new RuntimeException(e), "Failed to extract early tweaker jar");
            }
            Utils.makeCrashReport(new RuntimeException("EarlyTweaker not loaded"), "EarlyTweaker not loaded");
        } else if (!EarlyTweakerRegistry.registeredTweakNames.contains(tweakerClass.getName())) {
            Utils.makeCrashReport(new RuntimeException("Requested EarlyTweaker was not loaded: " + tweakerClass.getName()), "Requested EarlyTweaker was not loaded");
        }
    }

    public static void ensureVersion(String version, Class<?> sourceClass) throws IOException, URISyntaxException {
        File coremodsFolder = new File("./mods/", MC_VERSION);
        if (!coremodsFolder.exists() && !coremodsFolder.mkdirs()) {
            Utils.makeCrashReport(
                    new RuntimeException("Failed to create coremods folder"),
                    "Failed to create coremods folder"
            );
        } else {
            File earlyTweaker = new File(coremodsFolder, "!!!!!!!!!!EarlyTweaker.jar");
            Constants.log.info(String.format("Version %s was requested", version));
            if (!earlyTweaker.exists()) {
                Constants.log.info("No version found, extracting new version");
                extract(new File(sourceClass.getProtectionDomain().getCodeSource().getLocation().toURI()), earlyTweaker);
                Utils.makeCrashReport(new RuntimeException("Extracted new version"), "Extracted new version");
            } else {
                try (JarFile jarFile = new JarFile(earlyTweaker)) {
                    String locVersion = jarFile.getManifest().getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);

                    Constants.log.info(String.format("Found version %s", locVersion));

                    DefaultArtifactVersion current = new DefaultArtifactVersion(locVersion);
                    DefaultArtifactVersion target = new DefaultArtifactVersion(version);
                    if (current.compareTo(target) < 0) {
                        Constants.log.info(String.format("Version %s is outdated, extracting new version", locVersion));
                        extract(new File(sourceClass.getProtectionDomain().getCodeSource().getLocation().toURI()), earlyTweaker);
                        Utils.makeCrashReport(new RuntimeException("Extracted new version"), "Extracted new version");
                    }
                }
            }
        }
    }

    public static void extract(File currentLoc, File file) throws IOException {
        Path temp = Files.createTempFile("EarlyTweaker-extraction", null);

        Constants.log.info(String.format("Requested extraction from %s to %s", currentLoc, temp));

        if (!currentLoc.isFile()) {
            Utils.makeCrashReport(new RuntimeException("Failed to extract early tweaker jar: currentLoc is not a file"), "Failed to extract early tweaker jar: currentLoc is not a file");
        } else {
            Constants.log.info(String.format("Extracting %s to %s", currentLoc, temp));
            try (JarFile jarFile = new JarFile(currentLoc); JarOutputStream jos = new JarOutputStream(Files.newOutputStream(temp))) {
                jarFile.stream().filter(e -> e.getName().startsWith("gg/skytils/earlytweaker/")).forEach(e -> {
                    try {
                        jos.putNextEntry(e);
                        IOUtils.copy(jarFile.getInputStream(e), jos);
                        jos.closeEntry();
                    } catch (IOException ex) {
                        Constants.log.fatal(String.format("Failed to extract %s", e.getName()), ex);
                    }
                });
                jos.putNextEntry(new JarEntry(JarFile.MANIFEST_NAME));
                Manifest manifest = new Manifest();
                manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
                manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, VERSION);
                manifest.getMainAttributes().putValue("TweakClass", EarlyTweakerLoader.class.getName());
                manifest.getMainAttributes().putValue("TweakOrder", "-1000000");
                manifest.write(jos);
                jos.closeEntry();
            }
            if (!file.exists()) {
                Files.move(temp, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Constants.log.info(String.format("Extracted %s to %s", currentLoc, file));
            } else {
                Path logLoc = temp.resolveSibling(temp.getFileName() + ".log");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try (PrintWriter log = new PrintWriter(Files.newBufferedWriter(logLoc))) {
                        try (
                                FileInputStream in = new FileInputStream(temp.toFile()); FileChannel source = in.getChannel();
                                FileOutputStream out = new FileOutputStream(file); FileChannel destination = out.getChannel()
                        ) {
                            source.transferTo(0, source.size(), destination);
                            log.println(String.format("Extracted %s to %s", currentLoc, file));
                        } catch (Exception e) {
                            log.println(String.format("Failed to extract %s to %s", currentLoc, file));
                            e.printStackTrace(log);
                        }
                    } catch (IOException e) {
                        Constants.log.fatal("Failed to write extraction log", e);
                    }
                }));
                Constants.log.info(String.format("Queued extraction of %s to %s", currentLoc, file));
            }
        }
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        tweakers.forEach(tweaker -> {
            try {
                Constants.log.info(String.format("Running acceptOptions for %s", tweaker.getClass().getName()));
                tweaker.acceptOptions(args, gameDir, assetsDir, profile);
            } catch (Throwable e) {
                Constants.log.fatal(String.format("Failed to run acceptOptions for %s", tweaker.getClass().getName()), e);
            }
        });
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        tweakers.forEach(tweaker -> {
            try {
                Constants.log.info(String.format("Running injectIntoClassLoader for %s", tweaker.getClass().getName()));
                tweaker.injectIntoClassLoader(classLoader);
            } catch (Throwable e) {
                Constants.log.fatal(String.format("Failed to run injectIntoClassLoader for %s", tweaker.getClass().getName()), e);
            }
        });
    }

    @Override
    public String[] getLaunchArguments() {
        ArrayList<String> args = new ArrayList<>();
        tweakers.forEach(tweaker -> {
            try {
                Constants.log.info(String.format("Running getLaunchArguments for %s", tweaker.getClass().getName()));
                args.addAll(Arrays.asList(tweaker.getLaunchArguments()));
            } catch (Throwable e) {
                Constants.log.fatal(String.format("Failed to run getLaunchArguments for %s", tweaker.getClass().getName()), e);
            }
        });
        return args.toArray(new String[0]);
    }

    @Override
    public String getLaunchTarget() {
        throw new UnsupportedOperationException("EarlyTweaker should not be the primary tweaker");
    }
}
