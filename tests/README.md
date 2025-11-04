# Keycloak iam_spi API Testing Suite

Python testing framework for the Keycloak iam_spi custom extension API endpoints.

## Overview

This testing suite provides automated tests for all iam_spi REST API endpoints including:
- User management
- Institution management
- Mail/messaging
- Event management
- CSV export functionality
- Role-based permission testing

## Prerequisites

- Python 3.8 or higher
- pip
- A running instance of this software in development mode (see docker/README.md)

## Installation

### Dependencies

```bash
cd tests
pip install -r requirements.txt
```

This will install:
- `requests` - HTTP library
- `pytest` - Testing framework

### Environment

Set environment variables or create a `.env` file:

```bash
export KEYCLOAK_URL="http://localhost:48080"
export REALM="imis3"
export CLIENT_ID="iam-client"
export CLIENT_SECRET="exampleclientsecret"
```

Or create a `tests/.env` file:
```ini
KEYCLOAK_URL=http://localhost:48080
REALM=imis3
CLIENT_ID=iam-client
CLIENT_SECRET=exampleclientsecret
```

## Running Tests

### Quick Start

```bash
cd docker
docker compose --env-file dev.env -f docker-compose.yml -f docker-compose.dev.yml down --volumes
docker compose --env-file dev.env -f docker-compose.yml -f docker-compose.dev.yml up -d
cd tests
./run_tests.py
```

### Run Specific Test Suites

```bash
./run_tests.py user              # Run user API tests only
./run_tests.py institution       # Run institution tests
./run_tests.py mail event        # Run mail and event tests
./run_tests.py --verbose         # Run with verbose output
```

### Run with pytest Directly

```bash
# Run all tests
pytest

# Run specific test file
pytest test_user_api.py

# Run tests matching pattern
pytest -k "test_list"

# Run with verbose output
pytest -v

# Run with coverage
pytest --cov=lib --cov-report=html
```

### Run Specific Tests

```bash
# Run a specific test class
pytest test_user_api.py::TestUserAPI

# Run a specific test method
pytest test_user_api.py::TestUserAPI::test_list_users

# Run tests matching keyword
pytest -k "role_permission"
```

## Configuration Options

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `KEYCLOAK_URL` | Keycloak base URL | `http://localhost:48080` |
| `REALM` | Keycloak realm name | `imis3` |
| `CLIENT_ID` | OAuth2 client ID | `iam-client` |
| `CLIENT_SECRET` | OAuth2 client secret | `exampleclientsecret` |
| `REQUEST_TIMEOUT` | API request timeout (seconds) | `30` |

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
  artifacts:
    reports:
      junit: tests/report.xml
    paths:
      - tests/htmlcov/
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
