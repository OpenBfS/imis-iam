# IMIS3 IAM  Client

## Docker Setup

If a docker network has not already been created:

```bash
docker network create imis3
```

Build image:

```bash
cd /path/to/repo
docker build -f docker/client/Dockerfile --force-rm=true -t bfs/iam_client .
```

Run container

```bash
docker run --name iam_client --network imis3 -v $PWD/docker/client:/usr/local/iamdocker -v $PWD/client/:/usr/local/iamclient/ -p 48081:80 -d bfs/iam_client
```

The client should listen to localhost:48081.
