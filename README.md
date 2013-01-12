Confluence Space User Management Plugin
=====

The CSUM plugin gives Space Administrators ability to manage users and user-groups associated to their Atlassian Confluence Wiki Spaces.

Using this plugin Space Administrators can:

* Create / delete user-groups that can be space-specific (optional)
* Add / remove users from user-groups associated to given wiki space
* Create new users automatically as part of their addition to a user-group (optional, and provided that LDAP is available to fetch user details needed to create user account)
* View list of users for selected user-group
* Search users to add / remove from selected user-group (optional)
* Create users and user-groups in Jira (optional, and provided that you are using Jira for Confluence user management)
* Create a user-group with a comma-delimited list of users in a single operation (optional)
* Perform bulk add / remove of users from user-group(s)
* Full control over UI messages for localization and internationalization (i10n/i18n). You can also just use this to tweak your messages for your environment.

PLEASE NOTE: CSUM can only be used to manage users and groups in a read-write repository via the Confluence API. If you are using a read-only LDAP repository or similar as your primary repository, even though it supports using LDAP to validate usernames as a secondary repository, it will not be able to manage users/groups if the access to that repository via the Confluence API is read-only.

### Installation

This plugin can be downloaded and installed within [Atlassian Confluence][atlassian_confluence]. Newer releases are hosted with [Atlassian Marketplace][atlassian_marketplace]. All releases are stored in the releases branch of this repository, as a backup.

Please check the CSUM plugin release compatibility matrix in [Atlassian Marketplace][atlassian_marketplace] to determine what CSUM version works with which version of Confluence. Please report any incompatibility issues in this GitHub project's [issues][issues] section.

### Configuration

There should be a link to configure the plugin in the UI in Confluence after you install it. The plugin may need be configured appropriately before it will work properly.

* User Manager Location - use Confluence unless you are using Jira for Confluence User Management (see [Understanding User Management in Confluence] for more information)
* If you are using Jira for User/Group Managment, you will need to:
  * Specify the JIRA SOAP Service Endpoint URL (for example: http://yourjiraserverhostname/rpc/soap/jirasoapservice-v2 )
  * Specify a username and password (be sure to check the checkbox to the left of "Set Password" to set it) for an admin user in Jira that you want to use for user/group management.
  * It will attempt to test the connection to Jira, so you'll need to make sure that the SOAP service is enabled in Jira configuration.
* If you want to use LDAP to get info for users in order to autoprovision them if they don't already exist, set "Autoprovision Users Using LDAP Info" to YES. Otherwise, set it to NO.
* If "Autoprovision Users Using LDAP Info" is set to YES, you'll need to:
  * Set provider type (whether it should get LDAP info from osuser.xml or atlassian-user.xml)
  * Attribute names (talk to your LDAP person to get the right values)
  * Set LDAP provider fully qualified classname. This may just be "com.opensymphony.user.provider.ldap.LDAPCredentialsProvider".
* Unless you don't want space admins to create/delete groups that match the prefix/suffix specified, set Group Actions Permitted to YES.
* New Group Name Creation Pattern takes the place of the old rexexp pattern from v1.0 such that you specify a groupname prefix and/or groupname suffix for all groups to be manageable by space admins.
* Max number of user ids/group ids - this just keeps a space admin from trying to add tons of users and groups at once
* User Search Enabled - you'll want this to be YES because it really helps space admins when they only know the first/last name or email of the users they need to add (for example), unless:
  * You don't want space admins seeing all users in Confluence (the only info it gives about the user is username, fullname, and email though).
  * You have a ton of users (tens of thousands) and are using the old osuser schema instead of the new atlassian-user schema. (because the osuser schema is really slow for that many users)
* Personal Space User/Group Administration Allowed - choose NO if in doubt. You probably don't want users to be able to manage their own groups for personal spaces.
* Plugin Deactivated/Downtime Message - at first you may think "hey. doesn't confluence already have another mechanism to disable plugins?" However, this may be useful for some, because it allows you to specify a downtime message, which you can't currently do via normal plugin disabling.

Here are the CSUM Tasks need to perform in order to integrate Crowd with CSUM:

* Admin setting for Crowd Connector integration parameters (as needed)
* Admin setting for choosing to create groups in Jira/Confluence or via Crowd Connector
* Admin setting to set a limit on the number of groups than can be created for the space

#### Configuration Troubleshooting

* If you have problems with Jira integration and you are sure that it is not the connection/authentication but is an actual integration issue, it could be that the webservice definition of your Jira is incompatible with the plugin. If there is a newer version of the plugin available, please update it and see if that works. Otherwise, download a copy of the wsdl from your jira from (jiraurl)/rpc/soap/jirasoapservice-v2?wsdl and attaching that along with logs to a jira ticket for this plugin and we'll see what we can do. You can also try doing a diff of your wsdl to the jira.wsdl in plugin trunk to see what is different, and even try just checking out trunk, replacing the wsdl, and rebuilding, and deploying target/(jar name) to see if just a simple rebuild works.

### Customizing

* English and German translations are included, but if you wish to provide alternate translations or change text or messages in the plugin, create a file called AtlassianPlugin_en.properties (substitute your locale for 'en' in filename) containing the properties you wish to override, and place this file in all server nodes into (confluence webapp directory)/WEB-INF/classes/csum/confluence/permissionmgmt/. You only need to include the properties that you want to override. The properties which you can override can be found in src/main/resources/csum/confluence/permissionmgmt/AtlassianPlugin_en.properties in whichever release tag you are using. If you would like to provide a translation, please feel free to fork the project and contribute.
* We did some clean-up of the i18n properties names in 2.0 (final), so be aware that you'll need to update the key names in the property file after installing it, if you had previously customized them. This was necessary to help avoid conflicts with other plugins' i18n properties. For those with earlier dev versions of the plugin, there were also key changes between 2.0-alpha4 and 2.0-alpha5.
* Our aim is that this plugin work acceptably well in almost any theme you could practically use. If it doesn't, please suggest the fix we could implement and attach the theme plugin to the ticket.
* If you have any suggestions to help you customize the plugin to use it more effectively or suggestions for better internationalization support, please let us know.

### Usage

* As space administrator or confluence administrator, choose a space from the dashboard or just click on the globe icon next to the space in the dashboard and go to Browse Space -> Manage Users/Groups (tab)
* You can select a group here if there are groups that match space pattern defined in config that have viewspace permission.
  * If group actions are allowed in plugin config, you can: add a group, add a group and specify users, create multiple groups (enter a comma-delimited list of group names).
  * If you create a new group, the CSUM plugin gives the created group the "view" right to the space. You can manage permissions either by clicking on "edit space permissions" link at the bottom, or by going to space admin (tab) -> permissions.
    Select a group (create one first if there is not one already- don't worry- you can delete it)
    You can add users to groups by entering a comma-delimited list of usernames (ids). When a user is added to a group, one of the situations must apply:
    * The user must either exist according to Confluence (and Confluence may optionally in turn be using some other source like LDAP, Jira, Crowd, etc. for user management)
    * OR the user must exist in Jira (if plugin configured to use Jira's webservice for user management)
    * OR if the plugin is configured to use LDAP to gather user information for user creation, then:
      * If the user doesn't exist already in Confluence or Jira (depending on the "user manager" you choose in the plugin config) when you are adding a user to a group, it will lookup that user by username in LDAP, using Confluence's LDAP configuration supplied in atlassian-user.xml or osuser.xml. Notes: (1) The order of things in atlassian-user.xml and osuser.xml matter, as it will search for users in repositories in that order. If you don't want it to hit LDAP for users, but use the Confluence user tables instead, put the LDAP repository second in that config file. (2) Using LDAP for this plugin doesn't require you to be using LDAP for anything else in Confluence, but there's no problem if you are as long as you pay attention to point #1. (3) If you'd like the plugin to be able to use LDAP config from some other config other than atlassian-user.xml or osuser.xml or would like it to use some other source other than LDAP for user info for user creation, feel free to request an enhancement or better yet modify it yourself and see the contribution section about how to contribute that functionality, but please first look into whether Confluence can integrate with whatever authN/authR/SSO you would like it to use, since that is likely much easier.
      * It will then create that user in Confluence or Jira (depending on the "user manager" you choose in the plugin config)
      * It will then add that new user to the group specified.
  * You can remove users from the selected group by clicking on trash can.
  * If user search is allowed in plugin config, click on directory search link.
  * You can lookup users from entire userlist here and add/remove them from the selected group.
  * Click bulk actions link.
  * Here you can add/remove users from groups more quickly if you know the usernames (ids).

Here is some information that might help if you're trying to decide how to configure CSUM. Sorry if some of it is a repeat:

  * The CSUM plugin doesn't care what you are using for user management, unless you are using Jira for user management (in which case, you should configure it to use Jira for user management). So feel free to use Crowd, LDAP, etc. The reason for this is that the CSUM plugin uses Confluence's own user API, which basically should (in theory) let the CSUM plugin use whatever Confluence uses for a repository.
  * You don't need to configure the plugin for LDAP unless you want the CSUM plugin to automagically know about users that Confluence doesn't know about when you are trying to add those users to a group (or create a group with those users). If you configure CSUM to use LDAP for this purpose only when it can't find the users via the Confluence API, it will assume that you have an LDAP repository configured (and not commented-out) in atlassian-user.xml/osuser.xml. Note that if you do decide you want to use LDAP as a backup method of adding new users that don't already exist in Confluence when you add them to a group/create a group containing those users then be aware that Confluence itself will try to use the repositories in the order listed in atlassian-user.xml/osuser.xml. So if Confluence already has that LDAP repo listed, even if secondary, then why can't Confluence just see the users in LDAP? It's a good question, and the only answer I can provide at the moment is "don't ask". Maybe someone else can provide a better reason.
  * Do not configure the plugin with LDAP configuration (using atlassian-user/osuser) if you don't have an LDAP repository setup in atlassian-user.xml/osuser.xml, because it won't work.
  * There is no reason you have to use LDAP configuration in order to use CSUM, as long as Confluence already contains all of the users you would want to add to a group.

### How to add CSUM into Space Admin menu of Builder Theme

To add this plugin into the Space Admin menu of the Builder Theme use:

      {builder-show:permission=spaceadmin,siteadmin}
      {compound-menuitem:webui|location=system.space|key=space-custom-usermanagement|icon=users4_add|caption=Group Management|flat=true}
      {builder-show}

### Gotchas

* If a user is put into a group, and permissions are given to the group to allow that group to administer the space, and that group is manageable via space admin, then if the user removes the group that gives them rights to manage the group, the user will get a "you can't do that" screen after the operation has completed. This is expected behavior, but maybe not intuitive. It is not a good idea to restrict users from being able to do this, because there is no easy way to determine that the user will definitely lose access to manage if he/she were to delete the group.
* LDAP in the configuration page of the plugin is there so that it can create users in either Confluence or Jira (depending on the "user manager" you choose in the plugin config) if they don't already exist when you try to add them to a group. It basically uses an LDAP lookup to get the email and full name of the user as reported by LDAP (with configurable settings for which LDAP attribute names these two should come from). This autoprovisioning of users as they are added to space groups using LDAP is also helpful if you are using LDAP for passwords auth only, although it isn't necessary. The point of confusion here is that if you have configured Confluence to use LDAP as the source of all users then you may not need to configure LDAP within the plugin (it won't hurt anything to have it though).
* Rajendra tested the CSUM plugin with Jira and the only issue appears to be that Confluence itself takes a little while to get changes to Jira groups in his environment. That being said we are not actively supporting CSUM integration with Jira.
* 1.x version is no longer actively supported. If you are using Confluence 2.3 or higher, please try upgrading to 2.x of the plugin.
* CSUM v2.0.x requires Java 1.5 or later. CSUM v2.1+ requires Java 1.6 or later. Dependencies of the plugin require these versions.

### Troubleshooting

If you have any trouble and can't find the answers you need on this page and in the long comments trail, please:

* Go to [issues][issues].
* Search for the bug to make sure it isn't already there
* If a bug exists, then feel free to watch it or comment on it
* If there is no bug already then continue with the following steps:
   * Setup confluence user management debug logging using the config information here: http://confluence.atlassian.com/display/DOC/Requesting+External+User+Management+Support
   * Edit that same logging config (in many versions, I think it is the .../confluence/WEB-INF/classes/log4j.properties file), add the following line and restart Confluence: (This assumes that you have "log4j.appender.confluencelog=org.apache.log4j.ConsoleAppender" defined above it, otherwise basically do whatever you need to to enable debug logging for that package.)

            log4j.logger.csum=DEBUG, confluencelog

   * Ensure that you start capturing logging before you reproduce the issue and then copy the log immediately after you reproduce the issue.
   * The log containing debug information for the plugin and Confluence is in (confluence.home)/logs/atlassian-confluence.log, not (confluence.install.directory)/logs/.
   * Starting to tail the log via:

            tail -f (confluence.home)/logs/atlassian-confluence.log | ~/error.log

   * Stop immediately after you reproduce the problem may produce the most efficient log to debug the issue, unless the log rolls over while you are tailing.
   * Ensure that it has captured debug logging. Preferably reproduce the issue in a test environment that is low traffic.
   * Be sure to scrub any private information from the logs, because when you include it in your issue, as it will be public.
   
### Development

Get Involved

* Report [issues][issues].
* Fork and do a pull request for anything you'd like to contribute.
* See [README-dev][readme-dev] for further information.

### Contributors

* Rajendra Kadam
* Gary S. Weaver
* Ren Provey
* Andy Brook
* Igor Minar
* Christian Nesemann
* Tom Saathoff

Thanks also to everyone else that has contributed to, tested, and/or used CSUM!

### Release History

* 2.2.1.1 (#33) - 12 Dec 2012 - Stable
* 2.2.1 (#23) - 29 Jun 2011 - Stable
* 2.1.1 (#22) - 07 Feb 2011 - Stable
* 2.1.0 (#21) - 06 Feb 2011 - Stable
* 2.0.6 (#20) - 24 Oct 2010 - Stable
* 2.0.5 (#19) - 17 Aug 2009 - Stable
* 2.0.4 (#18) - 03 Jun 2009 - Stable
* 2.0.3 (#17) - 12 Apr 2009 - Stable
* 2.0.2 (#16) - 09 Jul 2008 - Stable
* 2.0.1 (#15) - 09 Jul 2008 - Stable
* 2.0 (#14) - 17 Feb 2008 - Stable
* 2.0-rc-3 (#13) - 17 Oct 2007 - Not Stable
* 2.0-rc-2 (#12) - 09 Oct 2007 - Not Stable
* 2.0-rc-1 (#11) - 01 Oct 2007 - Not Stable
* 2.0-beta-2 (#10 - 17 Sep 2007 - Not Stable
* 2.0-beta-1 (#9) - 05 Sep 2007 - Not Stable
* 1.1 (#3) - 03 Oct 2006 - Stable

### License

Copyright (c) 2007-2013, Custom Space User Management Plugin Development Team, released under a [BSD-style License][lic].

[atlassian_confluence]: http://www.atlassian.com/software/confluence/_
[atlassian_marketplace]: https://marketplace.atlassian.com/plugins/raju.kadam.confluence.permissionmgmt
[issues]: https://github.com/sillycat/confluence-space-user-management/issues
[readme-dev]: https://github.com/sillycat/confluence-space-user-management/blob/master/README-dev.md
[git]: http://git-scm.com/
[lic]: http://github.com/sillycat/confluence_space_user_management/blob/master/LICENSE
