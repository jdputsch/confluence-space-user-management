----------------
Joining the Team
----------------

If you are interested in contributing to the project, first try posting a comment to this specific plugin's page.

If no one responds within a reasonable timeframe and you cannot contact any of the team members listed in TEAM.txt and/or pom.xml,
then feel free to contact atlassian to get dev access to subversion and make changes.

-----------------
Development Rules
-----------------

Before you make any changes to the project, please read and accept the following guidelines:

1) All changes to the plugin must not be specific to any single organization that might be using confluence.

2) Make as much as you can configurable without going overboard.

3) Discuss changes with the rest of the team, even if it is post-commit.

4) Never check jars into source. Put jars in as compile scope if you need to add them in the pom.xml.

5) Never edit source in a tag other than editing pom.xml to change version. Do all development in trunk.

6) Use Sun coding conventions: http://java.sun.com/docs/codeconv/

7) If you change formatting make sure no one else is working in that class, and try to do that as a separate commit.

8) Good code should not need many comments. Write self-documenting code.

9) Tests should be able to run in any supported environment.

10) Have fun!

-------------------------------------
i18n/i10n/Customizing Labels/Messages
-------------------------------------

Internationalization, localalization, and customization of labels and messages is easy.

All a user has to do is define the properties they want to override in (confluence webapp directory)/WEB-INF/classes/csum/confluence/permissionmgmt/AtlassianPlugin_(locale).properties

As a developer, it is a little harder, but not too much. Any new labels or messages need to have entries in:
src/main/resources/csum/confluence/permissionmgmt/AtlassianPlugin.properties
and
src/main/resources/csum/confluence/permissionmgmt/AtlassianPlugin_en.properties

See:
* http://confluence.atlassian.com/display/DISC/i18n+in+Confluence+Plugin+Development
* http://java.sun.com/docs/books/tutorial/i18n/format/messageFormat.html
* http://java.sun.com/docs/books/tutorial/i18n/index.html
* http://java.sun.com/docs/books/tutorial/i18n/intro/checklist.html

----------------
Releasing Plugin
----------------

1) Before releasing check with team to make sure is ok and that they know you are releasing.

2) If you don't already have the whole plugin checked out (including all tags) you might want it to make this easier:
 svn co https://studio.plugins.atlassian.com/svn/SUSR/

3) svn up in trunk
 cd trunk
 svn up

4) Build trunk:
 mvn -Dmaven.test.skip=true clean install

5) Deploy plugin and make sure it works

6) svn up in trunk and build again (it could have changed while you were testing)
 svn up
 mvn -Dmaven.test.skip=true clean install

7) Immediately after successful build, tag it (replace instances of username and version with appropriate values):
 svn copy --username (username) https://studio.plugins.atlassian.com/svn/SUSR/trunk https://studio.plugins.atlassian.com/svn/SUSR/tags/(version) -m "Tagging (version)"

8) Get tag:
 cd ../tags
 svn up

9) Change the pom.xml version element in the tag to (version) you just tagged

10) Commit the tag.
 svn commit

11) Build the tag:
 mvn -Dmaven.test.skip=true clean install

12) Copy tag to releases, add and commit
 cp target/...jar ../releases/
 svn add ../releases/....jar
 svn commit ../releases/

13) Edit http://confluence.atlassian.com/display/CONFEXT/Custom+Space+User+Management+Plugin and change all references to point at latest jar release. Add comment to it to let people know there is a new release with a link to the built jar.

14) Add release in https://plugins.atlassian.com/manage/plugin/133

15) Add release and set release info in tickets in Jira

16) Update pom.xml in trunk to increment version to next snapshot version.

17) Follow the process above and don't make substitutions or changes. For example, the maven build should ensure Java backwards compatibility, but if you build with IDea or something else, it might not. Also, note that it is intentional that the tag's pom is changed and that it is not changed in trunk. Trunk's pom should always have SNAPSHOT in the version of the pom.

-------
Reports
-------

Thanks to Andy we have a site.xml and pom.xml changes to support it

To produce site use:
 mvn -Dmaven.test.skip=true clean site

If you are Andy or you have graphviz installed http://www.graphviz.org and have uncommented the pom.xml and added a local repository to the pom that contains the CSUM plugin, then you can do this:
 mvn depgraph:depgraph
