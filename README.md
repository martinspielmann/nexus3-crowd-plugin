#Nexus3 Crowd Plugin
This plugin adds a Crowd realm to Sonatype Nexus OSS and enables you to authenticate with Crowd Users and authorize with crowd roles.

It works with Nexus 3.1.x and Crowd 2.x

This is a fork of http://patrickroumanoff.github.io/nexus-crowd-plugin/

<a href='https://ci.pingunaut.com/job/pingunaut/job/nexus3-crowd-plugin/job/master/'><img src='https://ci.pingunaut.com/buildStatus/icon?job=pingunaut/nexus3-crowd-plugin/master'></a>

##Prerequisites
* JDK 8 is installed
* Apache Maven is installed
* Sonatype Nexus OSS 3.1.* is installed 

#####Directory naming convention:
When Nexus gets downloaded and unzipped, there are typically two directories created:
* nexus-3.1.0-04
* sonatype-work/nexus3

To avoid confusion, the conventions of the Sonatype reference will be used in the following descriptions:
* nexus-3.1.0-04 will be referred to as **$install-dir**
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
####2. Edit karaf properties
In *[**$install-dir**/etc/karaf]* there is a file called *org.apache.karaf.features.cfg*.
#####2.1. Add nexus3-crowd-plugin to the featuresRepositories
The file contains a property called *featuresRepositories* which does already contain a 
comma separated list of feature repositories. Append the nexus3-crowd-plugin feature to the list
by pointing to the feature file in you local maven repository (the file it was created in step 1).
```
,file:${user.home}/.m2/repository/com/pingunaut/nexus/nexus3-crowd-plugin/3.1.0-04/nexus3-crowd-plugin-3.1.0-04-features.xml
```

#####2.2. Add nexus3-crowd-plugin to the boot features
Wthin the same *org.apache.karaf.features.cfg* file there is another property called *featuresBoot* which 
contains a comma separated list of features which should be started during application startup.
Append com.google.common.base (which this plugin depends on) and nexus3-crowd-plugin to it:
 ```
, com.google.common.base, nexus3-crowd-plugin
```
After editing, your *org.apache.karaf.features.cfg* file
should look something like this:
<img style="border: 1px solid grey;" src="https://pseudorandombullshitgenerator.com/img/karaf-features-properties.png" />
  
####3. Create crowd.properties
Create a crowd.properties file in *[**$install-dir**/etc/]*<br/>
The file has to contain the following properties:
  ```
  crowd.server.url (e.g. http://localhost:8095/crowd)
  application.name
  application.password
  ```
  
##Usage
#####1. Activate Plugin
After installation you have to activate the plugin in the administration frontend.
You have to login with an administrative nexus account to do so. The default admin credentials are
* username: *admin*
* password: *admin123*

After login you can navigate to the realm administration.
Activate the plugin by dragging it to the right hand side:
<img style="border: 1px solid grey;" src='https://pseudorandombullshitgenerator.com/img/nexus_crowd.png'>
#####2. Map Crowd Groups to Nexus Roles
As a last step you have to map your crowd groups to nexus internal roles.
<img style="border: 1px solid grey;" src='https://pseudorandombullshitgenerator.com/img/nexus-5.png'>
A good starting point is mapping one crowd group to *nx-admin* role, so you can start managing Nexus with your Crowd Login.
* Choose a crowd group
* Think up a new unique name for the mapped role
* Add *nx-admin* to the contained roles
<img style="border: 1px solid grey;" src='https://pseudorandombullshitgenerator.com/img/nexus-6.png'>

That's it. You should no be able to logout and login with your Crowd user (provided that user Crowd user is in one of you previously mapped groups).

##Development
#####1. Start nexus with console
Move into your **$install-dir**. Edit the file bin/nexus.vmoptions to contain the following line
  ```
  -Dkaraf.startLocalConsole=true
  ```
  After that (re-)start nexus. It will then startup with an interactive console enabled. (If the console doesn't show up, you may hit the Enter key after startup).
  Your console should look like this afterwards:
  ```
  karaf@root()> 
  ```
  
#####2. Install plugin bundle
  Within the console just type
  ```
  bundle:install -s file://[ABSOLUTE_PATH_TO_YOUR_JAR]
  ```