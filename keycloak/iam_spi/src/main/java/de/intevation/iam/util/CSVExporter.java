/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import de.intevation.iam.model.UserIamAttributes;

public class CSVExporter<T> {

    private char fieldSeparator = ',';
    private char quoteType = '\"';
    private char rowDelimiter = ';';
    private Charset encoding = Charset.forName("UTF-8");

    /**
     * Export objects.
     * @param objects
     * @return Export as InputStream
     */
    public InputStream export(ArrayList<T> objects) {
        if (objects.size() == 0) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        String[] header = getHeader(objects.get(0));
        CSVFormat format = CSVFormat.DEFAULT
            .withDelimiter(fieldSeparator)
            .withQuote(quoteType)
            .withRecordSeparator(rowDelimiter)
            .withHeader(header);
        try {
            CSVPrinter printer = new CSVPrinter(result, format);
            objects.forEach(object -> {
                ArrayList<String> row = new ArrayList<String>();
                try {
                    for (PropertyDescriptor propertyDescriptor
                            : getPropertyDescriptors(object)) {
                        Object value = propertyDescriptor.getReadMethod()
                            .invoke(object);
                        if (value.getClass()
                            == de.intevation.iam.model.UserIamAttributes.class
                        ) {
                            row.addAll(parseNestedModel(value));
                        } else {
                            row.add(value.toString());
                        }
                    }
                    printer.printRecord(row);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
            printer.close(true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new ByteArrayInputStream(
            result.toString().getBytes(encoding));
    }

    ArrayList<String> parseNestedModel(Object object) {
        ArrayList<String> row = new ArrayList<String>();
        for (PropertyDescriptor propertyDescriptor
            : getPropertyDescriptors(object)) {
            Object value;
            try {
                value = propertyDescriptor.getReadMethod()
                    .invoke(object);
                if (value != null) {
                    row.add(value.toString());
                } else {
                    row.add("");
                }
            } catch (IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }
        return row;
    }

    private String[] getHeader(Object object) {
        ArrayList<String> fields = new ArrayList<String>();

        for (
            PropertyDescriptor propertyDescriptor
            : getPropertyDescriptors(object)) {
            //Check for nested models
            if (propertyDescriptor.getPropertyType()
                == de.intevation.iam.model.UserIamAttributes.class) {
                String[] nestedHeader = getHeader(new UserIamAttributes());
                fields.addAll(Arrays.asList(nestedHeader));
            } else {
                fields.add(propertyDescriptor.getName());
            }
        }
        System.out.println(fields.toString());
        return fields.toArray(new String[0]);
    }

    private PropertyDescriptor[] getPropertyDescriptors(Object object) {
        try {
            PropertyDescriptor[] propertyDescriptorArray
                = Introspector.getBeanInfo(
                    object.getClass()).getPropertyDescriptors();
            ArrayList<PropertyDescriptor> propertyDescriptors
                = new ArrayList<PropertyDescriptor>(
                    Arrays.asList(propertyDescriptorArray));
            propertyDescriptors.removeIf(entry -> {
                return entry.getName().equals("class");
            });

            PropertyDescriptor[] array
                = new PropertyDescriptor[propertyDescriptors.size()];
            array = propertyDescriptors.toArray(array);
            return array;
        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public char getFieldSeparator() {
        return fieldSeparator;
    }

    public void setFieldSeparator(char fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    public char getQuoteType() {
        return quoteType;
    }

    public void setQuoteType(char quoteType) {
        this.quoteType = quoteType;
    }

    public char getRowDelimiter() {
        return rowDelimiter;
    }

    public void setRowDelimiter(char rowDelimiter) {
        this.rowDelimiter = rowDelimiter;
    }

    public Charset getEncoding() {
        return encoding;
    }

    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

}
