# IAM Keycloak Theme

Custom Keycloak theme for the IMIS3 Identity Management system, providing branded login pages and email templates.

## Components

### Login Theme (`login/`)

Customizes the authentication and login interface

- **Templates**: FreeMarker (`.ftl`) templates for login pages
- **Styles**: Custom CSS for branding
- **Localization**: Message bundles (German and English)
- **Resources**: Images, CSS files, and other assets

### Email Theme (`email/`)

Customizes email notifications sent by Keycloak:

- **HTML Templates**
- **Text Templates**: Plain-text fallback versions
- **Localization**: Message bundles (only German)
- **Custom Templates**: Account expiration, reminders, account status notifications

## Directory Structure

```
iam_theme/
├── login/                          # Login theme
│   ├── login.ftl                   # Main login page template
│   ├── template_base.ftl           # Base template with header/footer
│   ├── theme.properties            # Theme configuration
│   ├── messages/                   # Localization files
│   └── resources/                  # Static resources: Styles and favicon
└── email/                          # Email theme
    ├── theme.properties            # Theme metadata
    ├── html/                       # HTML email templates
    ├── text/                       # Plain-text email templates
    └── messages/                   # Localization files
```

## Customization

### Login Theme Configuration (`theme.properties`)

In this file various parameters are set, defining the default values of custom URLs and branding.

### Security Considerations

For XSS Prevention use `${kcSanitize(variable)?no_esc}` to render user-provided content.

### Changing Brand Colors

Edit `login/resources/css/iam.css`, section `:root`.

### Adding New Email Templates

1. Create new FreeMarker templates in `email/html/` and `email/text/`
2. Add translations to `email/messages/messages_de.properties`
3. Reference from IAM SPI code in `../iam_spi/src/main/java/de/intevation/iam/mail/IamMailTemplateProvider.java`.

## Internationalization

### Adding a New Language

1. Create `login/messages/messages_{locale}.properties`
2. Create `email/messages/messages_{locale}.properties`
3. Add locale to `theme.properties`:
   ```properties
   locales=de,en,fr
   ```
4. Restart Keycloak

## License

GPL version 3.0 or later.

See [LICENSES](../../LICENSES/) directory for complete license text.
