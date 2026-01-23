# Data Filtering Processing

This file describes the data flow when filtering by one or more data fields in the client.

When a user applies filters, the steps are:

1. User Input
2. Filter Component
3. Store Update
4. API Request
5. Backend: Query Parsing
6. Database Query
7. Response to client
8. Render result in table

## Client (Vue)

### UI Components

| File (`src/components/`) | Purpose | Relevant Methods |
|------|---------|-------------|
| [Filter.vue](../client/src/components/Search/Filter.vue) | Filter input component | `handleFilterInput`, `triggerSearch` |
| [DataTableServer.vue](../client/src/components/DataTableServer.vue) | Renders filter headers per column | `updateTable` |
| [ColumnSelection.vue](../client/src/components/Search/ColumnSelection.vue) | Column visibility | `updateSelectedColumns` |
| [UserTable.vue](../client/src/components/User/UserTable.vue) | User data table | `template` |
| [InstitutionTable.vue](../client/src/components/Institution/InstitutionTable.vue) | Institution data table | `template` |
| [searchTable.js](../client/src/components/Search/searchTable.js) | Filter and table header helpers | `getFilters`, `getFilterValue`, `createHeaders` |

### Query and State

| File (`src/`) | Purpose | Relevant Methods |
|------|-----------------|-------------|
| [stores/user.js](../client/src/stores/user.js) | User state store with `filterBy` object | `updateFilter`, `loadUsers` |
| [stores/institution.js](../client/src/stores/institution.js) | Institution state store with `filterBy` object | `updateFilter`, `loadInstitutions` |
| [stores/application.js](../client/src/stores/application.js) | Search requests and loading state | `searchRequest` |
| [lib/http.js](../client/src/lib/http.js) | HTTP client and query string builder | `createSearchQueryString` |

### Client Data Flow

1. User types in Filter.vue
2. `handleFilterInput()` called
3. `store.updateFilter(filterKey, value)` updates `filterBy` state
4. Debounced (500ms) `triggerSearch()` calls `applicationStore.searchRequest()`
5. `loadUsers()` or `loadInstitutions()` builds params via `createSearchQueryString()`
6. HTTP GET request: `/backend/realms/imis3/iam/user?search=firstName:"Example"&firstResult=0&maxResults=25`


## Backend (Java/Keycloak SPI)

### REST Resources (Controllers)

| File (`iam/`) | Endpoint | Purpose | Method |
|------|----------|---------|--------|
| [UserResource.java](../keycloak/iam_spi/src/main/java/de/intevation/iam/UserResource.java) | `GET /iam/user` | List and filter users | `getUsers` |
| [InstitutionResource.java](../keycloak/iam_spi/src/main/java/de/intevation/iam/InstitutionResource.java) | `GET /iam/institution` | List and filter institutions | `getInstitutions` |
| [ExportResource.java](../keycloak/iam_spi/src/main/java/de/intevation/iam/ExportResource.java) | `GET /iam/export/*` | Export (filtered) data | `exportUsers`, `exportInstitutions` |

### Query Parsing

| File (`iam/util/`) | Purpose | Method |
|------|---------|--------|
| [SearchQueryUtils.java](../keycloak/iam_spi/src/main/java/de/intevation/iam/util/SearchQueryUtils.java) | Parses search query strings into `Map`s | `getFields` |
| [SortUtil.java](../keycloak/iam_spi/src/main/java/de/intevation/iam/util/SortUtil.java) | In-memory sorting for user results | `sortByField` |

Both these utility components are necessary for sorting and filtering in searches with multiple attribute values (thus self-implemented and in-memory to minimize overlap with potentially colliding changes in keycloak code).

### Authorization

| File (`iam/auth/`) | Purpose | Method |
|------|---------|--------|
| [Authorizer.java](../keycloak/iam_spi/src/main/java/de/intevation/iam/auth/Authorizer.java) | Base authorization class | |
| [UserAuthorizer.java](../keycloak/iam_spi/src/main/java/de/intevation/iam/auth/UserAuthorizer.java) | Filter users by requester permissions | `checkVisible` |
| [InstitutionAuthorizer.java](../keycloak/iam_spi/src/main/java/de/intevation/iam/auth/InstitutionAuthorizer.java) | Filter institutions by requester permissions | `doAuthorize` |

### JPA Entities and Models

| File (`iam/model/`) | Purpose |
|------|---------|
| [jpa/Institution.java](../keycloak/iam_spi/src/main/java/de/intevation/iam/model/jpa/Institution.java) | Institution JPA entity |
| [representation/User.java](../keycloak/iam_spi/src/main/java/de/intevation/iam/model/representation/User.java) | User representation |
| [jpa/UserAttributes.java](../keycloak/iam_spi/src/main/java/de/intevation/iam/model/jpa/UserAttributes.java) | User Attributes representation |
| [representation/ObjectList.java](../keycloak/iam_spi/src/main/java/de/intevation/iam/model/representation/ObjectList.java) | Response type |

### Backend Data Flow

1. HTTP Request arrives: `GET /backend/realms/imis3/iam/user?search=firstName:"Example"` or `GET /backend/realms/imis3/iam/institution?search=name:"Example"`
2. `UserResource.getUsers()` or `InstitutionResource.getInstitutions` called
3. `SearchQueryUtils.getFields(search)` parses into `Map<String, List<String>>`
4. For Users:
   - Keycloak `searchForUserStream()` with attributes
   - Post-filter select list attributes
   - Post-filter custom attributes
   - `SortUtil` for sorting
   - `Authorizer.filter()` removes unauthorized items

   For Institutions:
   - JPA Criteria API query building: predicates, joins, etc.
   - Database query executed
5. `ObjectList` response returned with total size and paginated results

## Example Filter Scenario

**User filters by `firstName: "Example"` and `institutions: "Test Institution"`**

### Client Side:
1. `Filter.vue` captures input
2. `userStore.filterBy` becomes: `{ firstName: "Example", institutions: "Test Institution" }`
3. `createSearchQueryString()` builds: `firstName:"Example" institutions:"Test Institution"`
4. Request: `GET /backend/realms/imis3/iam/user?search=firstName:"Example" institutions:"Test Institution"&firstResult=0&maxResults=25`

### Backend Side:
1. `UserResource.getUsers()` receives request
2. `SearchQueryUtils.getFields()` parses to: `{ "firstName": ["Example"], "institutions": ["Test Institution"] }` (as `Map<String, List<String>>`)
3. Keycloak search for `firstName` containing "Example"
4. Post-filter: exact match on `institutions` attribute
5. Authorization filter applied
6. Results sorted and paginated
7. Response: `{ "size": 5, "list": [...] }`
