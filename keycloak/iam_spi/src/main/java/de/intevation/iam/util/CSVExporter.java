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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
        String[] properties = getHeader(objects.get(0));
        String[] attributes = getAttributes(objects.toArray());
        ArrayList<String> header = new ArrayList<>();
        header.addAll(Arrays.asList(attributes));
        header.addAll(Arrays.asList(properties));
        CSVFormat format = CSVFormat.DEFAULT
            .withDelimiter(fieldSeparator)
            .withQuote(quoteType)
            .withRecordSeparator(rowDelimiter)
            .withHeader(header.toArray(String[]::new));
        try (CSVPrinter printer = new CSVPrinter(result, format)) {
            for (T object: objects) {
                ArrayList<String> row = new ArrayList<String>();
                // Add User attributes
                Field field;
                Map<String, List<String>> objectAttributes = new HashMap<>();
                try {
                    field = object.getClass().getDeclaredField("attributes");
                    field.setAccessible(true);
                    objectAttributes = (Map<String, List<String>>) field.get(object);
                } catch (NoSuchFieldException e) {
                    // Skip if object has no attributes
                }
                for (String attribute : attributes) {
                    String values = "";
                    if (objectAttributes.containsKey(attribute)) {
                            List<String> attributeList = objectAttributes.get(attribute);
                            if (attributeList.size() > 1) {
                                values += attributeList.toString();
                            } else if (!attributeList.isEmpty()) {
                                values += attributeList.get(0);
                            }
                    }
                    row.add(values);
                }
                for (PropertyDescriptor propertyDescriptor
                        : getPropertyDescriptors(object)) {
                    String name = propertyDescriptor.getName();
                    if (name.equals("attributes") || name.equals("id")) {
                        continue;
                    }
                    Object value = propertyDescriptor.getReadMethod()
                        .invoke(object);
                    row.add(value != null ? value.toString() : "");
                }
                printer.printRecord(row);
            }
        }
        return new ByteArrayInputStream(
            encoding.encode(result.toString()).array());
    }

    private String[] getHeader(Object object) throws IntrospectionException {
        return getHeader(object, true);
    }

    private String[] getAttributes(Object[] objects) {
        Set<String> attributes = new HashSet<String>();
        for (Object object : objects) {
            Field field;
            try {
                field = object.getClass().getDeclaredField("attributes");
                field.setAccessible(true);
                Map<String, List<String>> objectAttributes = (Map<String, List<String>>) field.get(object);
                for (String key : objectAttributes.keySet()) {
                    if (key.startsWith("LDAP")) {
                        continue;
                    }
                    attributes.add(key);
                }
            } catch (NoSuchFieldException e) {
                // Ignore objects with no attributes
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return attributes.toArray(String[]::new);
    }

    private String[] getHeader(Object object, boolean skipId) throws IntrospectionException {
        ArrayList<String> fields = new ArrayList<String>();

        for (
            PropertyDescriptor propertyDescriptor
            : getPropertyDescriptors(object)) {
            if (skipId && propertyDescriptor.getName().equals("id")
                || propertyDescriptor.getName().equals("attributes")) {
                continue;
            }
            fields.add(propertyDescriptor.getName());
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
