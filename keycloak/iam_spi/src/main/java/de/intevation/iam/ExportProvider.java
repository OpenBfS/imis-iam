/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

import de.intevation.iam.model.Institution;
import de.intevation.iam.model.User;
import de.intevation.iam.util.CSVExporter;

public class ExportProvider implements RealmResourceProvider {

    private KeycloakSession session;

    /**
     * Constructor.
     * @param session
     */
    public ExportProvider(KeycloakSession session) {
        this.session = session;
    }

    /**
     * Export users to csv.
     * @param fieldSeparator Char used as field separator
     * @param quoteType Char used for quotes
     * @param rowDelimiter Char used as row delimiter
     * @param encoding Encoding to use
     * @return Resulting csv data
     */
    @GET
    @Path("/user")
    public Response exportUsers(
        @QueryParam("fieldSeparator") Character fieldSeparator,
        @QueryParam("quoteType") Character quoteType,
        @QueryParam("rowDelimiter") Character rowDelimiter,
        @QueryParam("encoding") String encoding
    ) {
        CSVExporter<User> exporter = new CSVExporter<User>();
        if (fieldSeparator != null) {
            exporter.setFieldSeparator(fieldSeparator);
        }
        if (quoteType != null) {
            exporter.setQuoteType(quoteType);
        }
        if (rowDelimiter != null) {
            exporter.setRowDelimiter(rowDelimiter);
        }
        if (encoding != null) {
            Charset charset = Charset.forName(encoding);
            exporter.setEncoding(charset);
        }

        UserProvider userProvider = new UserProvider(session);
        Response userResponse = userProvider.getUsers();
        ArrayList<User> users
            = userResponse.readEntity(ArrayList.class);
        InputStream result = exporter.export(users);
        return Response.ok(result, MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition",
                "attachment; filename=\"export.csv\"")
            .build();
    }

    /**
     * Export institutions as csv.
     * @param fieldSeparator Char used as field separator
     * @param quoteType Char used for quotes
     * @param rowDelimiter Char used as row delimiter
     * @param encoding Encoding to use
     * @return Institutions as CSV
     */
    @GET
    @Path("/institution")
    public Response exportInstitutions(
        @QueryParam("fieldSeparator") Character fieldSeparator,
        @QueryParam("quoteType") Character quoteType,
        @QueryParam("rowDelimiter") Character rowDelimiter,
        @QueryParam("encoding") String encoding) {
        CSVExporter<Institution> exporter = new CSVExporter<Institution>();
        if (fieldSeparator != null) {
            exporter.setFieldSeparator(fieldSeparator);
        }
        if (quoteType != null) {
            exporter.setQuoteType(quoteType);
        }
        if (rowDelimiter != null) {
            exporter.setRowDelimiter(rowDelimiter);
        }
        if (encoding != null) {
            Charset charset = Charset.forName(encoding);
            exporter.setEncoding(charset);
        }

        InstitutionProvider instProvider = new InstitutionProvider(session);
        Response instResponse = instProvider.getInstitutions();
        ArrayList<Institution> institutions
            = instResponse.readEntity(ArrayList.class);
        InputStream result = exporter.export(institutions);
        return Response.ok(result, MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition",
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
