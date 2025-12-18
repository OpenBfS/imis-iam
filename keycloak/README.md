# Keycloak Backend Components

This directory contains custom extensions and themes for Keycloak for the IMIS3 Identity Management system.

## Overview

Keycloak is used as the central identity provider for the IMIS applications. The backend is extended with:

1. **Custom SPI Extension** (`iam_spi/`) - Provides specific REST APIs and database tables
2. **Custom Theme** (`iam_theme/`) - Customizes login, and email templates

## License

GPL version 3.0 or later for all files except
`src/main/java/de/intevation/iam/util/SearchQueryUtils.java` which is under **Apache License 2.0**

See [LICENSES](../../LICENSES/) directory for complete license texts.
