/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

import de.intevation.iam.model.jpa.Institution;
import de.intevation.iam.model.representation.User;
import de.intevation.iam.util.CSVExporter;
import de.intevation.iam.util.Constants;
import de.intevation.iam.util.I18nUtils;

import jakarta.ws.rs.core.Response.Status;

public class ExportProvider implements RealmResourceProvider {

    private KeycloakSession session;

    private enum CsvOptions {
        comma(","), period("."), semicolon(";"), space(" "),
        singlequote("'"), doublequote("\""),
        linux("\n"), windows("\r\n");

        private final String value;

        CsvOptions(String v) {
            this.value = v;
        }

        public char getChar() {
            return this.value.charAt(0);
        }
    }

    private Boolean isAccepted(String name, CsvOptions[] options) {
        for (CsvOptions opt : options) {
            if (opt.equals(CsvOptions.valueOf(name))) {
                return true;
            }
        }
        return false;
    }

    private Boolean isValidFieldSeperator(String fieldSeparator) {
        CsvOptions[] fieldSperators = {CsvOptions.comma,
                CsvOptions.period, CsvOptions.semicolon, CsvOptions.space };
        return isAccepted(fieldSeparator, fieldSperators);
    }

    private Boolean isValidQuoteType(String quoteType) {
        CsvOptions[] quoteTypes = {CsvOptions.singlequote,
                CsvOptions.doublequote };
        return isAccepted(quoteType, quoteTypes);
    }

    private Boolean isValidRowDelimiter(String rowDelimiter) {
        CsvOptions[] rowFelimiters = {CsvOptions.windows,
                CsvOptions.linux };
        return isAccepted(rowDelimiter, rowFelimiters);
    }

    /**
     * Constructor.
     *
     * @param session
     */
    public ExportProvider(KeycloakSession session) {
        this.session = session;
    }

    /**
     * Export users to csv.
     *
     * @param fieldSeparator Char used as field separator
     * @param quoteType Char used for quotes
     * @param rowDelimiter Char used as row delimiter
     * @param encoding Encoding to use
     * @param search Optional search parameter (ignored if IDs are given)
     * @param ids Multiple user-IDs can be given to export specific users
     * @param headers Request headers
     * @return Resulting csv data
     */
    @GET
    @Path("/user")
    public Response exportUsers(
            @QueryParam("fieldSeparator") String fieldSeparator,
            @QueryParam("quoteType") String quoteType,
            @QueryParam("rowDelimiter") String rowDelimiter,
            @QueryParam("encoding") String encoding,
            @QueryParam("search") String search,
            @QueryParam("id") List<String> ids,
            @Context HttpHeaders headers
    ) {
        CSVExporter<User> exporter = new CSVExporter<>();
        try {
            setCsvOptions(
                exporter, fieldSeparator, quoteType, rowDelimiter, encoding);
        } catch (IllegalArgumentException iae) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        ResourceBundle i18n = I18nUtils.getI18nBundle(session,
            headers.getHeaderString(Constants.SHIB_USER_HEADER));

        UserProvider userProvider = new UserProvider(session);
        List<User> users = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            for (String id: ids) {
                users.add(userProvider.getUserById(id, headers));
            }
        } else {
            users = userProvider.getUsers(headers, search, null, null)
                .getList();
        }
        return doExport(exporter, users, i18n);
    }

    /**
     * Export institutions as csv.
     *
     * @param fieldSeparator Char used as field separator
     * @param quoteType Char used for quotes
     * @param rowDelimiter Char used as row delimiter
     * @param encoding Encoding to use
     * @param search Optional search parameter
     * @param headers Request headers
     * @return Institutions as CSV
     */
    @GET
    @Path("/institution")
    public Response exportInstitutions(
            @QueryParam("fieldSeparator") String fieldSeparator,
            @QueryParam("quoteType") String quoteType,
            @QueryParam("rowDelimiter") String rowDelimiter,
            @QueryParam("encoding") String encoding,
            @QueryParam("search") String search,
            @Context HttpHeaders headers
    ) {
        CSVExporter<Institution> exporter = new CSVExporter<>();
        try {
            setCsvOptions(
                exporter, fieldSeparator, quoteType, rowDelimiter, encoding);
        } catch (IllegalArgumentException iae) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        ResourceBundle i18n = I18nUtils.getI18nBundle(session,
                headers.getHeaderString(Constants.SHIB_USER_HEADER));

        InstitutionProvider instProvider = new InstitutionProvider(session);
        return doExport(
            exporter, instProvider.getInstitutions(
                headers, search, null, null, null, null).getList(), i18n);
    }

    private <T> void setCsvOptions(
        CSVExporter<T> exporter,
        String fieldSeparator,
        String quoteType,
        String rowDelimiter,
        String encoding
    ) {
        if (fieldSeparator == null || !isValidFieldSeperator(fieldSeparator)
            || quoteType == null || !isValidQuoteType(quoteType)
            || rowDelimiter == null || !isValidRowDelimiter(rowDelimiter)
        ) {
            throw new IllegalArgumentException();
        }
        exporter.setFieldSeparator(
            CsvOptions.valueOf(fieldSeparator).getChar());
        exporter.setQuoteType(CsvOptions.valueOf(quoteType).getChar());
        exporter.setRowDelimiter(CsvOptions.valueOf(rowDelimiter).getChar());
        exporter.setEncoding(Charset.forName(encoding));
    }

    private <T> Response doExport(
        CSVExporter<T> exporter,
        List<T> objects,
        ResourceBundle i18n
    ) {
        InputStream result;
        try {
            result = exporter.export(objects);
        } catch (IllegalAccessException | InvocationTargetException
                | IOException | IntrospectionException e) {
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity(i18n.getString("error_csv_generic"))
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                .entity(i18n.getString("error_csv_options"))
                .build();
        }

        return Response.ok(result, new MediaType(
                "text", "csv", exporter.getEncoding().name()))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"export.csv\"")
            .build();
    }

    @Override
    public void close() {
    }

    @Override
    public Object getResource() {
        return this;
    }
}
