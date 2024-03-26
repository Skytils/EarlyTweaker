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

import com.google.common.primitives.Ints;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;

import java.util.*;

public class EarlyTweakerRegistry {
    public static Set<String> registeredTweakNames = new HashSet<>();
    static TreeSet<EarlyTweakerWrapper> tweakers = new TreeSet<>();

    public static void registerTweaker(Class<? extends IEarlyTweaker> tweaker) {
        if (registeredTweakNames.add(tweaker.getName())) {
            tweakers.add(new EarlyTweakerWrapper(tweaker));
        }
    }

    @Deprecated
    public static void injectTweakers() {
        @SuppressWarnings("unchecked")
        List<ITweaker> tweaks = (List<ITweaker>) Launch.blackboard.get("Tweaks");
        for (EarlyTweakerWrapper tweaker : tweakers.descendingSet()) {
            try {
                Constants.log.info(String.format("Injecting %s into Blackboard", tweaker.tweakerClass.getName()));
                tweaks.add(1, tweaker.tweakerClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                Constants.log.fatal(String.format("Failed to inject early tweaker: %s", tweaker.tweakerClass.getName()), e);
            }
        }
    }

    public static List<IEarlyTweaker> instantiateTweakers() {
        List<IEarlyTweaker> tweakers = new ArrayList<>();
        for (EarlyTweakerWrapper tweaker : EarlyTweakerRegistry.tweakers) {
            try {
                Constants.log.info(String.format("Instantiating early tweaker: %s", tweaker.tweakerClass.getName()));
                tweakers.add(tweaker.tweakerClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                Constants.log.fatal(String.format("Failed to instantiate early tweaker: %s", tweaker.tweakerClass.getName()), e);
            }
        }
        return tweakers;
    }

    static class EarlyTweakerWrapper implements Comparable<EarlyTweakerWrapper> {
        public Class<? extends IEarlyTweaker> tweakerClass;
        public int order;

        public EarlyTweakerWrapper(Class<? extends IEarlyTweaker> tweakerClass) {
            this.tweakerClass = tweakerClass;
            IEarlyTweaker.EarlyOrder annotation = tweakerClass.getAnnotation(IEarlyTweaker.EarlyOrder.class);
            this.order = annotation == null ? 0 : annotation.value();
        }


        @Override
        public int compareTo(EarlyTweakerWrapper o) {
            int ordering = Ints.saturatedCast((long) order - (long) o.order);
            if (ordering != 0) return ordering;
            return tweakerClass.getName().compareTo(o.tweakerClass.getName());
        }
    }
}
