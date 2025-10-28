# Identity management for IMIS applications

This application serves as central identity and access management for the IMIS
("Integrated Measuring and Information System for the Surveillance of Environmental Radioactivity") applications of the
German Federal Office for Radiation Protection.


## Components

All components are orchestrated by a Docker Compose setup (`docker/`).

### Backend
[Keycloak](https://www.keycloak.org/) is used as central identity provider.
Specific requirements for user attributes can be met by configuring
the [User Profile](https://www.keycloak.org/docs/latest/server_admin/index.html#user-profile).
For requirements that cannot be met by User Profile configuration,
custom [JPA entities](https://www.keycloak.org/docs/latest/server_development/index.html#_extensions_jpa) are added as an extension (`keycloak/iam_spi/`).
The same extension also provides [REST interfaces](https://www.keycloak.org/docs/latest/server_development/index.html#_extensions_rest)
for the management of these specific aspects.

The REST interfaces provided by the extension generally exclude User Profile
attributes with an annotation "private" set to true, or which are part of
a group with such annotation. These attributes are only disclosed at
`user/profile` for the logged in user.

### Client
A client based on [Vue.js](https://vuejs.org) and [Vuetify](https://vuetifyjs.com)
provides a GUI for REST interfaces provided by the backend (`client/`).

## License

This software is licensed unter GPL version 3.0 or later.
It includes one file licensed unter Apache 2.0:
`keycloak/iam_spi/src/main/java/de/intevation/iam/util/SearchQueryUtils.java`

A copy of both license texts is included in directory [`LICENSES`](./LICENSES/)
