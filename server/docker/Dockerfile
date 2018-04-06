FROM tomcat
ADD tomcat-users.xml $CATALINA_HOME/conf

HEALTHCHECK CMD curl --fail http://localhost:8080/forecast-api/health || exit 1

ENV AOL_ENVIRONMENT prod
ADD forecast-api.war /usr/local/tomcat/webapps/
RUN apk add --update curl
RUN rm -rf webapps/forecast-api && mkdir webapps/forecast-api && unzip -o webapps/forecast-api.war -d webapps/forecast-api/
RUN mkdir -p /usr/local/tomcat/logs/
RUN touch /usr/local/tomcat/logs/forecast-api.log

ADD javamelody-core-1.67.0.jar webapps/forecast-api/WEB-INF/lib
ADD jrobin-1.5.9.jar webapps/forecast-api/WEB-INF/lib
RUN apk add ttf-dejavu

CMD tail -F /usr/local/tomcat/logs/forecast-api.log & /usr/local/tomcat/bin/catalina.sh run
