package com.matt.forgehax.util.mod.loader;

import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import com.matt.forgehax.Globals;
import com.matt.forgehax.Helper;
import com.matt.forgehax.mcversion.MCVersionChecker;
import com.matt.forgehax.util.mod.BaseMod;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created on 5/16/2017 by fr1kin
 */
public class ForgeHaxModLoader implements Globals {
    @SuppressWarnings("unchecked")
    public static Collection<Class<? extends BaseMod>> getClassesInPackage(String pack) {
        final List<Class<? extends BaseMod>> classes = Lists.newArrayList();
        try {
            ClassPath classPath = ClassPath.from(getClassLoader());
            classPath.getTopLevelClasses(pack).forEach(info -> {
                try {
                    Class<?> clazz = info.load();
                    if(clazz.isAnnotationPresent(RegisterMod.class)
                            && BaseMod.class.isAssignableFrom(clazz)
                            && clazz.getDeclaredConstructor() != null) { // will throw exception if it doesn't exist
                        MCVersionChecker.requireValidVersion(clazz);
                        classes.add((Class<? extends BaseMod>)clazz);
                    }
                } catch (Exception e) {
                    Helper.getLog().warn(String.format("[%s] '%s' is not a valid mod class: %s", e.getClass().getSimpleName(), info.getSimpleName(), e.getMessage()));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.unmodifiableCollection(classes);
    }

    public static Collection<BaseMod> loadClasses(Collection<Class<? extends BaseMod>> classes) {
        List<BaseMod> mods = Lists.newArrayList();
        classes.forEach(clazz -> {
            try {
                mods.add(clazz.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                Helper.printStackTrace(e);
                Helper.getLog().warn(String.format("Failed to create a new instance of '%s': %s", clazz.getSimpleName(), e.getMessage()));
            }
        });
        return Collections.unmodifiableCollection(mods);
    }

    private static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
