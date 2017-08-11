# startup command for rpi

# 1. create log folder
mkdir -p logs

# 2. start reader in debug mode
java -DdebugMode=1 -DapiHost=http://azai.synology.me:8888 -DreaderHost=192.168.0.100 -jar impinjCtrl.jar