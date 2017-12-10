Nexus Crowd Plugin [![Build Status](https://travis-ci.org/PatrickRoumanoff/nexus-crowd-plugin.png)](https://travis-ci.org/PatrickRoumanoff/nexus-crowd-plugin)
==================

This plugin works with Nexus 2.14.x and Crowd 2.x

This is a fork of the original work done by sonatype, but as far as I can tell
they stopped supporting it after Nexus 1.8, the code got moved quite a bit from
svn to github, lost on the way and only the source for Nexus 1.3 is now
available from gitHub.

With a bit of research, I was able to pull the latest available code base for
Nexus 1.8 which was taged 1.6.2-SNAPSHOT from the sonatype forge repository
thanks to the source artifact, but this later version is missing a test suite.

The crowd integration is now using the Crowd REST API.

The aim of this project is to offer an integration between Nexus and Crowd that
can be installed on Nexus 2.x OSS and offers the same level of functionality than
its Nexus 1.8 counter part.

To build the Nexus Plugin bundle, use the maven lifecycle pre-integration-test

Please read on at http://patrickroumanoff.github.io/nexus-crowd-plugin/
