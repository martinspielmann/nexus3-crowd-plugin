Nexus Crowd Plugin [![Build Status](https://travis-ci.org/PatrickRoumanoff/nexus-crowd-plugin.png)](https://travis-ci.org/PatrickRoumanoff/nexus-crowd-plugin)
==================

This plugin works with Nexus 3.x and Crowd 2.x

This is a fork of the original work done by Sonatype, but
they stopped supporting the oss version and moved it to Nexus Pro, 
if you need a supported version go buy their awesome software.

The crowd integration is using the Crowd REST API - which allows us to ignore all Atlassian dependencies and greatly simplifies the dev process.

The aim of this project is to offer an integration between Nexus and Crowd that
can be installed on Nexus 3.x OSS and offers some basic functionality; for advanced features please look up Nexus Pro.

To build the Nexus Plugin bundle, you can run : mvn clean package bundle:bundle

Please read on at https://github.com/PatrickRoumanoff/nexus-crowd-plugin/wiki
