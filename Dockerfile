FROM openjdk:11-jre
COPY target/wellynews-0.0.1-SNAPSHOT.jar /opt/wellynews/wellynews-0.0.1-SNAPSHOT.jar

COPY certs/gdig2.crt gdig2.crt
RUN /usr/local/openjdk-11/bin/keytool -import -alias gdig2 -keystore /usr/local/openjdk-11/lib/security/cacerts -file gdig2.crt -noprompt -storepass changeit
COPY certs/Thawte_RSA_CA_2018.crt Thawte_RSA_CA_2018.crt
RUN /usr/local/openjdk-11/bin/keytool -import -alias Thawte_RSA_CA_2018 -keystore /usr/local/openjdk-11/lib/security/cacerts -file Thawte_RSA_CA_2018.crt -noprompt -storepass changeit

RUN echo "networkaddress.cache.ttl=60" >> /usr/local/openjdk-11/conf/security/java.security

CMD ["java", "-XshowSettings:vm", "-XX:+PrintCommandLineFlags", "-XX:MaxRAMPercentage=75","-jar","/opt/wellynews/wellynews-0.0.1-SNAPSHOT.jar", "--spring.config.location=/opt/wellynews/conf/wellynews.properties"]
