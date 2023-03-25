FROM eclipse-temurin:17-alpine
COPY target/wellynews-0.0.1-SNAPSHOT.jar /opt/wellynews/wellynews-0.0.1-SNAPSHOT.jar

COPY certs/gdig2.crt gdig2.crt
RUN /opt/java/openjdk/bin/keytool -import -alias gdig2 -keystore /opt/java/openjdk/lib/security/cacerts -file gdig2.crt -noprompt -storepass changeit
COPY certs/Thawte_RSA_CA_2018.crt Thawte_RSA_CA_2018.crt
RUN /opt/java/openjdk/bin/keytool -import -alias Thawte_RSA_CA_2018 -keystore /opt/java/openjdk/lib/security//cacerts -file Thawte_RSA_CA_2018.crt -noprompt -storepass changeit

RUN echo "networkaddress.cache.ttl=60" >> /opt/java/openjdk/conf/security/java.security

COPY honeycomb/honeycomb-opentelemetry-javaagent-1.4.2.jar /opt/honeycomb-opentelemetry-javaagent-1.4.2.jar

CMD ["java", "-XshowSettings:vm", "-XX:+PrintCommandLineFlags", "-XX:MaxRAMPercentage=75", "-javaagent:/opt/honeycomb-opentelemetry-javaagent-1.4.2.jar", "-jar","/opt/wellynews/wellynews-0.0.1-SNAPSHOT.jar", "--spring.config.location=/opt/wellynews/conf/wellynews.properties"]

