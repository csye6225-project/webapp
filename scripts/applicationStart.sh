#!/bin/bash

sudo iptables -t nat -I PREROUTING -p tcp --dport 80 -j REDIRECT --to-ports 8080
sudo nohup java -jar /home/ubuntu/app/demo-0.0.1-SNAPSHOT.jar > /dev/null 2> /dev/null < /dev/null &