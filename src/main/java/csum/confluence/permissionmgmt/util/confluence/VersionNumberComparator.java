package csum.confluence.permissionmgmt.util.confluence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Comparator;

/**
 * @author Gary S. Weaver
 */
public class VersionNumberComparator implements Comparator {

    private static Log log = LogFactory.getLog(ConfluenceUtil.class);

    /**
     * Compares two version numbers.
     *
     * @param version
     * @param version1
     * @return Returns -1 if version less than version1, 0 if version same as version1, 1 is version greater than version1.
     */
    public int compare(Object version, Object version1) {

        if (!(version instanceof String && version1 instanceof String)) {
            return 0;
        }

        // for now just strip everything after the first non-digit or dot
        String workingVersion = stripEverythingAfterFirstNonDigitOrDotversion((String)version);
        String workingVersion1 = stripEverythingAfterFirstNonDigitOrDotversion((String)version1);

        String[] verPartAndRest = ifNotNullThenSplitStringAtFirstDot(workingVersion);
        String[] ver1PartAndRest = ifNotNullThenSplitStringAtFirstDot(workingVersion1);

        String verPart = ifNotNullThenTrimStringAndNullifyIfTrimmedStringIsEmpty(verPartAndRest[0]);
        String ver1Part = ifNotNullThenTrimStringAndNullifyIfTrimmedStringIsEmpty(ver1PartAndRest[0]);
        String verRest = verPartAndRest[1];
        String ver1Rest = ver1PartAndRest[1];

        if (verPart==null && ver1Part!=null) {
            return -1;
        }
        else if (verPart!=null && ver1Part==null) {
            return 1;
        }
        else if (verPart==null) {
            return 0;
        }
        else {
            int i = Integer.parseInt(verPart);
            int i1 = Integer.parseInt(verPart);
            if (i<i1) {
                return -1;
            }
            else if (i>i1) {
                return 1;
            }
        }

        if (verRest==null && ver1Rest!=null) {
            return -1;
        }
        else if (verRest!=null && ver1Rest==null) {
            return 1;
        }
        else if (verRest==null) {
            return 0;
        }

        return compare(verRest, ver1Rest);
    }

    private String stripEverythingAfterFirstNonDigitOrDotversion(String s) {
        StringBuffer sb = new StringBuffer();
        boolean foundFirstChar = false;
		for (int i=0; i<s.length() && !foundFirstChar; i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                sb.append(c);
            }
			else {
			    foundFirstChar = true;
			}
        }
        return sb.toString();
    }

    private String[] ifNotNullThenSplitStringAtFirstDot(String s) {
        String[] result = new String[2];
        if (s!=null) {
            result[0] = s;
            int indexOfFirstDot = s.indexOf('.');
            if (indexOfFirstDot!=-1) {
                result[0] = s.substring(0, indexOfFirstDot);
                result[1] = s.substring(indexOfFirstDot + 1, s.length());
            }
        }
        return result;
    }

    private String ifNotNullThenTrimStringAndNullifyIfTrimmedStringIsEmpty(String version) {
        if (version!=null) {
            version = version.trim();
            if ("".equals(version)) {
                version = null;
            }
        }

        return version;
    }
}
