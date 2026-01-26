# Client tests

Vue testing framework for the Vue client to the Keycloak iam_spi custom extension API endpoints.

## Overview

This testing suite provides automated tests for UI elements and components:
- Forms: the `ChipTextField` component
- User management
- Basic Institution management

Backend and integration tests are located in `tests/`.

## Prerequisites

- Vue
- Vite

## Running Tests

In a development environment:
```shell
yarn test
```

Execute using docker:
```shell
docker exec -it docker-client-1 yarn test
```

## CI/CD Integration

### GitLab CI Example

```yaml
test:
  stage: test
  image: node:18
  before_script:
    - cd client
    - yarn install
  script:
    - yarn test
```

### GitHub Actions Example

```yaml
name: Client Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-node@v6
        with:
          node-version: 18
      - name: Install dependencies
        run: |
          cd client
          npm install --global yarn
          yarn install
      - name: Run tests
        run: |
          cd client
          yarn test
```
