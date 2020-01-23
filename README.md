# Nexus3 Crowd Plugin
This plugin adds a Jira embedded Crowd realm to Sonatype Nexus OSS and enables you to authenticate with Jira Crowd Users and authorize with Jira Crowd roles. This plugin is primarily written to work with the docker install of Nexus.

It works with Nexus 3.x and Jira 8.x and later

[![Build Status](https://travis-ci.org/EarlPomeroy/nexus3-jira-embedded-crowd-plugin.svg?branch=master)](https://travis-ci.org/EarlPomeroy/nexus3-jira-embedded-crowd-plugin)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](https://github.com/EarlPomeroy/nexus3-jira-embedded-crowd-plugin/blob/master/LICENSE)
[![codecov](https://codecov.io/gh/EarlPomeroy/nexus3-jira-embedded-crowd-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/EarlPomeroy/nexus3-jira-embedded-crowd-plugin)


##### Directory naming convention:
When Nexus gets pulled and ran from docker, there are typically two directories created:
* /opt/sonatype/nexus
* /opt/sonatype/sonatype-work/nexus3 (usually a symlink to /nexus-data)

To avoid confusion, the conventions of the Sonatype reference will be used in the following descriptions:
* /opt/sonatype/nexus will be referred to as **$install-dir**
* /opt/sonatype/sonatype-work/nexus3 or /nexus-data will be referred to as **$data-dir**

See [https://books.sonatype.com/nexus-book/reference3/install.html#directories](https://books.sonatype.com/nexus-book/reference3/install.html#directories) for reference.

## Installation

### Test installation with docker
#### 1. Use the following Dockerfile
```
FROM sonatype/nexus3

USER root

# Download crowd plugin
COPY ./target/nexus3-jira-crowd-plugin-3.21.0-SNAPSHOT.jar /opt/sonatype/nexus/system/nexus3-jira-crowd-plugin.jar

# Install plugin
RUN echo "reference\:file\:nexus3-jira-crowd-plugin.jar = 200" >> /opt/sonatype/nexus/etc/karaf/startup.properties

# setup permissions
RUN chown nexus:nexus -R /opt/sonatype/nexus

USER nexus
```

#### 2. Setup /nexus-data volume

```
mkdir -p /path/to/your/nexus-data/etc
# Nexus likes to run with UID 200 
chown -R 200 /path/to/your/nexus-data

# For Mac, 200 doesn't work, open permission on the folder
chmod 777 /path/to/your/nexus-data

# Copy crowd-properties.json to the etc folder
cp crowd-properties.json /path/to/your/nexus-data/etc
```
#### 3. Setup crowd-properties file
Create a *crowd-properties.json* file in **$install-dir**/etc<br/>
The file has to contain the following properties:
```
{
  "serverURL": "https://jira.example.com",
  "appUser": "nexus",
  "appPass": "nexus",
  "authCache": false,
  "connectTimeout": 15000,
  "socketTimeout": 15000,
  "requestTimeout": 15000,
  "filterGroup": "jira-users",
  "roleMapping": [
    {
      "nexusRole": "nx-user",
      "jiraRole": "jira-user"
    },
    {
      "nexusRole": "nx-admin",
      "jiraRole": "jira-admin"
    }
  ]
}
```
 Field | Purpose 
|----:|:--------- |
**serverURL**|URL to Jira install
**appUser**|Jira admin user for making REST API calls to Jira server
**appPass**|Jira admin password for authentication. Usually stored in docker secrets.
**authCache**|(true or false) Cache hashed credentials in plugin (see not below)
**connectTimeout**|Connection timeout
**socketTimeout**|Socket timeout
**requestTimeout**|Request timeout
**filterGroup**|Jira Group all users must belong to for login
**roleMapping**|List of Jira Groups that are mapped to Nexus groups
**nexusRole**|Nexus Role
**jiraRole**|Jira group
     

#### 3. Run in Terminal

```
docker build -t nexus-test .
docker run --rm -it -v /path/to/your/nexus-data:/nexus-data -p 8081:8081 nexus-test
```

### Native installation without docker

#### Prerequisites
* JDK 8 is installed
* Sonatype Nexus OSS 3.x is installed 

#### 1. Download latest release jar into nexus system folder
Releases can be found here: https://github.com/pingunaut/nexus3-crowd-plugin/releases
```
cd $install-dir/system/
wget https://github.com/EarlPomeroy/nexus3-jira-embedded-crowd-plugin/releases/download/nexus3-jira-crowd-plugin-3.20.0/nexus3-jira-crowd-plugin-3.20.0.jar
```

#### 2. Add bundle to startup properties
Append the following line to *startup.properties* file found in **$install-dir**/etc/karaf
```
reference\:file\:nexus3-jira-crowd-plugin-3.20.0.jar = 200
```

#### 3. Setup crowd-properties file
See step 3 in Docker test install

**appPass** should be set in a file and passed as the key's value or set in the environment variable $APPLICATION_PASSWORD
  
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

That's it. You should no be able to logout and login with your Crowd user (provided that your Crowd user is in one of you previously mapped groups).

**Remark:** Caching can improve authentication performance significantly by moving credential validation into memory instead of requesting it from the crowd server every time. However if authCache is set to true, a hashed version of user credentials will be cached. This might be a security risk and is also the reason why this property defaults to false.

## Development

#### 1.Build the plugin
Build and install the into your local maven repository using the following commands:
```
https://github.com/EarlPomeroy/nexus3-jira-embedded-crowd-plugin.git
cd nexus3-jira-embedded-crowd-plugin
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
