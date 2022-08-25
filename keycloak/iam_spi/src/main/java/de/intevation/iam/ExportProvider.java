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
import java.util.Arrays;

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
import javax.ws.rs.core.Response.Status;

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

    private Boolean isAccepted(String name, CsvOptions options[]) {
        for (CsvOptions opt : options) {
            try {
                if (opt.equals(CsvOptions.valueOf(name))) {
                    return true;
                }
            } catch (IllegalArgumentException iae) {
                return false;
            }
        }
        return false;
    }

    private Boolean isValidFieldSeperator(String fieldSeparator) {
        CsvOptions[] fieldSperators = { CsvOptions.comma,
                CsvOptions.period, CsvOptions.semicolon, CsvOptions.space };
        return isAccepted(fieldSeparator, fieldSperators);
    }

    private Boolean isValidQuoteType(String quoteType) {
        CsvOptions[] quoteTypes = { CsvOptions.singlequote,
                CsvOptions.doublequote };
        return isAccepted(quoteType, quoteTypes);
    }

    private Boolean isValidRowDelimiter(String rowDelimiter) {
        CsvOptions[] rowFelimiters = { CsvOptions.windows,
                CsvOptions.linux };
        return isAccepted(rowDelimiter, rowFelimiters);
    }

    private Boolean isValidEncoding(String encoding) {
        return Arrays.asList("utf-8", "utf-16", "ascii").contains(encoding);
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
     * @param quoteType      Char used for quotes
     * @param rowDelimiter   Char used as row delimiter
     * @param encoding       Encoding to use
     * @return Resulting csv data
     */
    @GET
    @Path("/user")
    public Response exportUsers(
            @QueryParam("fieldSeparator") String fieldSeparator,
            @QueryParam("quoteType") String quoteType,
            @QueryParam("rowDelimiter") String rowDelimiter,
            @QueryParam("encoding") String encoding) {
        CSVExporter<User> exporter = new CSVExporter<User>();
        if (fieldSeparator != null) {
            if (!isValidFieldSeperator(fieldSeparator)) {
                return Response.status(Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
            exporter.setFieldSeparator(
                    CsvOptions.valueOf(fieldSeparator).getChar());
        }
        if (quoteType != null) {
            if (!isValidQuoteType(quoteType)) {
                return Response.status(Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
            exporter.setQuoteType(
                    CsvOptions.valueOf(quoteType).getChar());
        }
        if (rowDelimiter != null) {
            if (!isValidRowDelimiter(rowDelimiter)) {
                return Response.status(Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
            exporter.setRowDelimiter(
                    CsvOptions.valueOf(rowDelimiter).getChar());
        }
        if (encoding != null) {
            if (!isValidEncoding(encoding)) {
                return Response.status(Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
            Charset charset = Charset.forName(encoding);
            exporter.setEncoding(charset);
        }

        UserProvider userProvider = new UserProvider(session);
        Response userResponse = userProvider.getUsers();
        ArrayList<User> users = userResponse.readEntity(ArrayList.class);
        InputStream result = exporter.export(users);
        return Response.ok(result, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition",
                        "attachment; filename=\"export.csv\"")
                .build();
    }

    /**
     * Export institutions as csv.
     *
     * @param fieldSeparator Char used as field separator
     * @param quoteType      Char used for quotes
     * @param rowDelimiter   Char used as row delimiter
     * @param encoding       Encoding to use
     * @return Institutions as CSV
     */
    @GET
    @Path("/institution")
    public Response exportInstitutions(
            @QueryParam("fieldSeparator") String fieldSeparator,
            @QueryParam("quoteType") String quoteType,
            @QueryParam("rowDelimiter") String rowDelimiter,
            @QueryParam("encoding") String encoding) {
        CSVExporter<Institution> exporter = new CSVExporter<Institution>();
        if (fieldSeparator != null) {
            if (!isValidFieldSeperator(fieldSeparator)) {
                return Response.status(Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
            exporter.setFieldSeparator(
                    CsvOptions.valueOf(fieldSeparator).getChar());
        }
        if (quoteType != null) {
            if (!isValidQuoteType(quoteType)) {
                return Response.status(Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
            exporter.setQuoteType(
                    CsvOptions.valueOf(quoteType).getChar());
        }
        if (rowDelimiter != null) {
            if (!isValidRowDelimiter(rowDelimiter)) {
                return Response.status(Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
            exporter.setRowDelimiter(
                    CsvOptions.valueOf(rowDelimiter).getChar());
        }
        if (encoding != null) {
            if (!isValidEncoding(encoding)) {
                return Response.status(Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
            Charset charset = Charset.forName(encoding);
            exporter.setEncoding(charset);
        }

        InstitutionProvider instProvider = new InstitutionProvider(session);
        Response instResponse = instProvider.getInstitutions();
        ArrayList<Institution> institutions = instResponse.readEntity(ArrayList.class);
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
