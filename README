
General Information
===================

See http://confluence.atlassian.com/display/CONFEXT/Custom+Space+User+Management+Plugin


Directions for compiling using Maven 2
======================================

Here are some things I learned that might make it easier for those interested in using the Atlassian Plugin Developer Kit for Maven 2:

1) If you haven't already, download and install maven 2 from http://maven.apache.org/download.html and make sure your MAVEN_HOME and PATH are set as in the maven install documentation on their site.

2) If you haven't already, install maven 2 pdk plugin to your local maven repository. One one to do this is to build it (example below uses 2.1.1 tag which was the latest as of time of writing, but you may want to browse to the http://svn.atlassian.com/svn/public/contrib/maven-plugins/com.atlassian.maven.plugins/atlassian-pdk/tags/ to see what is the latest version available and adjust accordingly):

mkdir tmp
cd tmp
svn co http://svn.atlassian.com/svn/public/contrib/maven-plugins/com.atlassian.maven.plugins/atlassian-pdk/tags/2.1.1/
cd 2.1.1
mvn clean install

3) Go back to the trunk dir of the plugin project, so if you started off in that dir, you could just:

cd ../..

4) To build, and install to your local repository, use the following command. If problems building, see http://confluence.atlassian.com/display/DISC/Developing+Confluence+Plugins+with+Maven+2 for info on defining your pom.xml.

mvn clean install

5) After that go into confluence into administration -> plugins. Choose this project's plugin. If it already exists, uninstall the plugin from confluence and (re)install the new one in target/...jar. If the plugin fails to load for any reason, you probably don't have enough or have too many dependencies, so see http://confluence.atlassian.com/display/DISC/Developing+Confluence+Plugins+with+Maven+2 for info on defining your pom.xml.

Other helpful hints for those new to maven 2:

To generate an IDea project:
mvn idea:idea

To generate an Eclipse project:
mvn eclipse:eclipse

To build in Netbeans try Mevenide Netbeans for Maven 2 at http://mevenide.codehaus.org/m2-site/index.html
