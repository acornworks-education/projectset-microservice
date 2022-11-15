#!/bin/bash

docker-compose up -d database
docker-compose run flyway
docker-compose up -d featuretoggle
docker-compose up -d monolith

docker-compose exec featuretoggle ./flipt import /var/opt/flipt/data.yaml
