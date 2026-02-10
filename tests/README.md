# Keycloak iam_spi API Testing Suite

Python testing framework for the Keycloak iam_spi custom extension API endpoints.

## Overview

This testing suite provides automated tests for all iam_spi REST API endpoints including:
- User management
- Institution management
- Mail/messaging
- Event management
- Network management
- CSV export functionality
- Role-based permission testing
- Integration testing

Client (UI) tests are located in `client/src/tests`.

## Prerequisites

- Python 3.8 or higher
- pip
- A running instance of this software in development mode (see docker/README.md)

## Dependencies

```bash
cd tests
pip install -r requirements.txt
```

- Firefox browser installed
- GeckoDriver (automatically managed by webdriver-manager)

## Running Tests

### Quick Start

```bash
cd docker
docker compose --env-file dev.env -f docker-compose.yml -f docker-compose.dev.yml down --volumes
docker compose --env-file dev.env -f docker-compose.yml -f docker-compose.dev.yml up -d
cd tests
./run_tests.py
# or
pytest
```

### Run Specific Test Suites

Some examples:

```bash
./run_tests.py user              # Run user API tests only
./run_tests.py backend           # Run all backend API tests
./run_tests.py frontend          # Run frontend tests only
./run_tests.py institution       # Run institution tests
./run_tests.py mail event        # Run mail and event tests
./run_tests.py --verbose         # Run with verbose output
```

For a full list of available tests selectors, see the output of the built-in help:
```bash
./run_tests.py --help
```

## Configuration Options

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `CLIENT_URL` | Client base URL | `http://localhost:48081` |
| `KEYCLOAK_URL` | Keycloak base URL | `http://localhost:48080` |
| `REALM` | Keycloak realm name | `imis3` |
| `CLIENT_ID` | OAuth2 client ID | `iam-client` |
| `CLIENT_SECRET` | OAuth2 client secret | `exampleclientsecret` |
| `REQUEST_TIMEOUT` | API request timeout (seconds) | `30` |
| `DB_HOST` | Database hostname | `localhost` |
| `DB_PORT` | Database port | `2345` |
| `DB_USER` | Database username | `keycloak` |
| `DB_PASSWORD` | Database password | `secret` |
| `DB_NAME` | Database name | `keycloak` |
| `SELENIUM_HEADLESS` | Run Selenium Headless | `true` |

For example, these options allow running the tests on a different machine than the one hosting the application.
This is particularly useful when the development instance is a remote VM, reachable via SSH, with its interfaces accessible through SSH SOCKS Proxies and Local Forwards.
Running the tests locally allows executing integration tests in the foreground with immediate visibility into test execution.

SSH Config:
```ini
Host imis.example.com
    LocalForward localhost:48080 localhost:48080 # BfS IMIS
    LocalForward localhost:48081 localhost:48081 # BfS IMIS
    LocalForward localhost:2345 localhost:2345 # BfS IMIS Postgres
```

And `/etc/hosts`:
```
127.0.0.1	imis.example.com
```

```bash
SELENIUM_HEADLESS=false KEYCLOAK_URL=http://imis.example.com:48080 CLIENT_URL=http://imis.example.com:48081 ./tests/run_tests.py
```

As an alternative to the hosts and SSH configuration, `proxychains` could be used for a transparent proxy approach.

## Manually use helper functions to delete a user/institution from the database

During development or testing, extraneous data may accumulate in the database.
Rather than recreating all containers from scratch, deleting the specific records is often easier and faster.
As deleting users and institutions is intentionally not possible via the user interfaces of IMIS and Keycloak, these helper functions provide a convenient alternative.

**Warning:** While these functions can technically be used in production environments, doing so is strongly discouraged as it circumvents the intended delete constraints.

```bash
python3 -i lib/db_helpers.py
delete_user_from_db('6b19e3e3-c11e-4308-8f27-8f090d3b5fd5')
delete_institution_from_db(4)
delete_institution_from_db(institution_facil_name='test_5owza6aa')
```

## CI/CD Integration

### GitLab CI Example

```yaml
test:
  stage: test
  image: python:3.11
  before_script:
    - cd tests
    - pip install -r requirements.txt
  script:
    - ./run_tests.py
```

### GitHub Actions Example

```yaml
name: API Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-python@v2
        with:
          python-version: '3.11'
      - name: Install dependencies
        run: |
          cd tests
          pip install -r requirements.txt
      - name: Run tests
        run: |
          cd tests
          ./run_tests.py
```
