# Permissions and Roles

This gives an overview of the roles and permission system used in IMIS3 Identity Management.

## Role Hierarchy

The system defines three roles with an inheritance hierarchy:

| Role           | Inherits | Description                                                |
|----------------|----------|------------------------------------------------------------|
| `user`         | -        | Base role for all authenticated users                      |
| `editor`       | `user`   | Can manage users and institutions within their own network |
| `chief_editor` | `editor` | Unrestricted access across all networks                    |

## Network-Based Access Control

Both users and institutions belong to a **network** (e.g. `"08"`, `"30"`).
Editors are restricted to operating within their own network.
Chief editors have no network restrictions.

## Permissions

### User Data

| Operation                            | User | Editor | Chief Editor |
|--------------------------------------|------|--------|--------------|
| View enabled, non-hidden users       | yes  | yes    | yes          |
| View disabled users (same network)   | no   | yes    | yes          |
| View disabled users (other network)  | no   | no     | yes          |
| View retired users                   | no   | no     | yes          |
| View hidden users                    | no   | no     | yes          |
| Update own profile                   | yes  | yes    | yes          |
| Update other user (same network)     | no   | yes    | yes          |
| Update other user (other network)    | no   | no     | yes          |
| Create user (same network)           | no   | yes    | yes          |
| Create user (other network)          | no   | no     | yes          |
| Set privileged user attributes       | no   | no     | yes          |

#### Privileged User Attributes

The following attributes can only be set or changed by a chief editor:

- `enabled`: account enabled/disabled status. Enabled means, the user can login via Keycloak.
- `retired`: retirement status. Retired users are also disabled.
- `role`: role assignment
- `network`: network membership (exception see below)
- `hiddenInAddressbook`: visibility in the address book

When an editor creates a user, the following defaults are enforced:
- `role` must be `user` (editors cannot assign `editor` or `chief_editor`)
- `network` must match the editor's own network
- Boolean privileged attributes must be `false` (user is disabled, not retired, not hidden)

### Institution Data

| Operation                              | User | Editor | Chief Editor |
|----------------------------------------|------|--------|--------------|
| View institutions                      | yes  | yes    | yes          |
| Create institution (same network)      | no   | yes    | yes          |
| Create institution (other network)     | no   | no     | yes          |
| Update institution (same network)      | no   | yes    | yes          |
| Update institution (other network)     | no   | no     | yes          |
| Change institution network             | no   | no     | yes          |
| Set privileged institution attributes  | no   | no     | yes          |

#### Privileged Institution Attributes

The following attributes can only be set or changed by a chief editor:

- `active`: whether the institution is active
- `measFacilId`: measurement facility ID
- `tags`: institution tags/categories

When an editor creates an institution, `active` is `false`, `measFacilId` is unset, and `tags` is empty.

### Mail

| Operation   | User | Editor | Chief Editor |
|-------------|------|--------|--------------|
| View mail   | yes  | yes    | yes          |
| Send mail   | no   | yes    | yes          |

### Events

| Operation      | User | Editor | Chief Editor |
|----------------|------|--------|--------------|
| View events    | yes  | yes    | yes          |
| Create event   | no   | no     | yes          |
| Update event   | no   | no     | yes          |
| Delete event   | no   | no     | yes          |

### Other endpoints

- Network: Every user can view the list of all networks
- Export: Same restrictions as for listing/viewing/searching users/institutions

## Modifying Permissions

Changes to the permission system require updates across multiple components.
After any change, rebuild the Keycloak container to take effect.
It is recommended to write unit tests to make sure the changed permissions work correctly in all scenarios.

### Adding/Renaming/Changing a Role

The checklist below covers adding a new role.
For other permission changes, not all steps will apply.

1. Add the role to the `IaMRole` enum in
   `keycloak/iam_spi/src/main/java/de/intevation/iam/auth/IaMRole.java`.
2. Add the role and its composite mappings in
   `docker/keycloak/data/realm.json`.
3. Update the authorizer classes (see below) to handle the new role.
4. If the role affects which profile fields are visible, also update
   `docker/keycloak/user_profile.json`.
5. If the client UI shows/hides elements based on role, update the relevant frontend components. Role state is managed in
   `client/src/stores/profile.js`.
   The most relevant UI components are:
   - `client/src/components/User/ManageUser.vue`
   - `client/src/components/Institution/ManageInstitution.vue`
   - `client/src/components/Search/searchTable.js` for column visibility
6. Add a test user with the new role in `docker/keycloak/data/realm.json`
   and `docker/keycloak/add_example_data.sql`, then register credentials
   in `tests/lib/auth.py` (`TEST_USERS` dictionary).
7. Add test cases covering the new role's permissions.

### Changing What a Role Can Do

Each entity type (user, institution, etc.) has an authorizer class that enforces permissions at the API level.
These are located in `keycloak/iam_spi/src/main/java/de/intevation/iam/auth/`.

All authorizers extend `Authorizer<T>` and implement a `doAuthorize(data, RequestMethod)`.
Role checks go through `IaMRole.isRoleOf(user, session)`, which also resolves hierarchical roles via Keycloak.

The individual authorizer classes:

- `UserAuthorizer.java`
- `InstitutionAuthorizer.java`
- `MailAuthorizer.java`
- `EventAuthorizer.java`

The resource classes (`UserResource.java`, `InstitutionResource.java`, etc.) can also enforce constraints beyond what the authorizers handle.
For example, `UserResource` forces retired users to be disabled and hides disabled users from unprivileged users.
When changing permissions, check both layers.

After changing an authorizer or resource, add or update the corresponding test cases in `tests/backend/test_*.py` and run the tests to verify.

### Changing Privileged Attributes

For both users and institutions, there is a list of privileged attributes in their respective authorizers.
These privileged attributes can only be modified by chief editors (enforced by `checkPrivilegedAttributes`).

When making an attribute non-privileged (i.e. allowing editors to set it), you may also need to update `user_profile.json` to prevent users from editing it through their own profile.
Otherwise users could change the attribute on themselves via the Keycloak account UI.

For example, commit `4fcc69ad` ("Allow editors to edit tags") made user `tags` editable by editors.

### Changing User Profile Field Visibility

Which roles can see or edit which profile fields in the Keycloak UI is configured in
`docker/keycloak/user_profile.json`.
