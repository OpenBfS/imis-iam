# Changelog

## 1.0.5 - 15.12.2025

### Changed

- Hide 'BAW-Kontaktdaten', only owner can see his contact-details [#6146] (cklassen, tgottfried)
- Update to KC version 26.4.6 [#6277] (swagner)


### Added

- Make timestamp readable in the DB. [#6163] (swagner)

## 1.0.4 - 27.10.2025

### Changed

- Upgrade keycloak to version 26.3.4. [#6250] (swagner)
- Apply multi-stage-build to addressbook and keycloak. [#5965] (pschwabauer, ffuhlbrueck)
- Highlight tabs and column headers. [#6064] (cklassen)
- Make session cookie name of addressbook configurable. [#6224] (ffuhlbrueck)
- Make institution category mandatory. [#6236] (swagner, cklassen)

### Added

- Show filtered state with icon in column headers. [#5931] (cklassen)
- Make users sortable. [#6062] (cklassen, pschwabauer)
- Implement search for multiple OR-combined values within one user attribute. [#5891] (pschwabauer, cklassen, tgottfried)

### Fixed

- Fix wrong result upon running filter operations in quick succession. [#6225] (cklassen)
- Fix problem with bulk (un)assignment of mailing lists. [#6229] (cklassen)
- Fix handling of special characters in institution names and other attributes. [#6140] (cklassen)
- Add warning for duplicate institution addresses [#5650] (cklassen)
- Fix imprecise search in mailing lists. [#6232] (tgottfried)


## 1.0.3 - 27.08.2025

### Changed

- Change icon for export button. [#6170] (swagner)

### Added

- Add toggle button to (de)select all columns. [#6198] (cklassen)
- Add version info. [#6187] (cklassen)

### Fixed

- Fix search results for institution categories. [#6081] (pschwabauer)
- Improve handling of stop signal in client container. [#6017] (swagner)
- Fix sorting of empty entries in multi-value fields. [#6071] (pschwabauer)
- Fix export with default columns setting. [#6197] (cklassen)
- Fix display error for institutions with empty serviceBuildingState. [#6214] (cklassen)
- Fix sorting of institutions. [#6207] (cklassen)


## 1.0.2 - 2025-07-28

### Changed

- Make timeout of OIDC session inactivity configurable. [#6135] (swagner, ffuhlbrueck)
- Allow selection of multiple values for attribute "position". [#6142] (pschwabauer)

### Added

- Add password change option to app bar. [#6141] (swagner)
- Introduce attribute to hide users in addressbook. [#6144] (ffuhlbrueck, pschwabauer)

### Fixed

- Fix loss of data entry in multi-value form fields (ChipTextFields) upon save. [#6130] (swagner)
- Fix filter for attribute "network". [#6122] (pschwabauer)
- Fix translation of federal states. [#6158] (cklassen)


## 1.0.1 - 2025-06-24

### Changed

- Customize theme and text. (mfabry)

### Added

- Enable sorting of institution categories. [#6071] (pschwabauer)

### Fixed

- Remove unnecessary newlines from csv export. [#6088] (swagner)


## 1.0.0 - 2025-05-27
