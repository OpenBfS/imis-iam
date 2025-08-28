/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.util;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SortUtil {

    public static <T> void sortByField(List<T> list, String fieldName, boolean ascending) {
        if (list == null || list.isEmpty() || fieldName == null || fieldName.isEmpty()) {
            return;
        }

        list.sort((o1, o2) -> {
            Object value1;
            Object value2;
            try {
                Field field = o1.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);

                value1 = field.get(o1);
                value2 = field.get(o2);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                try {
                    Field field = o1.getClass().getDeclaredField("attributes");
                    field.setAccessible(true);

                    value1 = field.get(o1);
                    value2 = field.get(o2);
                } catch (NoSuchFieldException | IllegalAccessException ex) {
                    return 0;
                }
                if (value1 instanceof Map && value2 instanceof Map) {
                    value1 = ((Map<?, ?>) value1).get(fieldName);
                    value2 = ((Map<?, ?>) value2).get(fieldName);
                }
            }

            if (value1 == null && value2 == null) {
                return 0;
            }
            if (value1 == null) {
                return ascending ? -1 : 1;
            }
            if (value2 == null) {
                return ascending ? 1 : -1;
            }
            int comparison;
            if (value1 instanceof List<?> && value2 instanceof List<?>) {

                if (((List<?>) value1).isEmpty()) {
                    return ascending ? -1 : 1;
                }

                if (((List<?>) value2).isEmpty()) {
                    return ascending ? 1 : -1;
                }
                // Compare least element
                Comparable<Object> c1 = Collections.min((List) value1);
                Comparable<Object> c2 = Collections.min((List) value2);

                comparison = c1.compareTo(c2);
            } else if (value1 instanceof Comparable) {
                Comparable<Object> c1 = (Comparable<Object>) value1;
                comparison = c1.compareTo(value2);
            } else {
                throw new IllegalArgumentException("Field '" + fieldName + "' is not comparable");
            }
            return ascending ? comparison : -comparison;
        });
    }
}
