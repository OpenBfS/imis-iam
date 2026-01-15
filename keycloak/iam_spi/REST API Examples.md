# REST API Examples

## Authenticate

```bash
access_token=$(curl --silent -d client_id=iam-client -d client_secret=exampleclientsecret \
  -d grant_type=password -d username=exampleuser -d password=secret \
  http://localhost:48081/realms/imis3/protocol/openid-connect/token | jq -r '.access_token')
```

## User Management Examples

### Get Current User Profile

Get the profile data of the currently authenticated user:

```bash
curl --fail -H "Authorization: Bearer $access_token" \
  http://localhost:48081/realms/imis3/iam/user/profile | jq .
```

Example response:
```json
{
  "attributes": {
    "firstName": ["Example"],
    "lastName": ["User"],
    "email": ["example@user.com"],
    "username": ["exampleuser"]
    ...
  }
}
```

### List Users with Pagination

Get a paginated list of users:

```bash
curl --fail -H "Authorization: Bearer $access_token" \
  "http://localhost:48081/realms/imis3/iam/user?firstResult=0&maxResults=10" | jq .
```

Example response:
```json
{
  "size": 3,
  "list": [
    {
      "id": "4cf05905-f84a-4052-8525-61676731f205",
      "attributes": {
        "lastName": [
          "Redakteur"
        ],
        ...
      }
    ...
    }
  ]
}
```

### Search Users

Search for users by specific attributes (here: the last name):

```bash
curl --fail -H "Authorization: Bearer $access_token" \
  "http://localhost:48081/realms/imis3/iam/user?search=lastName:user | jq .
```

Search with multiple criteria:

```bash
curl --fail -H "Authorization: Bearer $access_token" \
  "http://localhost:48081/realms/imis3/iam/user?search=lastName:user%20firstName:example" | jq .
```

### Get User by ID

```bash
user_id="95454150-9e8b-45df-8943-bb9e81968357"
curl --fail -H "Authorization: Bearer $access_token" \
  "http://localhost:48081/realms/imis3/iam/user/${user_id}" | jq .
```

### Create a New User

This requires the access token of a chief editor.

```bash
curl --fail -H "Authorization: Bearer $access_token" \
  -H "Content-Type: application/json" \
  -d '{
    "attributes": {
        "username": ["newuser"],
        "email": ["newuser@example.com"],
        "firstName": ["New"],
        "lastName": ["User"],
        "position": ["Mitarbeitende"]
    },
    "institutions": ["Institution 2"],
    "role": "user",
    "network": "08",
    "enabled": true
  }' \
  http://localhost:48081/realms/imis3/iam/user | jq .
```

### Update User

This requires the access token of an editor in the same network or chief editor.

```bash
curl -X PUT --fail -H "Authorization: Bearer $access_token" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "4fc98dd9-8d2d-4a7f-9795-9c1410689af8",
    "attributes": {
        "username": ["newuser"],
        "email": ["newuser@example.com"],
        "firstName": ["New2"],
        "lastName": ["User"],
        "position": ["Mitarbeitende"]
    },
    "institutions": ["Institution 2"],
    "role": "user",
    "network": "08",
    "enabled": true
  }' \
  http://localhost:48081/realms/imis3/iam/user/ | jq .
```

### Update User Profile

```bash
curl -X PUT --fail -H "Authorization: Bearer $access_token" \
  -H "Content-Type: application/json" \
  -d '{
  "attributes": {
    ...
    }
  }' \
  http://localhost:48081/realms/imis3/iam/user/profile | jq .
```

### Get Available Roles

```bash
curl --fail -H "Authorization: Bearer $access_token" \
  http://localhost:48081/realms/imis3/iam/user/roles | jq .
```

## Institution Management Examples

### List Institutions

```bash
curl --fail -H "Authorization: Bearer $access_token" \
  "http://localhost:48081/realms/imis3/iam/institution?maxResults=10" | jq .
```

### Get Institution by ID

```bash
institution_id="1"
curl --fail -H "Authorization: Bearer $access_token" \
  "http://localhost:48081/realms/imis3/iam/institution/${institution_id}" | jq .
```

### Search Institutions

```bash
curl --fail -H "Authorization: Bearer $access_token" \
  "http://localhost:48081/realms/imis3/iam/institution?search=name:1" | jq .
```

### Create a New Institution

```bash
curl --fail -H "Authorization: Bearer $access_token" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Institution",
    "measFacilName": "new_inst",
    "measFacilId": "12345",
    "network": "02",
    "serviceBuildingLocation": "Fantasy",
    "serviceBuildingStreet": "Street 1",
    "serviceBuildingPostalCode": 78945,
    "tags": ["Bundesministerium"]
  }' \
  http://localhost:48081/realms/imis3/iam/institution | jq .
```

### Update an Institution

```bash
curl -X PUT --fail -H "Authorization: Bearer $access_token" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "...",
    ...
  }' \
  http://localhost:48081/realms/imis3/iam/institution | jq .
```

## Export Examples

### Export Users to CSV

CSV Export with specific attributes:

```bash
curl --fail -H "Authorization: Bearer $access_token" \
 'http://localhost:48080/realms/imis3/iam/export/user?fieldSeparator=%3B&rowDelimiter=%0D%0A&encoding=iso-8859-15&quoteType=%22&attributes=firstName&attributes=lastName&attributes=institutions'
```

Additionally with a search filter:

```bash
curl --fail -H "Authorization: Bearer $access_token" \
  'http://localhost:48081/realms/imis3/iam/export/user?fieldSeparator=%3B&rowDelimiter=%0D%0A&encoding=iso-8859-15&quoteType=%22&attributes=firstName&attributes=lastName&attributes=institutions'
```

### Export Institutions to CSV

```bash
curl --fail -H "Authorization: Bearer $access_token" \
  'http://localhost:48081/realms/imis3/iam/export/institution?fieldSeparator=%3B&rowDelimiter=%0D%0A&encoding=iso-8859-15&quoteType=%22&attributes=name&attributes=measFacilName'
```

## Error Handling

Check for the HTTP code 401 Unauthorized, which indicates the Authorization token is expired.

Using `--fail` with curl will set the exit code of the command to non-zero of the HTTP status code indicates an error.
But this will also hide the HTTP body by default, which indicates the reason for the error.

## Complete Script Example

An example script that authenticates and lists users is included in file [`api-example.sh`](./api-example.sh).
