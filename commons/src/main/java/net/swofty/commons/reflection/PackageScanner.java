package net.swofty.commons.reflection;

import org.reflections.Reflections;
import org.tinylog.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.stream.Stream;

public final class PackageScanner {
    private PackageScanner() {}

    public static <T> Stream<T> instantiateSubtypes(String packageName, Class<T> clazz) {
        return new Reflections(packageName).getSubTypesOf(clazz).stream()
                .filter(subClass -> !Modifier.isAbstract(subClass.getModifiers()))
                .map(subClass -> instantiate(subClass, clazz, packageName))
                .filter(Objects::nonNull);
    }

    private static <T> T instantiate(Class<? extends T> subClass, Class<T> clazz, String packageName) {
        try {
            return clazz.cast(subClass.getDeclaredConstructor().newInstance());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return null;
        } catch (InstantiationException | InvocationTargetException e) {
            Logger.error(e, "Failed to instantiate {} while scanning package {} for {}",
                    subClass.getSimpleName(), packageName, clazz.getSimpleName());
            return null;
        }
    }
}
