FROM sonatype/nexus3:3.23.0

USER root

# Download crowd plugin
RUN curl -L https://github.com/EarlPomeroy/nexus3-jira-embedded-crowd-plugin/releases/download/3.23.0/nexus3-jira-crowd-plugin-3.23.0.jar --output /opt/sonatype/nexus/system/nexus3-jira-crowd-plugin.jar

# Uncomment to install plugin during local testing
# COPY ./target/nexus3-jira-crowd-plugin-3.20.1-SNAPSHOT.jar /opt/sonatype/nexus/system/nexus3-jira-crowd-plugin.jar

# Install plugin
RUN echo "reference\:file\:nexus3-jira-crowd-plugin.jar = 200" >> /opt/sonatype/nexus/etc/karaf/startup.properties

# setup permissions
RUN chown nexus:nexus -R /opt/sonatype/nexus

USER nexus
