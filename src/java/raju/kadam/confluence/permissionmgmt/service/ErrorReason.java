package raju.kadam.confluence.permissionmgmt.service;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jun 22, 2007
 * Time: 10:22:59 AM
 */
public interface ErrorReason {

    public static final String USER_NOT_FOUND = "User(s) not found";
    public static final String INVALID_GROUP_NAME = "Group name(s) invalid";
    public static final String UNSUPPORTED_FEATURE = "This feature is currently unsupported";
    public static final String INVALID_USER_MANAGER_LOCATION = "Please set User Manager Location in configuration";
    public static final String ACTION_NOT_SELECTED = "Please select an action";
}
