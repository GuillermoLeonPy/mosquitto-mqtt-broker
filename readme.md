download mqtt broker docker image
=================================

docker pull eclipse-mosquitto


Write docker-compose.yml file
=================================

References
----------
* https://github.com/sukesh-ak/setup-mosquitto-with-docker
* https://www.youtube.com/watch?v=U8f95agyUJg
* https://www.youtube.com/watch?v=L26JY2NH-Ys


Create config directory to store moquitto configuration, persistence file and password file
===========================================================================================

test-user@MSIGL65:~/docs/development/mqtt/mosquito$ mkdir config
test-user@MSIGL65:~/docs/development/mqtt/mosquito$ cd config/
test-user@MSIGL65:~/docs/development/mqtt/mosquito/config$ touch mosquitto.conf

Notice that mosquitto.conf is empty.

est-user@MSIGL65:~/docs/development/mqtt/mosquito$ ls -lat
total 16
drwxrwxr-x 2 test-user test-user 4096 abr  6 16:33 config
drwxrwxr-x 3 test-user test-user 4096 abr  6 16:32 .
-rw-rw-r-- 1 test-user test-user  306 abr  6 14:43 docker-compose.yml
drwxrwxr-x 3 test-user test-user 4096 abr  6 14:00 ..


Start mqtt broker container
===========================

test-user@MSIGL65:~/docs/development/mqtt/mosquito$ docker-compose up
Creating mqtt ... done
Attaching to mqtt
mqtt         | 1712435597: mosquitto version 2.0.18 starting
mqtt         | 1712435597: Config loaded from /mosquitto/config/mosquitto.conf.
mqtt         | 1712435597: Starting in local only mode. Connections will only be possible from clients running on this machine.
mqtt         | 1712435597: Create a configuration file which defines a listener to allow remote access.
mqtt         | 1712435597: For more details see https://mosquitto.org/documentation/authentication-methods/
mqtt         | 1712435597: Opening ipv4 listen socket on port 1883.
mqtt         | 1712435597: Opening ipv6 listen socket on port 1883.
mqtt         | 1712435597: mosquitto version 2.0.18 running

Notice the message
------------------
* Connections will only be possible from clients running on this machine

No connections will be possible from outside the container


List docker running containers
==============================

test-user@MSIGL65:~/docs/development/mqtt/mosquito/config$ docker ps
CONTAINER ID   IMAGE               COMMAND                  CREATED         STATUS         PORTS                                                                                  NAMES
e9ebc933bc25   eclipse-mosquitto   "/docker-entrypoint.…"   4 minutes ago   Up 4 minutes   0.0.0.0:1883->1883/tcp, :::1883->1883/tcp, 0.0.0.0:9001->9001/tcp, :::9001->9001/tcp   mqtt
test-user@MSIGL65:~/docs/development/mqtt/mosquito/config$


Log into the broker running container and create an user, enter a password
==========================================================================

test-user@MSIGL65:~/docs/development/mqtt/mosquito/config$ docker exec -ti mqtt sh
/ # mosquitto_passwd -c /mosquitto/config/pwfile user_00
Password: 
Reenter password: 
/ # exit
test-user@MSIGL65:~/docs/development/mqtt/mosquito/config$


Notice that the config folder owner has changed
===============================================

test-user@MSIGL65:~/docs/development/mqtt/mosquito$ ls -lat
total 16
drwxrwxr-x 2      1883      1883 4096 abr  6 18:18 config
drwxrwxr-x 3 test-user test-user 4096 abr  6 16:32 .
-rw-rw-r-- 1 test-user test-user  306 abr  6 14:43 docker-compose.yml
drwxrwxr-x 3 test-user test-user 4096 abr  6 14:00 ..

test-user@MSIGL65:~/docs/development/mqtt/mosquito$ sudo ls -lat config/
[sudo] password for test-user: 
total 20
drwxrwxr-x 3 test-user test-user 4096 abr  6 18:19 ..
drwxrwxr-x 2      1883      1883 4096 abr  6 18:18 .
-rw------- 1      1883      1883  121 abr  6 16:50 pwfile
test-user@MSIGL65:~/docs/development/mqtt/mosquito$ 

* The pwfile, that holds the user credentials, is not property of 'test-user', instead is property of the mosquitto user with UID 1883, under wich the Mosquitto broker within the container.

[https://cedalo.com/blog/mosquitto-docker-configuration-ultimate-guide/#How_to_install_and_configure_Mosquitto_MQTT_broker_in_Docker_from_scratch]

Edit the mosquitto.conf file created to declare the following
=============================================================

* security: to require credentials to connect
* persistence: to store data
* listeners: to make possible connections to the broker from outside the container

Notice that, edit operations require sudo privileges
----------------------------------------------------

test-user@MSIGL65:~/docs/development/mqtt/mosquito$ sudo vim config/mosquitto.conf
test-user@MSIGL65:~/docs/development/mqtt/mosquito$ sudo cat config/mosquitto.conf
allow_anonymous false
listener 1883
listener 9001
protocol websockets
persistence true
password_file /mosquitto/config/pwfile
persistence_file mosquitto.db
persistence_location /mosquitto/data/
test-user@MSIGL65:~/docs/development/mqtt/mosquito

Restart the docker container
============================

Stop issuing Control + C
------------------------

mqtt         | 1712435597: mosquitto version 2.0.18 running
^CGracefully stopping... (press Ctrl+C again to force)
Stopping mqtt ... done

Start the container
-------------------

test-user@MSIGL65:~/docs/development/mqtt/mosquito$ docker-compose up
Starting mqtt ... done
Attaching to mqtt
mqtt         | 1712436870: mosquitto version 2.0.18 starting
mqtt         | 1712436870: Config loaded from /mosquitto/config/mosquitto.conf.
mqtt         | 1712436870: Opening ipv4 listen socket on port 1883.
mqtt         | 1712436870: Opening ipv6 listen socket on port 1883.
mqtt         | 1712436870: Opening websockets listen socket on port 9001.
mqtt         | 1712436870: mosquitto version 2.0.18 running

* Notice the message: Opening websockets listen socket on port 9001

Download and start a web broker client
======================================

test-user@MSIGL65:~$ docker run --rm --name mqttx-web -p 80:80 emqx/mqttx-web
Starting up http-server, serving ./

http-server version: 14.1.1

http-server settings: 
CORS: disabled
Cache: 3600 seconds
Connection Timeout: 120 seconds
Directory Listings: visible
AutoIndex: visible
Serve GZIP Files: false
Serve Brotli Files: false
Default File Extension: none

Available on:
  http://127.0.0.1:80
  http://172.17.0.2:80
Hit CTRL-C to stop the server


Configure a connection through the web client
=============================================

Username: user_00
Password: 123456
Host: ws://localhost
Port: 9001
Path: /mqtt

* Remember the log message after broker container start: Opening websockets listen socket on port 9001

Create a new subscription
=========================

Requires a topic name, could be any. No pre- or post-steps needed.

Example: testtopic_01/#


Send messages
=============

* Select the created topic in the web client UI
* In the send messages section, enter the topic name without '/#'
* Send a message

As the selected topic in the UI is the same in wich the message have been sent, the message appears in the screen

Examples
--------

* Connect to the broker with your created connection: user_00 / 123456
* Subscribe to a topic: testtopic/123456/#
* Using the same connection, subscribe to a topic: testtopic/456789/#
* No pre- or post-steps needed to topic subscription.
* Send messages to topic testtopic/123456/#
* Send messages to topic testtopic/456789/#
* Messages remain separated despite being sent from the same connection.
* Subscribe to a topic: testtopic/#
* Notice 'testtopic' is a prefix of the previous subscripted topics
* Despite having subscripted after the messages were being send, the subscription shows the messages that were sent to both topics: testtopic/123456 and testtopic/456789
* Each message shows the topic to which were sent.


Notice docker volumes were created
==================================

Due to volume declarations in the docker-compose.yml, docker volumes were created

test-user@MSIGL65:~/docs/development/mqtt/mosquito$ docker volume ls
DRIVER    VOLUME NAME
local     36fe45a1a840c80c1f402ff3218830b71ea31ecdcb86f6125f8899c73ba300ae
local     dfee15595d5a3c661ee618fa45518d3d9e24a0ebaccbcbdcee40d1ece081babf
local     ed090b73d1d87f5ec8c2cc67975351b0f6db2ec82bf4a9ac09df0130566c25ac
local     mosquito_config
local     mosquito_data
local     mosquito_log
test-user@MSIGL65:~/docs/development/mqtt/mosquito$

Note on before commit to repository
===================================

As files inside the config folder are not property of the operating system user, perform a change ownership operation before commit

test-user@MSIGL65:~/docs/development/mqtt/mosquito$ ls -lat
total 32
drwxrwxr-x 8 test-user test-user 4096 abr  7 08:13 .git
-rw-rw-r-- 1 test-user test-user 8381 abr  7 08:12 readme.md
drwxrwxr-x 2      1883      1883 4096 abr  7 08:09 config
-rw-rw-r-- 1 test-user test-user  403 abr  7 06:38 docker-compose.yml
drwxrwxr-x 4 test-user test-user 4096 abr  6 19:38 .
drwxrwxr-x 3 test-user test-user 4096 abr  6 14:00 ..
test-user@MSIGL65:~/docs/development/mqtt/mosquito$ ls -lat config/
total 20
drwxrwxr-x 2      1883      1883 4096 abr  7 08:09 .
-rw------- 1      1883      1883   47 abr  7 08:09 mosquitto.db
drwxrwxr-x 4 test-user test-user 4096 abr  6 19:38 ..
-rw-rw-r-- 1      1883      1883  194 abr  6 16:53 mosquitto.conf
-rw------- 1      1883      1883  121 abr  6 16:50 pwfile
test-user@MSIGL65:~/docs/development/mqtt/mosquito$ 


Change ownership
----------------

sudo chown -R test-user config/

