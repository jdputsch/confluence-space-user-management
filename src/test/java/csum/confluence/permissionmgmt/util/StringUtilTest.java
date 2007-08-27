package csum.confluence.permissionmgmt.util;

import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Aug 27, 2007
 * Time: 2:36:25 PM
 */
public class StringUtilTest extends TestCase {

    public void test_clean() {
        assertEquals("Didn't clean expected chars", "abcdefgh", StringUtil.clean("ab<c>d</efg>h"));
    }

    public void test_isNullOrEmpty() {
        assertFalse("Should return false for 'a'", StringUtil.isNullOrEmpty("a"));
        assertTrue("Should return true for null", StringUtil.isNullOrEmpty(null));
        assertTrue("Should return true for empty", StringUtil.isNullOrEmpty(""));
    }

    public void test_getCleanedListFromDelimitedValueString() {
        List result = StringUtil.getCleanedListFromDelimitedValueString("a,b,c");
        assertEquals("Wrong number of values", 3, result.size());
    }

    public void test_convertCollectionToCommaDelimitedString() {
        Collection abc = new ArrayList();
        abc.add("a");
        abc.add("b");
        abc.add("c");
        String result = StringUtil.convertCollectionToCommaDelimitedString(abc);
        assertEquals("Unexpected content", "a, b, c", result);
    }

    public void test_areBothNullOrAreEqual() {
        assertFalse("Should return false for diff strings", StringUtil.areBothNullOrAreEqual("a","b"));
        assertFalse("Should return false for string and null", StringUtil.areBothNullOrAreEqual("c",null));
        assertFalse("Should return false for null and string", StringUtil.areBothNullOrAreEqual(null,"d"));
        assertTrue("Should return true for null", StringUtil.areBothNullOrAreEqual(null, null));
        assertTrue("Should return true for empty", StringUtil.areBothNullOrAreEqual("", ""));
        assertTrue("Should return true for empty", StringUtil.areBothNullOrAreEqual("abcd", "abcd"));
    }
}
