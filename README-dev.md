CSUM - Joining the Team
=====

If you are interested in contributing to the project, feel free to fork and contribute back. Or, you can create an issue and include a patch or gist.

### Development Rules

Before you make any changes to the project, please read and accept the following guidelines:

1) All changes to the plugin must not be specific to any single organization that might be using Confluence.

2) Make as much as you can configurable without going overboard.

3) Discuss changes with the rest of the team, even if it is post-commit.

4) Never check jars into source. Put jars in as compile scope if you need to add them in the pom.xml.

5) Never edit source in a tag other than editing pom.xml to change version. Do all development in trunk.

6) Use Sun coding conventions: http://java.sun.com/docs/codeconv/

7) If you change formatting make sure no one else is working in that class, and try to do that as a separate commit.

8) Good code should not need many comments. Write self-documenting code.

9) Tests should be able to run in any supported environment.

10) Have fun!

### i18n/i10n/Customizing Labels/Messages

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

### Releasing Plugin

Past releases were done in Atlassian's hosted repositories in subversion, so we have a lot of branches in Git. Now we're just going to Git tag releases after we change the version and build. Here is the new process:

1) Before releasing check with the team to make sure is ok and that they know you are releasing.

2) Build master:

    atlas-mvn -Dmaven.test.skip=true clean install

3) Manually install the built jar and make sure it works

4) Git pull in master and build again (it could have changed while you were testing)

5) Immediately after successful build, update the pom.xml to a stable version (remove -SNAPSHOT), add and commit pom.xml:

    emacs pom.xml
    git add pom.xml
    git commit -m "updated version to 1.2.3"

6) Build again, then add the release and tag it:

    atlas-mvn -Dmaven.test.skip=true clean install
	cp target/(jar name) releases
	git add releases/(jar name)
    git push
    git tag v1.2.3
    git push --tags

7) Immediately update pom.xml to the next snapshot version (next minor version is ok):

    emacs pom.xml
    git add pom.xml
    git commit -m "updated version to 1.2.4-SNAPSHOT"
    git push

8) Close any relevant issues with comment like "Released in v1.2.3".

### Build Reports

Thanks to Andy we have a site.xml and pom.xml changes to support it. We aren't building or storing these currently.

To produce site use:

    atlas-mvn -Dmaven.test.skip=true clean site

If you are Andy or you have graphviz installed http://www.graphviz.org and have uncommented the pom.xml and added a local repository to the pom that contains the CSUM plugin, then you can do this:

    atlas-mvn depgraph:depgraph
