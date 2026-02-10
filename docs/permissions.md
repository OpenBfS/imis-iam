# Permissions and Roles

This gives an overview on the roles and permission system used in IMIS3 Identity Management.

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
