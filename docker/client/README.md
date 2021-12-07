# IAM Client

## Docker Setup

Build image:

```bash
cd /path/to/repo
docker build -f docker/client/Dockerfile --force-rm=true -t bfs/iam_client .
```

Run container

```bash
docker run --name iam_client -v $PWD/docker/client:/usr/local/iamdocker -v $PWD/client/:/usr/local/iamclient/ -p 8180:80 -d bfs/iam_client
```

The client should listen to localhost:8180.
