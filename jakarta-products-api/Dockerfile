FROM quay.io/wildfly/wildfly:31.0.0.Final-jdk17

USER root

# Create PostgreSQL module directory
RUN mkdir -p /opt/jboss/wildfly/modules/org/postgresql/main

# Copy PostgreSQL driver
COPY --chown=jboss:jboss target/dependency/postgresql-*.jar /opt/jboss/wildfly/modules/org/postgresql/main/postgresql.jar

# Create module.xml for PostgreSQL driver with all required dependencies
RUN echo '<?xml version="1.0" encoding="UTF-8"?>' > /opt/jboss/wildfly/modules/org/postgresql/main/module.xml && \
    echo '<module xmlns="urn:jboss:module:1.9" name="org.postgresql">' >> /opt/jboss/wildfly/modules/org/postgresql/main/module.xml && \
    echo '    <resources>' >> /opt/jboss/wildfly/modules/org/postgresql/main/module.xml && \
    echo '        <resource-root path="postgresql.jar"/>' >> /opt/jboss/wildfly/modules/org/postgresql/main/module.xml && \
    echo '    </resources>' >> /opt/jboss/wildfly/modules/org/postgresql/main/module.xml && \
    echo '    <dependencies>' >> /opt/jboss/wildfly/modules/org/postgresql/main/module.xml && \
    echo '        <module name="java.sql"/>' >> /opt/jboss/wildfly/modules/org/postgresql/main/module.xml && \
    echo '        <module name="java.logging"/>' >> /opt/jboss/wildfly/modules/org/postgresql/main/module.xml && \
    echo '        <module name="java.management"/>' >> /opt/jboss/wildfly/modules/org/postgresql/main/module.xml && \
    echo '        <module name="java.naming"/>' >> /opt/jboss/wildfly/modules/org/postgresql/main/module.xml && \
    echo '        <module name="java.security.sasl"/>' >> /opt/jboss/wildfly/modules/org/postgresql/main/module.xml && \
    echo '        <module name="java.xml"/>' >> /opt/jboss/wildfly/modules/org/postgresql/main/module.xml && \
    echo '    </dependencies>' >> /opt/jboss/wildfly/modules/org/postgresql/main/module.xml && \
    echo '</module>' >> /opt/jboss/wildfly/modules/org/postgresql/main/module.xml

USER jboss

# Copy CLI script
COPY configure-datasource.cli /opt/jboss/wildfly/

# Configure datasource at build time using embedded CLI
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/configure-datasource.cli

# Copy the WAR file
COPY target/products-api.war /opt/jboss/wildfly/standalone/deployments/

EXPOSE 8080 9990

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
