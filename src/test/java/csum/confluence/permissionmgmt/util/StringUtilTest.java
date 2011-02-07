/**
 * Copyright (c) 2007-2011, Custom Space User Management Plugin Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Custom Space User Management Plugin Development Team
 *       nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package csum.confluence.permissionmgmt.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Gary S. Weaver
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
        assertFalse("Should return false for diff strings", StringUtil.areBothNullOrAreEqual("a", "b"));
        assertFalse("Should return false for string and null", StringUtil.areBothNullOrAreEqual("c", null));
        assertFalse("Should return false for null and string", StringUtil.areBothNullOrAreEqual(null, "d"));
        assertTrue("Should return true for null", StringUtil.areBothNullOrAreEqual(null, null));
        assertTrue("Should return true for empty", StringUtil.areBothNullOrAreEqual("", ""));
        assertTrue("Should return true for empty", StringUtil.areBothNullOrAreEqual("abcd", "abcd"));
    }
}
