#!/bin/bash

sudo iptables -t nat -I PREROUTING -p tcp --dport 80 -j REDIRECT --to-ports 8080
cd /home/ubuntu/app
sudo nohup java -jar demo-0.0.1-SNAPSHOT.jar > /dev/null 2> /dev/null < /dev/null &