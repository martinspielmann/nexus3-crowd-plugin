Nexus3 Crowd Plugin
==================

This plugin adds a Crowd realm to Sonatype Nexus and enables you to authenticate with Crowd Users and authorize with crowd roles.

It works with Nexus 3.x and Crowd 2.x

This is a fork of http://patrickroumanoff.github.io/nexus-crowd-plugin/

<a href='https://ci.pingunaut.com/job/pingunaut/job/nexus3-crowd-plugin/job/master/'><img src='https://ci.pingunaut.com/buildStatus/icon?job=pingunaut/nexus3-crowd-plugin/master'></a>

Usage
-
1. Get the plugin (<a href='https://github.com/pingunaut/nexus3-crowd-plugin/releases/download/3.0.1-01/nexus3-crowd-plugin-3.0.1-01.jar.zip'>Downloadable zip file</a>) orbuild it by yourself using the these commands
  ```
  git clone https://github.com/pingunaut/nexus3-crowd-plugin.git
  cd nexus3-crowd-plugin
  mvn install
  ```
  
2. Create crowd.properties file in [NEXUS_INSTALL_DIR]/etc. The file has to contain the following properties:
  ```
  crowd.server.url (e.g. http://localhost:8095/crowd)
  application.name
  application.password
  ```

3. Start nexus with console
  Move into your nexus installation folder. Edit the file bin/nexus.vmoptions to contain the following line
  ```
  -Dkaraf.startLocalConsole=true
  ```
  After that (re-)start nexus. It will then startup with an interactive console enabled. (If the console doesn't show up, you may hit the Enter key after startup).
  Your console should look like this afterwards:
  ```
  karaf@root()> 
  ```
  
4. Install plugin bundle
  Within the console just type
  ```
  bundle:install -s file://[ABSOLUTE_PATH_TO_YOUR_JAR]
  ```
  
5. Configure the plugin
  After installing the Crowd realm should show up in the realm administration and you can activate it like shown below:
  <img src='https://pseudorandombullshitgenerator.com/img/nexus_crowd.png'>

  A good starting point is mapping one crowd role to a nexus administrator role. Navigate to the role management, chose a crowd role a mapped role and map it to your desired nexus role.
  <img src='https://pseudorandombullshitgenerator.com/img/nexus-5.png'>
