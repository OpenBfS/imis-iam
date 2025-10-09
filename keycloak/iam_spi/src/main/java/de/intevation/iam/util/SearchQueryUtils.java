/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Adapted by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 * to return Map<String, List<String>> instead of originally Map<String, String>, adding support to search for multiple fields
 * source: https://github.com/keycloak/keycloak/blob/26.3.4/services/src/main/java/org/keycloak/utils/SearchQueryUtils.java
 */
package de.intevation.iam.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 * modified by Paul Schwabauer <paul.schwabauer@intevation.de>
 */
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
