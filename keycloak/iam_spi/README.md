# IAM SPI Extension

Custom Keycloak Service Provider Interface (SPI) extension providing domain-specific REST APIs and data models for the IMIS3 Identity Management system.

## Overview

This extension adds custom functionality to Keycloak through the SPI mechanism:

- **REST Resource Providers**: Custom HTTP API endpoints under `/realms/imis3/iam/*`
- **JPA Entity Providers**: Custom database entities (Institution, Event, Mail, Network)
- **Event Listener Providers**: Hooks into Keycloak lifecycle events
- **Email Template Providers**: Custom email template handling

## Architecture

### Package Structure

```
de/intevation/iam/
├── IamResource.java                    # Main REST resource router
├── IamResourceProvider.java            # Resource provider
├── IamResourceProviderFactory.java     # SPI factory
├── IamEventListenerProvider.java       # Event listener
├── IamEventListenerProviderFactory.java
├── auth/                               # Authorization layer
│   ├── Authorizer.java                 # Base authorizer
│   ├── UserAuthorizer.java
│   ├── InstitutionAuthorizer.java
│   ├── EventAuthorizer.java
│   ├── MailAuthorizer.java
│   ├── IaMRole.java                    # Role definitions
│   └── AuthorizationException.java
├── mail/                               # Mail functionality
│   ├── IamMailTemplateProvider.java
│   ├── IamMailTemplateProviderFactory.java
│   ├── MailScheduler.java              # Scheduled mail tasks
│   └── MailTask.java
├── model/                              # Data models
│   ├── jpa/                            # JPA entities
│   │   ├── Institution.java
│   │   ├── InstitutionTag.java
│   │   ├── Event.java
│   │   ├── Mail.java
│   │   ├── MailType.java
│   │   ├── Network.java
│   │   └── UserAttributes.java
│   └── representation/                 # DTOs for API
│       ├── User.java
│       ├── Role.java
│       └── ObjectList.java
├── validation/                         # Custom validators
│   └── [Custom constraint validators]
├── UserResource.java                   # User management endpoints
├── InstitutionResource.java            # Institution endpoints
├── MailResource.java                   # Mail endpoints
├── IamEventResource.java               # Event endpoints
├── NetworkResource.java                # Network endpoints
├── ExportResource.java                 # Data export endpoints
└── util/                               # Utilities
    ├── SearchQueryUtils.java           # Search query parsing
    ├── SortUtil.java
    ├── DateUtils.java
    ├── Constants.java
    ├── TimestampConverter.java
    └── TimestampConverterProvider.java
```

### SPI Registration

The custom SPI is registered through the [Java Service Provider Interface](https://www.baeldung.com/java-spi#bd-3-service-provider) mechanism using `META-INF`:

- `org.keycloak.connections.jpa.entityprovider.JpaEntityProviderFactory` - Registers custom JPA entities
- `org.keycloak.email.EmailTemplateProviderFactory` - Registers custom email templates
- `org.keycloak.events.EventListenerProviderFactory` - Registers event listeners
- `org.keycloak.services.resource.RealmResourceProviderFactory` - Registers custom REST endpoints

### Database and Data Models

Custom JPA entities extend Keycloak's schema with additional models:

- `Mail`: Email messages and archives, including `MailType`
- `UserAttributes`: Custom user attributes specific to IAM
- `Institution`: Institution data, including `Network`
- `InstitutionTag`: Institution tags
- `Event`: Event calendar

All entities use Keycloak's shared Hibernate EntityManager and PostgreSQL database.

The Database changelog including the SQL upgrade commands are in
`META-INF/db-changelog.xml`.

### Validation layers
- **Client-side**: Vuetify validation rules for immediate user feedback
- **Server-side**: Hibernate Validator with custom constraints and Jakarta Bean Validation annotations (plus some custom validations)

## Building

### Prerequisites

- Java
- Maven

### Build SPI Extension

```bash
cd keycloak/iam_spi
mvn clean package
```

## Code Quality

The SPI uses:

- **Maven Checkstyle**: Enforces coding standards (Javadoc, imports, whitespace, etc.), license headers and more
- **Maven Dependency Analysis**

## REST API Resources

The custom Keycloak SPI provides the following REST resources exposed under `/realms/imis3/iam/*`.

### User (`/user`)

Manages users with advanced search, filtering, and role assignment:

- `GET /userprofilemetadata`: Metadata (attribute description) of users profiles as JSON
- `GET /user/profile`: Get the profile data of the current user
- `PUT /user/profile`: Update the profile data of the current user
- `GET /user`: List and search users, with paginagion
- `GET /user/{id}`: Get single user details
- `POST /user`: Create a new user
- `PUT /user/`: Update an existing user
- `GET /user/roles`: List available roles

**Query Parameters** for `/user`:
- `search`: Search query
- `sortBy`: Sort by attribute
- `order`: Sort order (ascending, descending)
- `firstResult`: Pagination
- `maxResults`: Pagination

### Institution (`/institution`)

Manages institutions and organizations:

- `GET /institution`: List and search institutions
- `GET /institution/{id}`: Get single institution details
- `POST /institution`: Create a new institution
- `PUT /institution`: Update an existing institution
- `GET /institution/tag`: Get all institution tags

**Query Parameters** for `/institution`:
- `search`: Search query
- `sortBy`: Sort by attribute
- `order`: Sort order (ascending, descending)
- `firstResult`: Pagination
- `maxResults`: Pagination

### Event (`/event`)

Manages calendar events:

- `GET /event`: List events
- `GET /event/{id}`: Get single event details
- `POST /event`: Create a new event
- `PUT /event`: Update an existing event
- `DELETE /event/{id}`: Delete an event

### Mail (`/mail`)

Manages emails:

- `GET /mail`: List emails
- `GET /mail/archive/{id}`: Archive a given email
- `POST /iam/mail`: Send email to a mailing list
- `GET /mail/type`: Get all mail types

**Query Parameters** for `/mail`:
- `type`: Filter by type IDs
- `count`: Limit number of results
- `archived`: Return only (un)archived emails
- `start`: Start date
- `end`: End date
- `sender`: Filter on email sender
- `recipients`: Filter on email recipient

### Network Resource (`/iam/network`)

Manages institution networks:

- `GET /iam/networks` - List all networks

### Export Resource (`/iam/export`)

Exports data in CSV format:

- `POST /iam/export/user` - Export users to CSV
- `POST /iam/export/institution` - Export institutions to CSV

#### Parameters

**CSV Options**:

- `fieldSeparator`
- `quoteType`
- `rowDelimiter`
- `encoding`

**Query options**:

- `search`
- `ids`
- `attributes`

### API Response Format

Standard response format for list endpoints:
```json
{
  "size": 42,
  "list": [
    { "id": 1, "name": "..." },
    { "id": 2, "name": "..." }
  ]
}
```

## Authorization

### Role-Based Access Control

The authorization layer uses these RBAC roles:

```java
public enum IaMRole {
    CHIEF_EDITOR,   // Full access
    EDITOR,         // Modify own institution, see others
    USER            // Modify themselves, see others
}
```

### Authorizers

Each resource has a dedicated authorizer:

- **EventAuthorizer**: Validates event operations
- **InstitutionAuthorizer**: Validates institution operations
- **MailAuthorizer**: Validates mail operations
- **UserAuthorizer**: Validates user management operations

### Search Query Format

`SearchQueryUtils` (based on Keycloak's `SearchQueryUtils`) parses search queries with operators:

```
firstName:"Example"
firstName:"Example" tags:"AK UN"
institutions:"Institution 1" institutions:"Institution 2"
```

Supports:
- Field-specific search: `field:"value"`
- Wildcards: `name:"red%eur"`
- Conjungation of multiple searches: `field:"value" field2:"value"`
- Logical OR for values: `institutions:"Institution 1" institutions:"Institution 2"`

## Database Schema

Custom tables created by JPA entities:

- `iam_event`: Calendar events
- `iam_institution`: Institution data
- `iam_institution_mail`: Institutions' email addresses
- `iam_institution_operation_mode_change_mail_addresses`: Institutions' email addresses for operation mode changes
- `iam_institution_operation_mode_change_phone_numbers`: Institutions' phone numbers for operation mode changes
- `iam_institution_operation_mode_change_sms_phone_numbers`: `iam_institution_operation_mode_change_phone_numbers`: Institutions' phone SMS numbers for operation mode changes
- `iam_institution_phone`: Institutions' phone numbers
- `iam_institution_tag`: Institution tags
- `iam_institution_tags`: Mapping table of institution tags and to institutions
- `iam_institution_user`: Mapping table of users to institutions
- `iam_mail`: Email messages
- `iam_mail-type`: Email message types
- `iam_network`: Institution networks
- `iam_user_attributes`: Extended user attributes

## Tests

Arquilian and other native test tools do not allow to write tests for a Keycloak extension itself.
That's why we use Python unittests in a IMIS Development setup for unit testing the HTTP API of the Keycloak extension.

See `../../tests/README.md` for more details.

## License

GPL version 3.0 or later for all files except
`src/main/java/de/intevation/iam/util/SearchQueryUtils.java` which is under **Apache License 2.0**

See [LICENSES](../../LICENSES/) directory for complete license texts.
