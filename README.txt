
General Information
===================

See http://confluence.atlassian.com/display/CONFEXT/Custom+Space+User+Management+Plugin


Directions for compiling using Maven 2
======================================

Here are some things I learned that might make it easier for those interested in using the Atlassian Plugin Developer Kit for Maven 2:

1) If you haven't already, download and install maven 2 from http://maven.apache.org/download.html and make sure your MAVEN_HOME and PATH are set as in the maven install documentation on their site.

1.1) Ensure you have given Maven enough memory by setting the following in your environment variables:

MAVEN_OPTS=-Xmx512m

2) Setup the Atlassian SDK. See the appropriate part of: http://confluence.atlassian.com/display/DEVNET/Developing+your+Plugin+using+the+Atlassian+Plugin+SDK

2.1) Install the activation jar:

Go to: http://www.oracle.com/technetwork/java/jaf102-139581.html

Download the zip file.

Unzip it.

Install it with Maven 2 (change ~/Downloads/jaf-1.0.2/activation.jar into the path to the jar your downloaded. For some reason, it cannot be a relative path.):

atlas-mvn install:install-file -DgroupId=javax.activation -DartifactId=activation -Dversion=1.0.2 -Dpackaging=jar -Dfile=/Users/myuser/Downloads/jaf-1.0.2/activation.jar

3) Go back to the trunk dir of the plugin project, so if you started off in that dir, you could just:

cd ../..

4) To build, and install to your local repository, use the following command. If problems building, see http://confluence.atlassian.com/display/DISC/Developing+Confluence+Plugins+with+Maven+2 for info on defining your pom.xml.

atlas-mvn clean install

note: if tests are failing, you may need to skip them to build the plugin. If so, use:

atlas-mvn -Dmaven.test.skip=true clean install

5) After that go into confluence into administration -> plugins. Choose this project's plugin. If it already exists, uninstall the plugin from confluence and (re)install the new one in target/...jar. If the plugin fails to load for any reason, you probably don't have enough or have too many dependencies, so see http://confluence.atlassian.com/display/DISC/Developing+Confluence+Plugins+with+Maven+2 for info on defining your pom.xml.

Other helpful hints for those new to maven 2:

To generate an IDea project:
atlas-mvn idea:idea

To generate an Eclipse project:
atlas-mvn eclipse:eclipse
(but David Peterson recommends Codehaus M2 Eclipse plugin because it includes all dependencies in single library and updates jars when pom.xml is changed)

To build in Netbeans:
atlas-mvn netbeans-freeform:generate-netbeans-project
or try the nbm plugin or Mevenide Netbeans for Maven 2 at http://mevenide.codehaus.org/m2-site/index.html
