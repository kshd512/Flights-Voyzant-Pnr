#!/bin/sh
>nohup.out
/bin/su mmytu -c "/usr/bin/nohup /opt/java/jdk8/bin/java -Xmx3072M -jar -Dprofile=prod -Dserver.port=8083 voyzant-pnr-web-0.0.1.jar >/dev/null 2>&1 &"
