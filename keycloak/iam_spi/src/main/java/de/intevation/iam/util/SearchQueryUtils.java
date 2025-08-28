/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchQueryUtils {

    public static Map<String, List<String>> getFields(final String query) {
        Map<String, List<String>> ret = new HashMap<>();
        char[] chars = query.trim().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            boolean inQuotes = false;
            boolean internal = false;
            StringBuilder name = new StringBuilder();
            while (i < chars.length && chars[i] != ':') {
                if (chars[i] == '\\') {
                    if (chars[i + 1] == '\"') {
                        i++;
                    } else if (chars[i + 1] == '\\') {
                        i += 2;
                        continue;
                    }
                } else if (chars[i] == '\"') {
                    if (!inQuotes && (!name.isEmpty())) {
                        internal = true;
                    } else if (internal) {
                        internal = false;
                    } else {
                        inQuotes = !inQuotes;
                        i++;
                        continue;
                    }
                } else if (chars[i] == ' ' && !inQuotes) {
                    break;
                }
                name.append(chars[i]);
                i++;
            }
            if (i == chars.length || chars[i] == ' ') {
                continue;
            }
            i++;
            inQuotes = false;
            internal = false;
            StringBuilder value = new StringBuilder();
            while (i < chars.length) {
                if (chars[i] == '\\') {
                    if (chars[i + 1] == '\"') {
                        i++;
                    } else if (chars[i + 1] == '\\') {
                        i += 2;
                        continue;
                    }
                } else if (chars[i] == '\"') {
                    if (!inQuotes && (!value.isEmpty())) {
                        internal = true;
                    } else if (internal) {
                        internal = false;
                    } else {
                        inQuotes = !inQuotes;
                        i++;
                        continue;
                    }
                } else if (chars[i] == ' ' && !inQuotes) {
                    break;
                }
                value.append(chars[i]);
                i++;
            }
            if (ret.containsKey(name.toString())) {
                ret.get(name.toString()).add(value.toString());
            } else {
                List<String> values = new ArrayList<>();
                values.add(value.toString());
                ret.put(name.toString(), values);
            }
        }
        return ret;
    }
}
