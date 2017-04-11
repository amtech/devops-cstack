# LICENCE : CloudUnit is available under the Affero Gnu Public License GPL V3 : https://www.gnu.org/licenses/agpl-3.0.html
# but CloudUnit is licensed too under a standard commercial license.
# Please contact our sales team if you would like to discuss the specifics of our Enterprise license.
# If you are not sure whether the GPL is right for you,
# you can always test our software under the GPL and inspect the source code before you contact us
# about purchasing a commercial license.

# LEGAL TERMS : "CloudUnit" is a registered trademark of Treeptik and can't be used to endorse
# or promote products derived from this project without prior written permission from Treeptik.
# Products or services derived from this software may not be called "CloudUnit"
# nor may "Treeptik" or similar confusing terms appear in their names without prior written permission.
# For any questions, contact us : contact@treeptik.fr

#!/usr/bin/env bash

##
##
## FUNCTIONS
##
##

if [[ $USER != "vagrant" ]]; then
    echo "This script must be run as vagrant user for demo environment"
    exit 1
fi

function with-elk {
    cp ./.env.demo.xip.io .env
    source .env
    docker network create skynet
    docker-compose  -f docker-compose.elk.yml \
                    -f docker-compose.demo.yml \
    up -d
}

function reset {
    for container in $(docker ps -aq --format '{{.Names}}' --filter "label=origin=application"); do
      echo "Delete applicative container "$container
      docker rm -f $container
      docker volume rm $container
    done

    docker-compose  -f docker-compose.elk.yml -f docker-compose.demo.yml kill
    docker-compose  -f docker-compose.elk.yml -f docker-compose.demo.yml rm -f
    docker volume rm cucompose_elasticsearch-data
    docker volume rm cucompose_gitlab-logs
    docker volume rm cucompose_mysqldata
    docker volume rm cucompose_redis-data
    docker network rm skynet
}

##
##
## MAIN
##
##

case "$1" in

'with-elk')
with-elk
;;

'reset')
reset $2
;;


*)
echo ""
echo "Usage $0 "
echo "Example : $0 with-elk"
echo "Choice between : "
echo "                    with-elk"
echo "                    reset"
echo ""
;;

esac
