# IAM Client

## Docker Setup

Build image:

```bash
docker build --force-rm=true -t bfs/iam_client .
```

Run container

```bash
docker run --name iam_client -v ../../client/:/usr/local/iamclient/ -p 8180:80 -d bfs/iam_client
```

The client should listen to localhost:8180.
