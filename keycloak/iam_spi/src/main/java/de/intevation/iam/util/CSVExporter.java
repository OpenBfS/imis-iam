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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CSVExporter<T> {

    // Name of property that contains attributes defined in User Profile
    private static final String ATTRIBUTES_PROPERTY_NAME = "attributes";

    private char fieldSeparator = ',';
    private char quoteType = '\"';
    private String rowDelimiter = "\n";
    private Charset encoding = StandardCharsets.UTF_8;

    /**
     * Export objects.
     * @param objects Objects to export
     * @param exportAttributes Attributes that are exported, if empty all
     *                         attributes are exported
     * @return Export as InputStream
     * @throws IOException Thrown if an error during csv printing occurred
     * @throws InvocationTargetException Thrown if attributes could not be read
     * @throws IllegalAccessException Thrown if attributes could not be read
     * @throws IntrospectionException
     */
    public InputStream export(List<T> objects, Set<String> exportAttributes)
            throws IOException, IllegalAccessException,
            InvocationTargetException,
            IntrospectionException {
        if (objects.isEmpty()) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        List<PropertyDescriptor> propertyDescriptors =
            getPropertyDescriptors(objects.get(0));
        Set<String> attributes = getAttributeNames(objects);
        List<String> header = new ArrayList<>();
        header.addAll(attributes);
        header.addAll(propertyDescriptors.stream().map(d -> d.getName())
            .toList());
        if (exportAttributes != null && !exportAttributes.isEmpty()) {
            header.retainAll(exportAttributes);
        }
        CSVFormat format = CSVFormat.DEFAULT
            .withDelimiter(fieldSeparator)
            .withQuote(quoteType)
            .withRecordSeparator(rowDelimiter)
            .withHeader(header.toArray(String[]::new));
        try (CSVPrinter printer = new CSVPrinter(result, format)) {
            for (T object: objects) {
                List<String> row = new ArrayList<String>();
                // Add User attributes
                Map<String, List<String>> objectAttributes =
                    getObjectAttributes(object);
                for (String attribute : attributes) {
                    if (!header.contains(attribute)) {
                        continue;
                    }
                    String values = "";
                    if (objectAttributes.containsKey(attribute)) {
                        List<String> attributeList =
                            objectAttributes.get(attribute);
                        if (attributeList.size() > 1) {
                            values += attributeList.toString();
                        } else if (!attributeList.isEmpty()) {
                            values += attributeList.get(0);
                        }
                    }
                    row.add(values);
                }
                for (
                    PropertyDescriptor propertyDescriptor : propertyDescriptors
                ) {
                    if (!header.contains(propertyDescriptor.getName())) {
                        continue;
                    }
                    Object value = propertyDescriptor.getReadMethod()
                        .invoke(object);
                    row.add(value != null ? value.toString() : "");
                }
                printer.printRecord(row);
            }
        }
        return new ByteArrayInputStream(result.toString().getBytes(encoding));
    }

    private Set<String> getAttributeNames(List<T> objects)
        throws IllegalAccessException {
        Set<String> attributes = new LinkedHashSet<>();
        for (T object : objects) {
            for (String key : getObjectAttributes(object).keySet()) {
                if (key.startsWith("LDAP")
                    || key.equals("modifyTimestamp")
                    || key.equals("createTimestamp")) {
                    continue;
                }
                attributes.add(key);
            }
        }
        return attributes;
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<String>> getObjectAttributes(Object object)
        throws IllegalAccessException {
        Map<String, List<String>> objectAttributes = new HashMap<>();
        try {
            Field field = object.getClass().getDeclaredField(
                ATTRIBUTES_PROPERTY_NAME);
            field.setAccessible(true);
            objectAttributes = (Map<String, List<String>>) field.get(object);
        } catch (NoSuchFieldException e) {
            // Skip if object has no attributes
        }
        return objectAttributes;
    }

    private List<PropertyDescriptor> getPropertyDescriptors(T object)
            throws IntrospectionException {
        List<PropertyDescriptor> propertyDescriptors
            = new ArrayList<>(Arrays.asList(Introspector.getBeanInfo(
                        object.getClass()).getPropertyDescriptors()));

        // User Profile attributes are handled separately,
        // database IDs should not be exported
        final Set<String> skipAttrs = Set.of(
            "class", ATTRIBUTES_PROPERTY_NAME, "id");
        propertyDescriptors.removeIf(entry -> {
            if (skipAttrs.contains(entry.getName())) {
                return true;
            }
            Field field;
            try {
                field = object.getClass().getDeclaredField(entry.getName());
            } catch (NoSuchFieldException e) {
                // Skip properties not backed by a field
                return true;
            }
            return field.getAnnotationsByType(JsonIgnore.class).length > 0;
        });

        return propertyDescriptors;
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

    public String getRowDelimiter() {
        return rowDelimiter;
    }

    public void setRowDelimiter(String rowDelimiter) {
        this.rowDelimiter = rowDelimiter;
    }

    public Charset getEncoding() {
        return encoding;
    }

    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }
}
