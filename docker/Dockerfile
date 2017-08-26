FROM java:openjdk-8-jdk
EXPOSE 8080
WORKDIR /opt/app/
RUN apt-get update && apt-get install -y zip jq
ADD kinesis-sandbox.jar /opt/app/
ADD production.yml /opt/app/

ENV JAVA_OPTS -Xms512m -Xmx512m     -XX:NewSize=256m -XX:MaxNewSize=256m

CMD java -jar kinesis-sandbox.jar db migrate ./production.yml && java \
  -server \
  $JAVA_OPTS \
  -verbose:gc -Xloggc:/var/eb_log/gc_%t_pid%p.log \
  -XX:+PrintGCDateStamps -XX:+PrintGCDetails \
  -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M \
  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/eb_log/ \
  -XX:ErrorFile=/var/eb_log/hs_err_pid%p.log \
  -Dsun.net.inetaddr.ttl=60 \
  -Djava.awt.headless=true \
  -Dfile.encoding=UTF-8 \
  -jar kinesis-sandbox.jar \
  server ./production.yml
