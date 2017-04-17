#!/usr/bin/env bash

for line in `cat .env | grep =`
do
    echo $line
    export $line
done

if [ "$CU_COMPOSE_FILES" = "" ]; then
    CU_K8S_FILES="-f ./cu-infra -f ./cu-monitor"
fi

if [ "$1" != "-y" ]; then
    echo "All containers will be deleted. Do you want to proceed ? [y/n]"
    read PROD_ASW
    if [ "$PROD_ASW" != "y" ] && [ "$PROD_ASW" != "n" ]; then
        echo "Enter y or n!"
        exit 1
    elif [ "$PROD_ASW" = "n" ]; then
        exit 1
    fi
fi

kubectl delete $CU_K8S_FILES
docker volume rm cu-redis
docker volume rm cu-cu-gitlab-config
docker volume rm cu-gitlab-logs
docker volume rm cu-gitlab-data
docker volume rm cu-mysql
docker volume rm cu-jekins
docker volume rm cu-nexus

echo "*******************************"
echo -e "Starting..."
echo "*******************************"

docker volume create -d flocker --name cu-redis
docker volume create -d flocker --name cu-cu-gitlab-config
docker volume create -d flocker --name cu-gitlab-logs
docker volume create -d flocker --name cu-gitlab-data
docker volume create -d flocker --name cu-mysql
docker volume create -d flocker --name cu-jekins
docker volume create -d flocker --name cu-nexus
kubectl create $CU_K8S_FILES


