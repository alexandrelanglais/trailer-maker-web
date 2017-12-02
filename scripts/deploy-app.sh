#!/usr/bin/env bash

set -eux

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd $DIR/..

sbt universal:packageZipTarball

SRV_HOME_DIR="/home/$DEPLOY_USER"
SRV_PROJECT_DIR="$SRV_HOME_DIR/trailer-maker-web"
USER_SERVER=$DEPLOY_USER@$DEPLOY_SERVER

ssh $USER_SERVER 'bash -s' << EOF
set -ux
# Kill previous running program.
sudo jps -mvl | grep play.core.server.ProdServerStart | cut -f1 -d ' ' | xargs --verbose sudo kill -9
EOF

rsync -av --progress --delete target/universal/*.tgz $USER_SERVER:$SRV_HOME_DIR/trailer-maker-web.tgz

ssh $USER_SERVER 'bash -s' << EOF
set -eux

cd $SRV_HOME_DIR
sudo rm -Rf backup-trailer-maker-web/
sudo mv trailer-maker-web/ backup-trailer-maker-web/
tar -xvf trailer-maker-web.tgz
sudo mv trailer-maker-web-* trailer-maker-web -f
sudo jps -mvl
sudo ./startServer.sh &>> /tmp/trailer-maker-web.out &

sleep 25
tail -n 150 /tmp/trailer-maker-web.out
EOF

curl -X GET "http://$DEPLOY_SERVER/" |  tail -n 10
