package dev.daweapon.commons.skyblock.item.attribute;

import dev.daweapon.commons.reflection.PackageScanner;
import lombok.Getter;
import lombok.Setter;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Setter
@Getter
// Base class for a single named, string-serializable piece of item metadata, with a registry of all discovered subclasses.
public abstract class ItemAttribute<T> {
    private static final ArrayList<ItemAttribute> attributes = new ArrayList<>();
    private static volatile List<Constructor<? extends ItemAttribute>> constructors;

    public T value;

    public ItemAttribute() {
        value = getDefaultValue(null);
    }

    public abstract String getKey();

    public abstract T getDefaultValue(@Nullable ItemStatistics defaultStatistics);

    public abstract T loadFromString(String string);

    public abstract String saveIntoString();

    public static void registerItemAttributes() {
        attributes.clear();
        loopThroughPackage(
                "dev.daweapon.commons.skyblock.item.attribute.attributes", ItemAttribute.class
        ).forEach(attributes::add);
    }

    public static Collection<ItemAttribute> getPossibleAttributes() {
        List<ItemAttribute> result = new ArrayList<>(constructors().size());
        for (Constructor<? extends ItemAttribute> constructor : constructors()) {
            try {
                result.add(constructor.newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private static List<Constructor<? extends ItemAttribute>> constructors() {
        List<Constructor<? extends ItemAttribute>> cached = constructors;
        if (cached != null && cached.size() == attributes.size()) return cached;
        List<Constructor<? extends ItemAttribute>> built = new ArrayList<>(attributes.size());
        for (ItemAttribute attribute : attributes) {
            try {
                built.add(attribute.getClass().getDeclaredConstructor());
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        constructors = List.copyOf(built);
        return constructors;
    }

    public static <T> Stream<T> loopThroughPackage(String packageName, Class<T> clazz) {
        return PackageScanner.instantiateSubtypes(packageName, clazz);
    }
}
