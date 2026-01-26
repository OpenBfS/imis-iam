# User Attributes

## Keycloak User Attributes vs IAM User Attributes

The IMIS3 Identity Management system uses two different types of user attributes that serve different purposes and are stored in different locations:

| Aspect | Keycloak Attributes | IAM User Attributes |
|--------|---------------------|---------------------|
| Configuration | `user_profile.json` | `UserAttributes.java` (JPA) |
| Storage | Keycloak's `user_attribute` table | Custom `iam_user_attributes` table |
| Validation | Keycloak User Profile | Custom Java code |
| User editable | Based on Keycloak permissions | Custom (admin/editor only) in `UserAuthorizer.java` |
| Multi-valued | Supported natively (all values are lists) | Requires custom handling |
| In API responses | `user.attributes` map | Top-level fields (`user.network`, etc.) |
| Adding new | Edit JSON + deploy | Code changes + DB migration |

### Keycloak User Attributes

- Personal user data that users can view/edit themselves
- Data is validated by Keycloak's built-in validation (email format, phone patterns, etc.)
- Multi-valued attributes (like tags or positions)
- Data is available in Keycloak's account and admin consoles

**Examples:**
- core Keycloak fields: `username`, `email`, `firstName`, `lastName`
- contact and profile information: `phone`, `mobile`, `fax`, `title`, `position`, `tags`
- private notification settings: `operationModeChangeMailAddresses`, `operationModeChangePhoneNumbers`

### IAM User Attributes (Custom Extension)

- Administrative data that users cannot edit themselves
- Privileged flags that only editors/chief editors can modify
- Allow complex relationships (like institution assignments) and dependencies between attributes (like retired and enabled)
- Can affects user visibility and access control
- Include system-managed fields (notification flags, expiry dates)

**Examples:**
- IAM specific attributes with special relations and meaning: `network`, `institutions`(stored in database table `iam_institution_user`)
- Administrative flags: `hiddenInAddressbook`, `retired`
- Account lifecycle fields: `expiryDate`, `inactivityNotificationSent`, `expiredNotificationSent`

## Adding or Changing IAM User Attributes

This document describes the steps required to add a new user attribute to the IMIS3 Identity Management system. It is based on the implementation of the `hiddenInAddressbook` and `retired` attributes.

This guide covers adding **IAM User Attributes**.
For Keycloak User Attributes, see the Keycloak documentation on User Profile configuration for backend changes, and apply the frontend part of this guide where applicable.

## Guide

### Database Migration

**Example commits:**
- `81ce0ba` for `hiddenInAddressbook`
- `a01876a` for `retired`

Add a new column to the `iam_user_attributes` table in `src/main/resources/META-INF/db-changelog.xml`:

```xml
<changeSet author="yourname@example.com" id="vX.Y">
    <sql dbms="all"
         splitStatements="true"
         stripComments="true">
       ALTER TABLE iam_user_attributes
         ADD COLUMN your_attribute_name BOOLEAN DEFAULT false;
    </sql>
</changeSet>
```

**Notes:**
- Use snake_case for column names (e.g., `hidden_in_addressbook`)
- Set default values
- Increment the version number appropriately

### JPA Entity (UserAttributes.java)

**Example commits:**
- `56daa35` for `hidden_in_addressbook`
- `56a42b4` for `retired`

Add the field and accessor methods in `src/main/java/de/intevation/iam/model/jpa/UserAttributes.java`:

```java
@Column(name = "your_attribute_name", nullable = false)
private boolean yourAttributeName;

public boolean getYourAttributeName() {
    return yourAttributeName;
}

public void setYourAttributeName(boolean yourAttributeName) {
    this.yourAttributeName = yourAttributeName;
}
```

**Best practices (from commit `a01876a`):**
- For boolean fields that should never be null, use the primitive `boolean`. Otherwise use the of `Boolean` class
- Use `is` prefix for boolean getters: `isYourAttributeName()` instead of `getYourAttributeName()`
- Ensure the database column is set to `NOT NULL` if using primitive types

### User Representation

Add the field to the API representation in `src/main/java/de/intevation/iam/model/representation/User.java`.

**Reference commits:**
- `81ce0ba` for `hiddenInAddressbook`
- `a01876a` for `retired`

#### Add the field default value

```java
private boolean yourAttributeName = false;
```

#### Read from JPA model in constructor

In the constructor that takes `UserModel`, reference the getter:

```java
if (jpaModel != null) {
    // ... existing code ...
    this.yourAttributeName = jpaModel.getYourAttributeName();
}
```

#### Add getter and setter

```java
public boolean isYourAttributeName() {
    return yourAttributeName;
}
public void setYourAttributeName(boolean yourAttributeName) {
    this.yourAttributeName = yourAttributeName;
}
```

#### Update `createOrUpdateJpaModel()` method

Add the field mapping:

```java
jpaUser.setYourAttributeName(yourAttributeName);
return jpaUser;
```

### User Authorization (UserAuthorizer.java)

If the attribute should only be modifiable by certain roles (e.g., chief editors), update `src/main/java/de/intevation/iam/auth/UserAuthorizer.java`.

**Example commits:**
- `ceadd09` for `hiddenInAddressbook`
- `56a42b4` for `retired`

#### Add constant for the attribute key

```java
private static final String YOUR_ATTRIBUTE_KEY = "yourAttributeName";
```

#### Add to privileged attributes list (if applicable)

Add the attribute key to `PRIVILEGED_ATTR`.

#### Add visibility rules (if applicable)

If the attribute affects user visibility, add checks in the `checkVisible` method.


### User Resource (UserResource.java)

Update `src/main/java/de/intevation/iam/UserResource.java` for filtering in the output and searches.

**Reference commits:**
- `91bcb72` for `hiddenInAddressbook`
- `56a42b4` and `7ec8cba` for `retired`

#### Add to filter attributes

In the user list/search method, add the attribute to the filterable attributes.
Refer to the logic building the `customAttributes` HashMap.

#### Add filter logic

If applicable, extend the logic of `customAttributesFilter`.

### User State (user.js)

**Example commits:**
- `ceadd09` for `hiddenInAddressbook`
- `56a42b4` for `retired`

Add the attribute to the expected user object in `client/src/components/User/user.js`:

```javascript
const expUser = {
  // ... existing fields ...
  yourAttributeName: false,
};
```

### User Table (UserTable.vue)

**Example commits:**
- `ceadd09` for `hiddenInAddressbook`
- `56a42b4` for `retired`

Add the column to the table headers in `client/src/components/User/UserTable.vue`:

```javascript
const columns = ...
      // ... existing columns ...
      { name: "yourAttributeName", default: false },
```

### Data Table Server (DataTableServer.vue)

**Example commits:**
- `ceadd09` for `hiddenInAddressbook`
- `56a42b4` for `retired`

Add filter support in `client/src/components/DataTableServer.vue`:

```html
<template v-else-if="header.key === 'yourAttributeName'">
  <Filter
    :filterKey="header.key"
    :items="booleanFilterItems"
    :label="$t('user.yourAttributeName')"
    :type="props.type"
  ></Filter>
</template>
```

### Manage User Form (ManageUser.vue)

**Example commits:**
- `ceadd09` for `hiddenInAddressbook`
- `56a42b4` for `retired`

Add an appropriate input element to the user edit form in `client/src/components/User/ManageUser.vue`.

Optionally add watchers for interdependent fields.

### Frontend: Search/Filter Components

Update search-related components if needed.

**Example commits:**
- `56a42b4` for `retired`

#### Filter.vue

If the filter should be single-select (not multiple), add to the exclusion list in `client/src/components/Search/Filter.vue`:

```javascript
const multiple = computed(() => ...
    !["enabled", "hiddenInAddressbook", "role", "yourAttributeName"]
    ...
  );
```

#### ColumnSelection.vue

Add to boolean column handling in `client/src/components/Search/ColumnSelection.vue`:

```javascript
} else if (["active", "enabled", "yourAttributeName"].includes(key)) {
  return t(`${value}`);
}
```

#### searchTable.js

If the column should only be visible to certain roles, update the method `createHeaders` accordingly.

### Localization

Add translations in `client/src/locales/de.js`.

**Example commits:**
- `ceadd09` for `hiddenInAddressbook`
- `56a42b4` for `retired`


### Tests

Add tests for the new attribute in `tests/backend/test_user_api.py`.

**Example commits:**
- `212294d` for `hiddenInAddressbook`
- `56a42b4` for `retired`

Also consider adding frontend and/or integration tests if the input forms and behaviour received relvant changes.
