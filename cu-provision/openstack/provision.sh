#!/bin/bash

export DEBIAN_FRONTEND=noninteractive
export LC_ALL=en_US.UTF-8
export LANG=en_US.UTF-8
export LANGUAGE=en_US.UTF-8

readonly COMPOSE_VERSION=1.8.0

set -x

CU_USER=admincu

# CREATE ADMINCU USER admincu account
useradd -m -s /bin/bash $CU_USER
usermod -G sudo $CU_USER

# PROVISION THE ENV
apt-get install -y nmap
apt-get install -y htop
apt-get install -y ncdu
apt-get install -y git
apt-get install -y haveged

# CLONE CLOUDUNIT
cd /home/$CU_USER && git clone https://github.com/Treeptik/cloudunit.git -b dev
chown -R $CU_USER:$CU_USER /home/$CU_USER

#==========================================================#
sudo apt-get update
sudo apt-get upgrade

cp -f /home/$CU_USER/cloudunit/cu-vagrant/files/profile /home/$CU_USER/.profile
source /home/$CU_USER/.profile
cp -f /home/$CU_USER/cloudunit/cu-vagrant/files/hosts /etc/hosts
cp -f /home/$CU_USER/cloudunit/cu-vagrant/files/environment /etc/environment
cp -f /home/$CU_USER/cloudunit/cu-vagrant/files/.bashrc /home/$CU_USER/.bashrc
sudo apt-get install -y apt-transport-https ca-certificates
sudo apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
sudo cp -f cloudunit/cu-vagrant/files/sources.list /etc/apt/sources.list

sudo apt-get update

sudo apt-get install -y apt-transport-https
sudo apt-get install -y curl
sudo apt-get install -y vim
sudo apt-get install -y haveged

#==========================================================#

# clean up
sudo rm -f \
  /var/log/messages   \
  /var/log/lastlog    \
  /var/log/auth.log   \
  /var/log/syslog     \
  /var/log/daemon.log \
  /var/log/upstart/docker.log \
  /home/$CU_USER/.bash_history \
  /var/mail/$CU_USER           \
  || true

#==========================================================#

#
# Docker-related stuff
#
sudo apt-get update
sudo apt-get install -y linux-image-extra-$(uname -r)
sudo apt-get install -y docker-engine
sudo apt-get install -y mysql-client

#sudo apt-mark hold docker-engine
sudo usermod -aG docker $CU_USER

# stop docker
sudo service docker stop

# configure docker
sudo cp /home/$CU_USER/cloudunit/cu-vagrant/files/docker.no.secure.service  /etc/default/docker

# restart docker
sudo service docker start

# enable memory and swap accounting
sudo cp /home/$CU_USER/cloudunit/cu-vagrant/files/grub /etc/default/grub
sudo update-grub

# install Docker Compose
# @see http://docs.docker.com/compose/install/
curl -o docker-compose -L https://github.com/docker/compose/releases/download/$COMPOSE_VERSION/docker-compose-`uname -s`-`uname -m`
chmod a+x docker-compose
sudo mv docker-compose /usr/local/bin

cd /home/$CU_USER/cloudunit/cu-services && ./build-services.sh all
