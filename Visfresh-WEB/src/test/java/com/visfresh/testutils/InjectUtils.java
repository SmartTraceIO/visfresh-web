/**
 *
 */
package com.visfresh.testutils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class InjectUtils {
    /**
     * Default constructor.
     */
    private InjectUtils() {
        super();
    }

    public static void injectAutowired(final Object target, final Object injecting, final boolean overwrite) {
        final Class<?> injectingClass = injecting.getClass();
        final List<Field> fields = new LinkedList<>();

        //find all autowired fields
        Class<?> current = target.getClass();
        while (current != null && current != Object.class) {
            for(final Field f: current.getDeclaredFields()) {
                final Class<?> declaringClass = f.getType();
                if (!Modifier.isStatic(f.getModifiers())
                        && f.getAnnotation(Autowired.class) != null
                        && declaringClass.isAssignableFrom(injectingClass)) {
                    fields.add(f);
                }
            }

            current = current.getSuperclass();
        }

        //do inject
        for (final Field field : fields) {
            field.setAccessible(true);
            try {
                if (overwrite || field.get(target) == null) {
                    field.set(target, injecting);
                }
            } catch (final Exception e) {
                throw new RuntimeException("Failed to inject the bean", e);
            }
        }
    }
}
