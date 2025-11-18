# IAM Frontend Application

The frontend application provides a user interface for the Keycloak IAM interfaces providing the following capabilities:
- to all users:
    - to see and change their own data
    - to see data of other users
    - to view all institution's data
    - export data as CSV
- to editors additionally:
    - to change user data in their institution, except chiefeditors
    - to create new institutions
    - to edit their institution's data
- to chiefeditors:
    - to change user data in all institutions
    - to edit all institution's data

The Client application is a Vue 3 Single Page Application (SPA) with Vuetify Material Design components.

#### Key Features
- **Advanced Search**: User and institution search with (multi-value) filtering, sorting, and pagination
- **User Management**: Create, edit, and delete users
- **Institution Management**: Manage institutions with tags, addresses, and contact information
- **Event Calendar**: Calendar interface for event management (currently disabled)
- **Mailing Lists**: Email archive and mailing list management (currently disabled)
- **Data Export**: CSV export of user and institution data


## Build and Usage

Install the dependencies:
```shell
yarn install
```

Compiles and hot-reloads for development:
```shell
yarn dev
```

Compiles and minifies for production:
```shell
yarn build
```

Lints and fixes files:
```shell
yarn lint
```

### Run the tests
```
yarn test
```
See also `client/src/tests/README.md` and `tests/README.md`.

### Vuejs Custom Configuration
See [Configuration Reference](https://cli.vuejs.org/config/).

## Authentication Flow
1. The User accesses the application via Apache httpd with `mod_auth_openidc`
2. Unauthenticated users are redirected to Keycloak login
3. Keycloak validates credentials
4. The User receives the OpenID Connect token
5. Token is included in all subsequent API requests

## Directory Structure

```
client/
├── public/                  # Static assets
│   └── favicon.ico
├── src/                     # Application source code
│   ├── components/          # Vue components organized by feature
│   │   ├── Calendar/        # Event calendar components
│   │   │   ├── Calendar.vue
│   │   │   ├── ManageEvent.vue
│   │   │   └── event.js
│   │   ├── Form/            # Reusable form field components
│   │   │   ├── Checkbox.vue
│   │   │   ├── ChipTextField.vue
│   │   │   ├── Combobox.vue
│   │   │   ├── DatePicker.vue
│   │   │   ├── GenericUserFormField.vue
│   │   │   ├── Select.vue
│   │   │   ├── Textarea.vue
│   │   │   └── TextField.vue
│   │   ├── Institution/     # Institution management components
│   │   │   ├── InstitutionTable.vue
│   │   │   ├── ManageInstitution.vue
│   │   │   └── institution.js
│   │   ├── Mailing/         # Mailing lists and email archive
│   │   │   ├── Archive.vue
│   │   │   ├── MailContent.vue
│   │   │   ├── MailDialog.vue
│   │   │   └── MailingLists.vue
│   │   ├── Search/          # Search, filter, and results components
│   │   │   ├── ColumnSelection.vue
│   │   │   ├── Filter.vue
│   │   │   ├── Results.vue
│   │   │   ├── ResultTable.vue
│   │   │   ├── Search.vue
│   │   │   └── searchTable.js
│   │   ├── UI/              # UI layout and utility components
│   │   │   ├── Appbar.vue
│   │   │   ├── Appfooter.vue
│   │   │   ├── EditTags.vue
│   │   │   ├── ExportDialog.vue
│   │   │   ├── SessionExpiredDialog.vue
│   │   │   ├── UIAlert.vue
│   │   │   ├── UIHeader.vue
│   │   │   └── UITooltip.vue
│   │   ├── User/            # User management components
│   │   │   ├── ManageUser.vue
│   │   │   ├── UserTable.vue
│   │   │   └── user.js
│   │   ├── ApplicationComponent.vue
│   │   ├── ConfirmCancelDialog.vue
│   │   ├── DataTableServer.vue
│   │   ├── InfoDialog.vue
│   │   ├── Main.vue
│   │   └── Notification.vue
│   ├── i18n/                # Internationalization configuration
│   │   └── index.js
│   ├── lib/                 # Utilities and composables
│   │   ├── http.js          # Axios HTTP client configuration
│   │   ├── use-form.js      # Form handling composable
│   │   └── use-notification.js  # Notification composable
│   ├── locales/             # Translation files
│   │   └── de.js            # German translations
│   ├── plugins/             # Vue plugins
│   │   └── vuetify.js       # Vuetify Material Design setup
│   ├── router/              # Vue Router configuration
│   │   └── index.js
│   ├── stores/              # Pinia state management stores
│   │   ├── application.js   # Global app state and dialogs
│   │   ├── coordinates.js   # Geographic coordinates
│   │   ├── events.js        # Event management state
│   │   ├── institution.js   # Institution list and search
│   │   ├── mail.js          # Mailing lists state
│   │   ├── profile.js       # Current user profile and permissions
│   │   └── user.js          # User list and search state
│   ├── test/                # Test files and utilities
│   │   ├── components/
│   │   ├── lib/
│   │   ├── setup.js
│   │   └── sharedTests.js
│   ├── App.vue              # Root Vue component
│   └── main.js              # Application entry point
├── eslint.config.js         # ESLint configuration
├── index.html               # HTML entry point
├── jsconfig.json            # JavaScript configuration
├── package.json             # Dependencies and scripts
├── vite.config.mjs          # Vite build configuration
├── vitest.config.js         # Vitest test configuration
└── yarn.lock                # Dependency lock file
```

## License

GPL version 3.0 or later.

See [LICENSES](../../LICENSES/) directory for complete license text.
