# startup command for rpi

# 1. create log folder
#mkdir -p logs

# 2. restart ntp service to sync system time
sudo /etc/init.d/ntp stop
sudo ntpd -q -g
sudo /etc/init.d/ntp start

# 3. start command. Link to dev IP
java -DapiHost=http://192.168.0.253:1337 -DreaderHost=192.168.0.100 -jar impinjCtrl.jar