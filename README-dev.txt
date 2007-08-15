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

----------------
Releasing Plugin
----------------

Before releasing check with team to make sure is ok and that they know you are releasing.

(where VERSION is the format major.minor like 2.0 or major.minor-alpha/beta-number like 2.0-alpha-1 and 2.0-beta-1. Please see http://svn.atlassian.com/svn/public/contrib/confluence/custom-space-user-management/tags/ )

(if needed) svn co https://svn.atlassian.com/svn/public/contrib/confluence/custom-space-user-management/
cd custom-space-user-management
cd trunk
mvn -Dmaven.test.skip=true clean install
ONLY PROCEED IF BUILD SUCCEEDS AND DO IT ONLY IMMEDIATELY AFTER A GOOD BUILD!
svn copy --username yourusername https://svn.atlassian.com/svn/public/contrib/confluence/custom-space-user-management/trunk https://svn.atlassian.com/svn/public/contrib/confluence/custom-space-user-management/tags/VERSION -m "Tagging VERSION"
(if needed) svn co https://svn.atlassian.com/svn/public/contrib/confluence/custom-space-user-management/
cd custom-space-user-management
cd tags
(if needed) svn up
cd VERSION
edit pom.xml (with unix EOL friendly text editor!)
change <version>...</version> to <version>VERSION</version> (again where VERSION is the version like 2.0-beta-1 for example)
save it
mvn -Dmaven.test.skip=true clean install
(if build didn't work, you are out of luck. start over and probably want to delete the TAG.)
svn commit (from tag dir) comment should be "Updated pom.xml for version VERSION"
cp target/(jar which should contain version in filename) ../releases/
svn add ../releases/(jarname you just copied)
svn commit ../releases
edit the plugin page at http://confluence.atlassian.com/display/CONFEXT/Custom+Space+User+Management+Plugin and change all references to point at latest jar release. Add comment to it to let people know there is a new release with a link to the built jar.

Notes:
* Follow the process above and don't make substitutions. For example, the maven build should ensure Java backwards compatibility, but if you build with IDea or something else, it might not.