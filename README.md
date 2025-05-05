# Identity management for IMIS applications

This application serves as central identity management for the IMIS
("Integriertes Mess- und Informationssystem") applications of the
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

### Client
A client based on [Vue.js](https://vuejs.org) and [Vuetify](https://vuetifyjs.com)
provides a GUI for REST interfaces provided by the backend (`client/`).
