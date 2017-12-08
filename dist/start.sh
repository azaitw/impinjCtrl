# startup command for rpi

# 1. create log folder
# mkdir -p logs

# 2. restart ntp service to sync system time
sudo /etc/init.d/ntp stop
sudo ntpd -q -g
sudo /etc/init.d/ntp start

# 3. start command
java -DapiHost=https://azai.synology.me:8888 -DreaderHost=192.168.0.100 -jar impinjCtrl.jar
