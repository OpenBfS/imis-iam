/* Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model.representation;

import java.util.List;

/**
 * Readonly representation of a list of objects, with an size field.
 * @param <T> Type of objects in list.
 */
public class ObjectList<T> {

    private long size;
    private List<T> list;

    /**
     * Create a ObjectList instance.
     * @param size Size of the whole list.
     * @param list List of objects.
     */
    public ObjectList(long size, List<T> list) {
        this.size = size;
        this.list = list;
    }

    public List<T> getList() {
        return list;
    }

    public long getSize() {
        return size;
    }
}
