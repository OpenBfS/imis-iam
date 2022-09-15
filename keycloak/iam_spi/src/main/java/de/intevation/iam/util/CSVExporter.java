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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.intevation.iam.model.UserIamAttributes;

public class CSVExporter<T> {

    private char fieldSeparator = ',';
    private char quoteType = '\"';
    private char rowDelimiter = '\n';
    private Charset encoding = Charset.forName("UTF-8");

    /**
     * Export objects.
     * @param objects
     * @return Export as InputStream
     * @throws IOException Thrown if an error during csv printing occured
     * @throws InvocationTargetException Thrown if attributes could not be read
     * @throws IllegalArgumentException Thrown if the csv options are invalid
     * @throws IllegalAccessException Thrown if attributes could not be read
     * @throws IntrospectionException
     */
    public InputStream export(ArrayList<T> objects)
            throws IOException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            IntrospectionException {
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
            CSVPrinter printer = new CSVPrinter(result, format);
            for (T object: objects) {
                ArrayList<String> row = new ArrayList<String>();
                for (PropertyDescriptor propertyDescriptor
                        : getPropertyDescriptors(object)) {
                    Object value = propertyDescriptor.getReadMethod()
                        .invoke(object);
                    if (value != null && value.getClass()
                        == de.intevation.iam.model.UserIamAttributes.class
                    ) {
                        row.addAll(parseNestedModel(value));
                    } else {
                        row.add(value != null? value.toString(): "");
                    }
                }
                printer.printRecord(row);
            }
            printer.close(true);

        return new ByteArrayInputStream(
            result.toString().getBytes(encoding));
    }

    ArrayList<String> parseNestedModel(Object object)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, IntrospectionException {
        ArrayList<String> row = new ArrayList<String>();
        for (PropertyDescriptor propertyDescriptor
            : getPropertyDescriptors(object)) {
            if (propertyDescriptor.getName().equals("id")) {
                continue;
            }
            Object value;
            value = propertyDescriptor.getReadMethod()
                .invoke(object);
            if (value != null) {
                row.add(value.toString());
            } else {
                row.add("");
            }
        }
        return row;
    }

    private String[] getHeader(Object object) throws IntrospectionException {
        return getHeader(object, false);
    }

    private String[] getHeader(Object object, boolean skipId) throws IntrospectionException {
        ArrayList<String> fields = new ArrayList<String>();

        for (
            PropertyDescriptor propertyDescriptor
            : getPropertyDescriptors(object)) {
            if (skipId && propertyDescriptor.getName().equals("id")) {
                continue;
            }
            //Check for nested models
            if (propertyDescriptor.getPropertyType()
                == de.intevation.iam.model.UserIamAttributes.class) {
                String[] nestedHeader = getHeader(new UserIamAttributes(), true);
                fields.addAll(Arrays.asList(nestedHeader));
            } else {
                fields.add(propertyDescriptor.getName());
            }
        }
        return fields.toArray(new String[0]);
    }

    private PropertyDescriptor[] getPropertyDescriptors(Object object)
            throws IntrospectionException {
        PropertyDescriptor[] propertyDescriptorArray
            = Introspector.getBeanInfo(
                object.getClass()).getPropertyDescriptors();
        ArrayList<PropertyDescriptor> propertyDescriptors
            = new ArrayList<PropertyDescriptor>(
                Arrays.asList(propertyDescriptorArray));
        propertyDescriptors.removeIf(entry -> {
            if (entry.getName().equals("class")) {
                return true;
            }
            Field field;
            try {
                field = object.getClass().getDeclaredField(entry.getName());
            } catch (NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
                return true;
            }
            return field.getAnnotationsByType(JsonIgnore.class).length > 0;
        });

        PropertyDescriptor[] array
            = new PropertyDescriptor[propertyDescriptors.size()];
        array = propertyDescriptors.toArray(array);
        return array;
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
