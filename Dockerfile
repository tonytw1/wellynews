FROM openjdk:10-jre
COPY target/wellynews-0.0.1-SNAPSHOT.jar /opt/squadlist/wellynews-0.0.1-SNAPSHOT.jar

COPY certs/gdig2.crt gdig2.crt
RUN /usr/bin/keytool -import -alias gdig2 -keystore /usr/lib/jvm/java-11-openjdk-amd64/lib/security/cacerts -file gdig2.crt -noprompt -storepass changeit
COPY certs/Thawte_RSA_CA_2018.crt Thawte_RSA_CA_2018.crt
RUN /usr/bin/keytool -import -alias Thawte_RSA_CA_2018 -keystore /usr/lib/jvm/java-11-openjdk-amd64/lib/security/cacerts -file Thawte_RSA_CA_2018.crt -noprompt -storepass changeit

CMD ["java","-jar","/opt/wellynews/wellynews-0.0.1-SNAPSHOT.jar", "--spring.config.location=/opt/wellynews/conf/wellynews.properties"]
