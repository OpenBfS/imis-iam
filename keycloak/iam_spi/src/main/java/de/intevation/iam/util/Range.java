/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.util;

public class Range {
    private int low = 0;
    private int high = Integer.MAX_VALUE;

    public Range(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public Range(String range) {
        if (range != null) {
            String[] ranges = range.split("-");
            try {
                low = Integer.valueOf(ranges[0]);
                high = Integer.valueOf(ranges[1]);
            } catch (NumberFormatException e) {
               // Ignore invalid range
            }
            if (high < low) {
                high = low;
            }
        }
    }

    public int getLow() {
        return low;
    }

    public int getHigh() {
        return high;
    }

}
