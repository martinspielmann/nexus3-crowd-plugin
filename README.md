#Nexus3 Crowd Plugin
This plugin adds a Crowd realm to Sonatype Nexus OSS and enables you to authenticate with Crowd Users and authorize with crowd roles.

It works with Nexus 3.2.x and Crowd 2.x

This is a fork of http://patrickroumanoff.github.io/nexus-crowd-plugin/

<a href='https://ci.pingunaut.com/job/pingunaut/job/nexus3-crowd-plugin/job/master/'><img src='https://ci.pingunaut.com/buildStatus/icon?job=pingunaut/nexus3-crowd-plugin/master'></a>

##Prerequisites
* JDK 8 is installed
* Apache Maven is installed
* Sonatype Nexus OSS 3.2.x is installed 

#####Directory naming convention:
When Nexus gets downloaded and unzipped, there are typically two directories created:
* nexus-3.2.1-01
* sonatype-work/nexus3

To avoid confusion, the conventions of the Sonatype reference will be used in the following descriptions:
* nexus-3.2.1-01 will be referred to as **$install-dir**
* sonatype-work/nexus3 will be referred to as **$data-dir**

See [https://books.sonatype.com/nexus-book/reference3/install.html#directories](https://books.sonatype.com/nexus-book/reference3/install.html#directories) for reference.



##Installation

####1.Build the plugin
Build and install the into your local maven repository using the following commands:
```
git clone https://github.com/pingunaut/nexus3-crowd-plugin.git
cd nexus3-crowd-plugin
mvn install
```

####2. Copy all needed jars into nexus system folder
```
cp -ra ~/.m2/repository/com/pingunaut *[**$install-dir**/system/com]*
```

####3. Add bundle to startup properties
Append the following line to *startup.properties* file found in *[**$install-dir**/etc/karaf]*<br />
Please replace [PLUGIN_VERSION] by the current plugin version.
```
mvn\:com.pingunaut.nexus/nexus3-crowd-plugin/[PLUGIN_VERSION] = 200
```

####4. Create crowd.properties
Create a *crowd.properties* file in *[**$install-dir**/etc]*<br/>
The file has to contain the following properties:
```
crowd.server.url=http://localhost:8095/crowd (replace by your nexus url)
application.name=nexus (replace by your nexus application name configured in crowd)
application.password=nexus (replace by your nexus application password configured in crowd)
cache.authentication=false (should authentication be cached? default is false)
```

**Remark:** Caching can improve authentication performance significantly 
by moving credential validation into memory instead of requesting it from 
the crowd server every time.
However if cache.authentication is set to true, 
a hashed version of user credentials will be cached. 
This might be a security risk and is also the reason why this property defaults to false.
  
##Usage
####1. Activate Plugin
After installation you have to activate the plugin in the administration frontend.
You have to login with an administrative nexus account to do so. The default admin credentials are
* username: *admin*
* password: *admin123* (don'T forget to change it!)

After login you can navigate to the realm administration.
Activate the plugin by dragging it to the right hand side:
<img style="border: 1px solid grey;" src='https://pseudorandombullshitgenerator.com/img/nexus_crowd.png'>
####2. Map Crowd Groups to Nexus Roles
As a last step you have to map your crowd groups to nexus internal roles.
<img style="border: 1px solid grey;" src='https://pseudorandombullshitgenerator.com/img/nexus-5.png'>
A good starting point is mapping one crowd group to *nx-admin* role, so you can start managing Nexus with your Crowd Login.
* Choose a crowd group
* Think up a new unique name for the mapped role
* Add *nx-admin* to the contained roles
<img style="border: 1px solid grey;" src='https://pseudorandombullshitgenerator.com/img/nexus-6.png'>

That's it. You should no be able to logout and login with your Crowd user (provided that your Crowd user is in one of you previously mapped groups).

##Development
####1. Start nexus with console
Move into your **$install-dir**. Edit the file bin/nexus.vmoptions to contain the following line
```
-Dkaraf.startLocalConsole=true
```
After that (re-)start nexus. It will then startup with an interactive console enabled. (If the console doesn't show up, you may hit the Enter key after startup).
Your console should look like this afterwards:
```
karaf@root()> 
```
  
####2. Install plugin bundle
  Within the console just type
  ```
  bundle:install -s file://[ABSOLUTE_PATH_TO_YOUR_JAR]
  ```

##Contributing
[![GitHub contributors](https://img.shields.io/github/contributors/pingunaut/nexus3-crowd-plugin.svg)](https://github.com/pingunaut/nexus3-crowd-plugin/graphs/contributors)

Thanks to all contributors who helped to get this up and running
