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
import java.util.Set;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.models.KeycloakSession;

import de.intevation.iam.model.jpa.Institution;
import de.intevation.iam.model.representation.User;
import de.intevation.iam.util.CSVExporter;


public class ExportResource {

    private KeycloakSession session;

    /**
     * Constructor.
     *
     * @param session
     */
    public ExportResource(KeycloakSession session) {
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
     * @param attributes Attributes that are exported, if empty all attributes are exported
     * @return Resulting csv data
     */
    @GET
    @Path("/user")
    @SuppressWarnings("checkstyle:ParameterNumber")
    public Response exportUsers(
            @QueryParam("fieldSeparator") String fieldSeparator,
            @QueryParam("quoteType") String quoteType,
            @QueryParam("rowDelimiter") String rowDelimiter,
            @QueryParam("encoding") String encoding,
            @QueryParam("search") String search,
            @QueryParam("id") List<String> ids,
            @QueryParam("attributes") Set<String> attributes
    ) {
        CSVExporter<User> exporter = new CSVExporter<>();
        setCsvOptions(
            exporter, fieldSeparator, quoteType, rowDelimiter, encoding);

        UserResource userProvider = new UserResource(this.session);
        List<User> users = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            for (String id: ids) {
                users.add(userProvider.getUserById(id));
            }
        } else {
            users = userProvider.getUsers(search, null, null)
                .getList();
        }
        return doExport(exporter, users, attributes);
    }

    /**
     * Export institutions as csv.
     *
     * @param fieldSeparator Char used as field separator
     * @param quoteType Char used for quotes
     * @param rowDelimiter Char used as row delimiter
     * @param encoding Encoding to use
     * @param search Optional search parameter (ignored if IDs are given)
     * @param ids Multiple institution-IDs can be given to export specific institutions
     * @param attributes Attributes that are exported, if empty all attributes are exported
     * @return Institutions as CSV
     */
    @GET
    @Path("/institution")
    @SuppressWarnings("checkstyle:ParameterNumber")
    public Response exportInstitutions(
            @QueryParam("fieldSeparator") String fieldSeparator,
            @QueryParam("quoteType") String quoteType,
            @QueryParam("rowDelimiter") String rowDelimiter,
            @QueryParam("encoding") String encoding,
            @QueryParam("search") String search,
            @QueryParam("id") List<Integer> ids,
            @QueryParam("attributes") Set<String> attributes
    ) {
        CSVExporter<Institution> exporter = new CSVExporter<>();
        setCsvOptions(
            exporter, fieldSeparator, quoteType, rowDelimiter, encoding);

        InstitutionResource instProvider = new InstitutionResource(session);
        List<Institution> institutions = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            for (Integer id: ids) {
                institutions.add(instProvider.getInstitutionById(id));
            }
        } else {
            institutions = instProvider.getInstitutions(search, null, null, null, null)
                .getList();
        }
        return doExport(exporter, institutions, attributes);
    }

    private <T> void setCsvOptions(
        CSVExporter<T> exporter,
        String fieldSeparator,
        String quoteType,
        String rowDelimiter,
        String encoding
    ) {
        if (fieldSeparator == null || !(fieldSeparator.length() == 1)
            || quoteType == null || !(quoteType.length() == 1)
            || rowDelimiter == null || rowDelimiter.isEmpty()
        ) {
            throw new BadRequestException();
        }
        exporter.setFieldSeparator(
            fieldSeparator.charAt(0));
        exporter.setQuoteType(quoteType.charAt(0));
        exporter.setRowDelimiter(rowDelimiter);
        exporter.setEncoding(Charset.forName(encoding));
    }

    private <T> Response doExport(
        CSVExporter<T> exporter,
        List<T> objects,
        Set<String> attributes
    ) {
        InputStream result;
        try {
            result = exporter.export(objects, attributes);
        } catch (IllegalAccessException | InvocationTargetException
                | IOException | IntrospectionException e) {
            e.printStackTrace();
            throw new InternalServerErrorException();
        }

        return Response.ok(result, new MediaType(
                "text", "csv", exporter.getEncoding().name()))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"export.csv\"")
            .build();
    }
}
