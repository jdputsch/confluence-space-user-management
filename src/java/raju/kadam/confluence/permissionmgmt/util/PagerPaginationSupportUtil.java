package raju.kadam.confluence.permissionmgmt.util;

import bucket.core.actions.PagerPaginationSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 16, 2007
 * Time: 11:51:30 AM
 */
public class PagerPaginationSupportUtil {

    public static final Log log = LogFactory.getLog(PagerPaginationSupportUtil.class);

    public static boolean hasNext(PagerPaginationSupport pps) {
        boolean result = false;

        log.debug("hasNext() called");
        debug(pps);
        if (pps!=null) {
            int[] startIndexes = pps.getNextStartIndexes();
            if (startIndexes!=null) {
                for (int i=(startIndexes.length-1); !result && i>=0; i--) {
                    int nextStartIndex = startIndexes[i];
                    log.debug("next start index " + nextStartIndex + "> start index " + pps.getStartIndex() + " ?");
                    if (nextStartIndex > pps.getStartIndex()) {
                        log.debug("setStartIndex to " + startIndexes[i]);
                        result = true;
                    }
                }
            }
            else {
                log.debug("nextStartIndexes was null");
            }
        }
        else {
            log.debug("next() shouldn't really be called with null. programming error");
        }

        log.debug("hasNext() returning " + result);

        return result;
    }

    public static void next( PagerPaginationSupport pps ) {
        log.debug("next() called");
        debug(pps);
        if (pps!=null) {
            int[] startIndexes = pps.getNextStartIndexes();
            if (startIndexes!=null) {
                boolean done = false;
                for (int i=0; !done && i<startIndexes.length; i++) {
                    int nextStartIndex = startIndexes[i];
                    log.debug("next start index " + nextStartIndex + "> start index " + pps.getStartIndex() + " ?");
                    if (nextStartIndex > pps.getStartIndex()) {
                        log.debug("setStartIndex to " + startIndexes[i]);
                        pps.setStartIndex(startIndexes[i]);
                        done = true;
                    }
                }
            }
            else {
                log.debug("nextStartIndexes was null");
            }
        }
        else {
            log.debug("next() shouldn't really be called with null. programming error");
        }
    }

    public static boolean hasPrev(PagerPaginationSupport pps) {
        boolean result = false;

        log.debug("hasPrev() called");
        debug(pps);
        if (pps!=null) {
            int[] startIndexes = pps.getNextStartIndexes();
            if (startIndexes!=null) {
                for (int i=(startIndexes.length-1); !result && i>=0; i--) {
                    int nextStartIndex = startIndexes[i];
                    log.debug("previous start index " + nextStartIndex + "< start index " + pps.getStartIndex() + " ?");
                    if (nextStartIndex < pps.getStartIndex()) {
                        log.debug("setStartIndex to " + startIndexes[i]);
                        result = true;
                    }
                }
            }
            else {
                log.debug("previousStartIndexes was null");
            }
        }
        else {
            log.debug("prev() shouldn't really be called with null. programming error");
        }

        log.debug("hasPrev() returning " + result + ".");

        return result;
    }

    public static void prev( PagerPaginationSupport pps ) {
        log.debug("prev() called");
        debug(pps);
        if (pps!=null) {
            int[] startIndexes = pps.getNextStartIndexes();
            if (startIndexes!=null) {
                boolean done = false;
                for (int i=(startIndexes.length-1); !done && i>=0; i--) {
                    int nextStartIndex = startIndexes[i];
                    log.debug("previous start index " + nextStartIndex + "< start index " + pps.getStartIndex() + " ?");
                    if (nextStartIndex < pps.getStartIndex()) {
                        log.debug("setStartIndex to " + startIndexes[i]);
                        pps.setStartIndex(startIndexes[i]);
                        done=true;
                    }
                }
            }
            else {
                log.debug("previousStartIndexes was null");
            }
        }
        else {
            log.debug("prev() shouldn't really be called with null. programming error");
        }
    }

    private static void debug( PagerPaginationSupport pps ) {
        StringBuffer sb = new StringBuffer();
        sb.append("PagerPaginationSupport debug:");
        if (pps!=null) {
            try {
                sb.append("\nCountOnEachPage=" + pps.getCountOnEachPage());
                sb.append("\nEndIndex=" + pps.getEndIndex());
                //doesn't do null check?
                //sb.append("\nNextIndex=" + pps.getNextIndex());
                sb.append("\nNextStartIndexes=" + intArrayToString(pps.getNextStartIndexes()));
                sb.append("\nNiceStartIndex=" + pps.getNiceStartIndex());
                //doesn't do null check
                //sb.append("\nPreviousIndex=" + pps.getPreviousIndex());
                sb.append("\nPreviousStartIndexes=" + intArrayToString(pps.getPreviousStartIndexes()));
                sb.append("\nStartIndex=" + pps.getStartIndex());
                sb.append("\nStartIndexValue=" + pps.getStartIndexValue());
                sb.append("\nTotal=" + pps.getTotal());
            }
            catch (Throwable t) {
                sb.append("failed at this point");
                log.error("Failed to debug PPS", t);
            }
        }
        else {
            sb.append(" PPS was null");
        }
        log.debug(sb.toString());
    }

    private static String intArrayToString(int[] ints) {
        StringBuffer sb = new StringBuffer();
        if (ints!=null) {
            for (int i=0; i<ints.length; i++) {
                if (i!=0) {
                    sb.append(", ");
                }
                sb.append( ints[i] );
            }
        }
        else {
            sb.append("null");
        }
        return sb.toString();
    }
}