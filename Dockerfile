FROM sonatype/nexus3

USER root

# Download crowd plugin
COPY ./target/nexus3-jira-crowd-plugin-3.7.2-SNAPSHOT.jar /opt/sonatype/nexus/system/nexus3-jira-crowd-plugin.jar

# Install plugin
RUN echo "reference\:file\:nexus3-jira-crowd-plugin.jar = 200" >> /opt/sonatype/nexus/etc/karaf/startup.properties

# setup permissions
RUN chown nexus:nexus -R /opt/sonatype/nexus

USER nexus
