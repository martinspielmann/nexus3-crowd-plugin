# Nexus3 Crowd Plugin
This plugin adds a Crowd realm to Sonatype Nexus OSS and enables you to authenticate with Crowd Users and authorize with crowd roles.

It works with Nexus 3.x and Crowd 2.x and 3.x

[![Jenkins](https://img.shields.io/jenkins/s/https/ci.martinspielmann.de/job/pingunaut/job/nexus3-crowd-plugin/job/master.svg)](https://ci.martinspielmann.de/job/pingunaut/job/nexus3-crowd-plugin/job/master/)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](https://github.com/pingunaut/nexus3-crowd-plugin/blob/master/LICENSE)
[![SonarQube Coverage](https://img.shields.io/sonar/https/sonarcloud.io/nexus3-crowd-plugin/coverage.svg)](https://sonarcloud.io/component_measures?id=nexus3-crowd-plugin&metric=coverage)


##### Directory naming convention:
When Nexus gets downloaded and unzipped, there are typically two directories created:
* nexus-3.13.0-01
* sonatype-work/nexus3

To avoid confusion, the conventions of the Sonatype reference will be used in the following descriptions:
* nexus-3.13.0-01 will be referred to as **$install-dir**
* sonatype-work/nexus3 will be referred to as **$data-dir**

See [https://books.sonatype.com/nexus-book/reference3/install.html#directories](https://books.sonatype.com/nexus-book/reference3/install.html#directories) for reference.

## Installation

### Test installation with docker
#### 1. Use the following Dockerfile
```
FROM sonatype/nexus3

USER root

# Install curl
RUN yum install -y curl

# Download crowd plugin
RUN curl -L https://github.com/pingunaut/nexus3-crowd-plugin/releases/download/nexus3-crowd-plugin-3.5.0/nexus3-crowd-plugin-3.5.0.jar --output /opt/sonatype/nexus/system/nexus3-crowd-plugin.jar

# Install plugin
RUN echo "reference\:file\:nexus3-crowd-plugin.jar = 200" >> /opt/sonatype/nexus/etc/karaf/startup.properties

# Add Crowd Config
RUN touch /opt/sonatype/nexus/etc/crowd.properties
RUN echo "crowd.server.url=jira.example.com" >> /opt/sonatype/nexus/etc/crowd.properties
RUN echo "application.name=nexus" >> /opt/sonatype/nexus/etc/crowd.properties
RUN echo "application.password=nexus" >> /opt/sonatype/nexus/etc/crowd.properties
RUN echo "cache.authentication=false" >> /opt/sonatype/nexus/etc/crowd.properties

# setup permissions
RUN chown nexus:nexus -R /opt/sonatype/nexus

USER nexus
```
#### 2. Run in Terminal

```
docker build -t test .
docker run --rm -ti test
```

### Native installation without docker

#### Prerequisites
* JDK 8 is installed
* Sonatype Nexus OSS 3.x is installed 

#### 1. Download latest release jar into nexus system folder
Releases can be found here: https://github.com/pingunaut/nexus3-crowd-plugin/releases
```
cd $install-dir/system/
wget https://github.com/pingunaut/nexus3-crowd-plugin/releases/download/nexus3-crowd-plugin-3.5.0/nexus3-crowd-plugin-3.5.0.jar
```

#### 2. Add bundle to startup properties
Append the following line to *startup.properties* file found in **$install-dir**/etc/karaf
```
reference\:file\:nexus3-crowd-plugin-3.5.0.jar = 200
```

#### 3. Create crowd.properties
Create a *crowd.properties* file in **$install-dir**/etc<br/>
The file has to contain the following properties:
```
crowd.server.url=http://localhost:8095/crowd (replace by your crowd url)
application.name=nexus (replace by your nexus application name configured in crowd)
application.password=nexus (replace by your nexus application password configured in crowd)
cache.authentication=false (should authentication be cached? default is false)

# optional:
timeout.connect=15000 (default is 15000)
timeout.socket=15000 (default is 15000)
timeout.connectionrequest=15000 (default is 15000)
```
  
## Usage
#### 1. Activate Plugin
After installation you have to activate the plugin in the administration frontend.
You have to login with an administrative nexus account to do so. The default admin credentials are
* username: *admin*
* password: *admin123* (don't forget to change it!)

After login you can navigate to the realm administration.
Activate the plugin by dragging it to the right hand side:
<img style="border: 1px solid grey;" src='https://martinspielmann.de/pseudorandombullshitgenerator/wp-content/uploads/2018/01/nexus_crowd.png'>
#### 2. Map Crowd Groups to Nexus Roles
As a last step you have to map your crowd groups to nexus internal roles.
<img style="border: 1px solid grey;" src='https://martinspielmann.de/pseudorandombullshitgenerator/wp-content/uploads/2018/01/nexus-5.png'>
A good starting point is mapping one crowd group to *nx-admin* role, so you can start managing Nexus with your Crowd Login.
* Choose a crowd group
* Think up a new unique name for the mapped role
* Add *nx-admin* to the contained roles
<img style="border: 1px solid grey;" src='https://martinspielmann.de/pseudorandombullshitgenerator/wp-content/uploads/2018/01/nexus-6.png'>

That's it. You should no be able to logout and login with your Crowd user (provided that your Crowd user is in one of you previously mapped groups).

**Remark:** Caching can improve authentication performance significantly 
by moving credential validation into memory instead of requesting it from 
the crowd server every time.
However if cache.authentication is set to true, 
a hashed version of user credentials will be cached. 
This might be a security risk and is also the reason why this property defaults to false.

## Development

#### 1.Build the plugin
Build and install the into your local maven repository using the following commands:
```
git clone https://github.com/pingunaut/nexus3-crowd-plugin.git
cd nexus3-crowd-plugin
mvn install
```

#### 2. Start nexus with console
Move into your **$install-dir**. Edit the file bin/nexus.vmoptions to contain the following line
```
-Dkaraf.startLocalConsole=true
```
After that (re-)start nexus. It will then startup with an interactive console enabled. (If the console doesn't show up, you may hit the Enter key after startup).
Your console should look like this afterwards:
```
karaf@root()> 
```
  
#### 3. Install plugin bundle
  Within the console just type
  ```
  bundle:install -s file://ABSOLUTE_PATH_TO_YOUR_JAR
  ```

## Contributing
[![GitHub contributors](https://img.shields.io/github/contributors/pingunaut/nexus3-crowd-plugin.svg)](https://github.com/pingunaut/nexus3-crowd-plugin/graphs/contributors)

Thanks to all contributors who helped to get this up and running
