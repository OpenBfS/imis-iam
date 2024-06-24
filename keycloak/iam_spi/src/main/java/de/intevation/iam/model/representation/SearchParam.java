/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
package de.intevation.iam.model.representation;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;


/**
 * Readonly representation of search parameter.
 */
public class SearchParam {


    private final Map<String, String> attributes;

    /**
     * Create a SearchParam instance from the given search param model.
     * @param searchParam SearchParam model to use
     */
    public SearchParam(String searchParam) throws WebApplicationException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<HashMap<String, String>> typeRef = new TypeReference<>() { };
            this.attributes = mapper.readValue(searchParam, typeRef);
        } catch (Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Could not parse json search param: " + e.getMessage())
                    .build());
        }
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
}
