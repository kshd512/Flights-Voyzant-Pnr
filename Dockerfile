FROM 524881529748.dkr.ecr.ap-south-1.amazonaws.com/mmt-ecs-base:java-8-ubuntu22

RUN mkdir -p /opt/service

ENV PROFILE prod

ENV PORT 8000

ENV JAR_FILE VOYZANT-Pnr-Web-0.0.1.jar

COPY VOYZANT-Pnr-Web/target/VOYZANT-Pnr-Web-0.0.1.jar /opt/service/

COPY .docker/kerberos/* /opt/kerberos/security/

WORKDIR /opt/service

CMD ["java", "-Xmx3g", "-Xms2g", "-jar", "-Dprofile=prod", "-Dserver.port=8000", "/opt/service/VOYZANT-Pnr-Web-0.0.1.jar"]