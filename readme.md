# Skin Server

This is a simple skin server to downloading, pre-caching and rendering

Current features:
- Render a skin head based on a file
- Render a skin/head based on a uuid or username
- Save skins into database for long-time cache
- Token based IP Selection for mojang requests


### Application properties 
```properties
skinserver.max-size=512 #Defines the maximum upscaling size of a skin/head
skinserver.min-size=16 #Defines the maximum downscaling size of a skin/head

skinserver.connection-addresses[0]=XXX.XXX.XXX.XX # Defines a array entry for used addresses
skinserver.connection-addresses[1]=XXX.XXX.XXX.XX # Defines a array entry for used addresses
```
# Installation with Docker
Run this command for start a simple single instance with mongodb properties.
```shell
docker run --name skinserver \
  -d \
  -e SPRING_DATA_MONGODB_AUTHENTICATION-DATABASE=admin \
  -e SPRING_DATA_MONGODB_DATABASE=skinserver \
  -e SPRING_DATA_MONGODB_HOST=127.0.0.1 \
  -e SPRING_DATA_MONGODB_PORT=27017 \
  -e SPRING_DATA_MONGODB_PASSWORD=YourPassword \
  -e SPRING_DATA_MONGODB_USERNAME=YourUsername \
  -e SKINSERVER_MAX-SIZE=512 \
  -e SKINSERVER_MIN-SIZE=16 \
  -e SKINSERVER_CONNECTION-ADDRESSES_0=127.0.0.1 \
  -p 8080:8080 \
   ghcr.io/themeinerlp/skinserver/skinserver:latest
```
Modify your database, host, username and password for your mongodb server. 
Optional: Modify SKINSERVER_MIN/MAX-SIZE and extend your IP Address Range 
# Installation with Docker-Compose
Copy this content below and paste in a `docker-compose.yaml` file
```yaml
version: '3.1'
services:
  mongo:
    image: mongo
    restart: always
    environment:
      MONGO_INITDB_ROOT_USERNAME: YourUsername
      MONGO_INITDB_ROOT_PASSWORD: YourPassword
      MONGO_INITDB_DATABASE: SkinServer
  skin-server:
    image: ghcr.io/themeinerlp/skinserver/skinserver
    restart: always
    ports:
      - 8080:8080
    environment:
      SPRING_DATA_MONGODB_AUTHENTICATION-DATABASE: admin
      SPRING_DATA_MONGODB_DATABASE: SkinServer
      SPRING_DATA_MONGODB_HOST: mongo
      SPRING_DATA_MONGODB_PORT: 27017
      SPRING_DATA_MONGODB_USERNAME: YourUsername
      SPRING_DATA_MONGODB_PASSWORD: YourPassword
      SKINSERVER_MAX-SIZE: 512
      SKINSERVER_MIN-SIZE: 16
      SKINSERVER_CONNECTION-ADDRESSES_0: 127.0.0.1
```
The run following commands:
```shell
docker-compose up -d 
```