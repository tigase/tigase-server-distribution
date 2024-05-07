Tigase's Docker package for the Tigase XMPP Server.

![Docker Pulls](https://img.shields.io/docker/pulls/tigase/tigase-xmpp-server)
![Docker Image Size (tag)](https://img.shields.io/docker/image-size/tigase/tigase-xmpp-server/8.1.0)

# Supported tags and respective `Dockerfile`s

## Simple tags
-   `nightly`
-   `nightly-enterprise`
-   `8.3.1`, `latest`
-   `8.3.1-enterprise`, `latest-enterprise`
-   `8.3.0`, `8.3.0-enterprise`
-   `8.2.4`, `8.2.4-enterprise`
-   `8.2.3`, `8.2.3-enterprise`
-   `8.2.2`, `8.2.2-enterprise`
-   `8.2.1`, `8.2.1-enterprise`
-   `8.2.0`, `8.2.0-enterprise`
-   `8.1.2`
-   `8.1.1`
-   `8.1.0`
-   `8.0.0`
-   `8.0.0-jre8`

> **_NOTE:_** `enterprise` flavours contain commercial components

> **_NOTE:_** versions 8.3.0 and 8.2.3 and earlier were build in project [tigase-xmpp-server-docker](https://tigase.dev/tigase/_server/tigase-xmpp-server-docker/)

# What is Tigase XMPP server?

[Tigase XMPP Server](https://tigase.dev/tigase/_server/tigase-server/) is scalable and performant server implementation of the XMPP protocol written in Java.

For more information about Tigase XMPP Server and related products, please visit https://tigase.net.

Documentation for Tigase XMPP Server is available at https://docs.tigase.net.

Available from [DockerHub](https://hub.docker.com/r/tigase/tigase-xmpp-server)

![Tigase logo](logo.png)

# How to use this image?

## Starting Tigase XMPP Server

Starting Tigase XMPP Server is very simple:

```bash
$ docker pull tigase/tigase-xmpp-server
$ docker run --name tigase-server -p 8080:8080 -p 5222:5222 tigase/tigase-xmpp-server:tag
```

where `tigase-server` is name of the container that will be created and `tag` is the tag specifying version of Tigase XMPP Server to run (if `tag` is not specified then `latest` will be used)

If Tigase XMPP Server is started for the first time (without any configuration), it will start web-based setup at port 8080.

## Configuration

### Setting hostname

In some cases, ie. in clustering, you may want to change the hostname of the container of Tigase XMPP Server. To do so you need to add `--hostname cluster-node-1` (`-h cluster-node-1`) to the list of `docker run` parameters.

#### Dedicated network

It's often a good idea to group related docker services on same, dedicated network. First, create the network: `docker network create -d bridge tigase_cluster` and then add it to `docker run` parameters: `--network tigase_cluster`

### Exposing ports

Tigase XMPP Server as the XMPP server is only useful if accessible from the outside of the container. Tigase exposes the following ports:
- `5222` - for incomming client to server XMPP connections (over StartTLS)
- `5223` - for incomming client to server XMPP connections (over DirectTLS/SSL)
- `5269` - for federated XMPP connections (s2s)
- `5277` - for inter-cluster communication
- `5280` - for BOSH connections
- `5281` - for BOSH connections over TLS/SSL
- `5290` - for WebSocket connections
- `5291` - for WebSocket connections over TLS/SSL
- `8080` - for HTTP server (web-based setup, REST API, file upload extension, etc.)
- `9050` - for JXM monitoring

Docker image defines all the above ports as exportable, however, it depends on the Tigase XMPP Server configuration if a particular service is available at any of those ports.

### Connecting to external database

If you want to use Tigase XMPP Server with the external database you need to connect Tigase XMPP Server container to the database container (must be in the same docker network) or allow Tigase XMPP Server to access database server.

Tigase XMPP Server supports the following databases:
- DerbyDB
- MySQL
- MSSQL
- PostgreSQL
- MongoDB

for details about a required version of the databases please check Tigase XMPP Server documentation at https://docs.tigase.net/.

It is recommended to pass database username and password for creation and schema management of the database.

```bash
$ docker run -e 'DB_ROOT_USER=root' -e 'DB_ROOT_PASS=root-pass' --name tigase-server -d tigase/tigase-xmpp-server
```

This will allow Tigase XMPP Server to manage and verify database schema.

Database configuration may be then done using web-based setup.

### Automatically creating Admin user

It's possible to pass `ADMIN_JID` and `ADMIN_PASSWORD` environment variables (using `-e` parameter) to automatically create an Administrator user.

### Exported volumes

This image exports following volumes to allow you to keep configuration, logs and persistent data outside of the container:
- `/home/tigase/tigase-server/etc/` - configuration of the server *(default config files will be created after first startup of the container)*
- `/home/tigase/tigase-server/certs/` - SSL certificates for use by the server for securing connectivity
- `/home/tigase/tigase-server/logs/` - detailed logs of the server
- `/home/tigase/tigase-server/data/` - data stored by HTTP-based file upload feature of the server

> **NOTE:** It's possible (and recommended) to share `etc` configuration directory across Tigase cluster as all instances use the same configuration.

### Tweaking memory configuration

When using default Tigase's docker images JDK11 is used, which is aware about being run within (docker) container. However, one should keep in mind that default JDK's memory settings will be applied (minimum heap of 25% of memory, maximum heap of 50% of available memory - either container's [if set], or host machine). It's possible to adjust those by setting `PRODUCTION_HEAP_SETTINGS` environment variable to the desired value. For example, to configure Tigase's JVM to use 90% and start with small initial heap add following `-e 'PRODUCTION_HEAP_SETTINGS=-XX:MaxRAMPercentage=90 -Xms128m'` to `docker run`.

It's also possible to tweak garbage collector settings by setting `GC` environment variable.

### Operating system settings

When running Tigase XMPP Server in production it's essential to apply configuration outlined in [Linux Settings for High Load Systems](https://docs.tigase.net/tigase-server/master-snapshot/Administration_Guide/html/#linuxhighload). In case of Tigase's Docker image this is done via parameters applied to `docker run`:
* [`--sysctl map` (Sysctl options (default map[]))](https://docs.docker.com/engine/reference/commandline/run/#configure-namespaced-kernel-parameters-sysctls-at-runtime)
* [`--ulimit ulimit` (Ulimit options (default []))](https://docs.docker.com/engine/reference/commandline/run/#set-ulimits-in-container---ulimit)

#### Number of opened files

This parameter is inherited from the host operating system and should be configred on the host. If there is a desire to adjust it then adding `--ulimit nofile=350000:350000` to the list of `docker run` parameters would do the trick

#### TCP network settings

Network configuration adjustments are mostly needed if online user status is not detected correctly after user is disconnected (or with a significant delay).

```
--sysctl "net.ipv4.tcp_keepalive_time=60" \
--sysctl "net.ipv4.tcp_keepalive_probes=3" \
--sysctl "net.ipv4.tcp_keepalive_intvl=90" \
--sysctl "net.ipv4.ip_local_port_range=1024 65000" 
```

## Complete Run Examples

### Single, basic instance 

Below command will run the latest version of Tigase with configuration, certificates and (http upload) data directories mapped, configured root database credentials, and ports mapped. 

```bash
$ docker run -d \
    --name some_tigase \
    -v /home/tigase/etc/:/home/tigase/tigase-server/etc/ \
    -v /home/tigase/certs/:/home/tigase/tigase-server/certs/ \
    -v /home/tigase/data/:/home/tigase/tigase-server/data/ \
    -e 'DB_ROOT_USER=root' \
    -e 'DB_ROOT_PASS=root-password' \
    -p 5222:5222 \
    -p 5280:5280 \
    -p 5290:5290 \
    -p 8080:8080 \
    tigase/tigase-xmpp-server
```

Once started, open http://localhost:8080 (from the same machine, or using http://<server_hostname>:8080), follow installer steps and save configuration at the end. You can find more details in the [Connect to the Web Installer](https://docs.tigase.net/en/latest/Tigase_Administration/Quick_Start_Guide/Intro.html#connect-to-the-web-installerl).
Default credentials used to access the installer are available in the `etc/config.tdsl` and are printed in the container logs, by default those are: 'admin-user' = 'admin' and 'admin-password' = 'tigase'.

### Cluster with mysql

1. Create docker network bridge named `tigase_cluster`

```bash
$ docker network create -d bridge tigase_cluster
```

2. Create MySQL container, connect it to created `tigase_cluster` network, configure name and hostname as `tigase_mysql`, expose port and configure root user password

```bash
$ docker run -d \
    --name tigase_mysql \
    --hostname tigase_mysql \
    --network tigase_cluster \
    -p 3306:3306 \
    -e MYSQL_ROOT_PASSWORD=root-password \
    mysql:5.7
```

3. Run the latest version of Tigase connected to `tigase_cluster` network with configuration, certificates and (http upload) data directories mapped, configured root database credentials and user-facing (5222, 5280, 5290, 8080) ports exposed. 
   
```bash
$ docker run -d \
   --name tigase_cl1 \
   --hostname tigase_cl1 \
   --network tigase_cluster \
   -v /home/tigase/etc/:/home/tigase/tigase-server/etc/ \
   -v /home/tigase/certs/:/home/tigase/tigase-server/certs/ \
   -v /home/tigase/data/:/home/tigase/tigase-server/data/ \
   -e 'DB_ROOT_USER=root' \
   -e 'DB_ROOT_PASS=root-password' \
   -p 5222:5222 \
   -p 5280:5280 \
   -p 5290:5290 \
   -p 8080:8080 \
   tigase/tigase-xmpp-server
```

4. Once started, open http://localhost:8080 (from the same machine, or using http://<server_hostname>:8080), follow installer steps and save configuration at the end. You can find more details in the [Connect to the Web Installer](https://docs.tigase.net/en/latest/Tigase_Administration/Quick_Start_Guide/Intro.html#connect-to-the-web-installer).
Default credentials used to access the installer are available in the `etc/config.tdsl` and are printed in the container logs, by default those are: 'admin-user' = 'admin' and 'admin-password' = 'tigase'.

5. Restart current container

```bash
$ docker restart tigase_cl1
```

6. Add nodes to cluster

```bash
docker run -d \
   --name tigase_cl2 \
   --hostname tigase_cl2 \
   --network tigase_cluster \ 
   -v /home/tigase/etc/:/home/tigase/tigase-server/etc/ \
   -v /home/tigase/certs/:/home/tigase/tigase-server/certs/ \
   -v /home/tigase/data/:/home/tigase/tigase-server/data/ \
   -e 'DB_ROOT_USER=root' \
   -e 'DB_ROOT_PASS=root-password' \
   -p 5322:5222 \
   -p 5380:5280 \
   -p 5390:5290 \
   -p 8083:8080 \
   tigase/tigase-xmpp-server
```

> **NOTE:** Make sure that `name`, `hostname` and bounded ports are unique - in this case second node uses `tigase_cl2` (instead of `tigase_cl1`) as `name` and `hostname` and bounded ports were changed to `5322`, `5380`, `5390` and `8083` to avoid conflicts.

### Docker Compose

The simplest and easiest way to start complete Tigase XMPP Server with database and (optional) accompanying CoTURN server for local audio-video is to use Docker Compose.

To start that way, get the latest [docker-compose.yml](https://tigase.dev/tigase/_server/tigase-server/~raw/master/src/main/docker/docker-compose.yml?disposition=ATTACHMENT) from the repository:

```shell
wget https://tigase.dev/tigase/_server/tigase-server/~raw/master/src/main/docker/docker-compose.yml
```

and start the server (adding `-d` option will run it as daemon):

```shell
docker compose up
```

Afterward, please continue in web browser using web installer by opening http://localhost:8080.


> **_NOTE_**: on the _"Basic Tigase server configuration"_ screen please select `MySQL` as Database 

> **_NOTE_**: on the _"Database configuration"_ screen please set "Address of the database instance" to `db` 

> **_NOTE_**: on the _"Database configuration"_ screen please set _"Database root user credentials"_ to the ones from `docker-compose.yaml` file

When finished with the setup restart the server - if you started it as stated above just hit ctrl+c and re-run `docker compose up`, otherwise if started as daemon just restart the xmpp service: `docker compose restart xmpp`

#### CoTURN for audio/video calls

> **_NOTE_**: Optional CoTURN server for TURN/STUN can be enabled by enabling `coturn` profile: `--profil coturn`:  

```shell
docker compose --profile coturn up
```

In that case you should configure external component discovery as described in [Using STUN & TURN server with Tigase XMPP Server with XEP-0215 (External Service Discovery) ](https://tigase.org/blog/tigase-server-with-stun-turn/)

# Building & Publishing

Images are build and pushed using [jib](https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin#build-your-image)

There are two profiles: `build-container-image-free` and `build-container-image-enterprise` building free/FOSS and enterprise images respectively. For example:

```shell
mvn jib:build -Pbuild-container-image-free
```

# License

<img alt="Tigase Tigase Logo" src="https://github.com/tigase/website-assets/blob/master/tigase/images/tigase-logo.png?raw=true" width="25"/> Official <a href="https://tigase.net/">Tigase</a> repository is available at: https://github.com/tigase/tigase-server/.

Copyright (c) 2004 Tigase, Inc.
