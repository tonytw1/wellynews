FROM tomcat:9-jre11
RUN rm -r /usr/local/tomcat/webapps/ROOT
COPY target/searchwellington-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
COPY catalina.properties /usr/local/tomcat/conf/catalina.properties

COPY certs/gdig2.crt gdig2.crt
RUN /usr/bin/keytool -import -alias gdig2 -keystore /usr/lib/jvm/java-11-openjdk-amd64/lib/security/cacerts -file gdig2.crt -noprompt -storepass changeit
COPY certs/Thawte_RSA_CA_2018.crt Thawte_RSA_CA_2018.crt 
RUN /usr/bin/keytool -import -alias Thawte_RSA_CA_2018 -keystore /usr/lib/jvm/java-11-openjdk-amd64/lib/security/cacerts -file Thawte_RSA_CA_2018.crt -noprompt -storepass changeit
