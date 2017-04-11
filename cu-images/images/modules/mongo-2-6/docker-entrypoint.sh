#!/bin/bash
set -e

if [ "${1:0:1}" = '-' ]; then
	set -- mongod "$@"
fi

# allow the container to be started with `--user`
if [ "$1" = 'mongod' -a "$(id -u)" = '0' ]; then
	chown -R mongodb /data/configdb /data/db
	exec gosu mongodb "$BASH_SOURCE" "$@"
fi

if [ "$1" = 'mongod' ]; then
	numa='numactl --interleave=all'
	if $numa true &> /dev/null; then
		set -- $numa "$@"
	fi
fi

if [[ -z "$APPLICATIVE_MONITORING" ]] || [ "$APPLICATIVE_MONITORING" -eq 1 ]; then
	/opt/cloudunit/monitoring-agents/metricbeat/metricbeat -c /opt/cloudunit/monitoring-agents/metricbeat/conf.d/mongodb.yml&
fi

exec "$@"
