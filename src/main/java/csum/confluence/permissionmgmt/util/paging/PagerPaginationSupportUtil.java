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

package csum.confluence.permissionmgmt.util.paging;

import bucket.core.actions.PagerPaginationSupport;
import csum.confluence.permissionmgmt.util.logging.LogUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Gary S. Weaver
 */
public class PagerPaginationSupportUtil {

    public static final Log log = LogFactory.getLog(PagerPaginationSupportUtil.class);


    public static Integer getStartIndexAsIntegerOrNull(PagerPaginationSupport pps) {
        Integer result = null;
        if (pps != null) {
            result = new Integer(pps.getStartIndex());
        }
        return result;
    }

    /**
     * Notes: this returns an unsorted list (index of items gets messed up due to use of HashMap to ensure same object
     * is not added to List more than once). It is awful than PagerUtils.toList(pps.getItems()) doesn't work. Need to
     * submit a bug. This does way too many queries by having to query for each page.
     *
     * @param pps
     * @return List
     */
    public static List toList(PagerPaginationSupport pps) {
        //TODO: it is awful than PagerUtils.toList(pps.getItems()) doesn't work. submit a bug. this does way too many queries            

        log.debug("toList() called");
        List result = null;
        if (pps != null) {
            //TODO: may be being paranoid by using Map here. consider not using if not needed
            // using map hopefully to avoid any chance of adding the same items more than once
            Map itemsAsKeysMap = new HashMap();
            int originalPageStartIndex = pps.getStartIndex();

            // start with first page
            pps.setStartIndex(0);
            log.debug("converting PagerPaginationSupport to List. at index=0");
            addAllAsKeys(pps.getPage(), itemsAsKeysMap);

            // get other pages
            int[] startIndexes = pps.getNextStartIndexes();
            if (startIndexes != null && startIndexes.length > 0) {
                for (int i = 0; i < startIndexes.length; i++) {
                    int nextStartIndex = startIndexes[i];
                    log.debug("converting PagerPaginationSupport to List. at index=" + nextStartIndex);
                    pps.setStartIndex(nextStartIndex);
                    addAllAsKeys(pps.getPage(), itemsAsKeysMap);
                }
            }

            result = new ArrayList(itemsAsKeysMap.keySet());
            ;
            if (result.size() != pps.getTotal()) {
                LogUtil.warnWithRemoteUserInfo(log, "Got incorrect number of items in toList()! Expected " + pps.getTotal() + " but got " + result.size());
            } else {
                log.debug("Got " + result.size() + " items from pps. This was same as pps.getTotal() so hopefully all is ok.");
            }
        }
        return result;
    }

    public static List getRanges(PagerPaginationSupport pps, int roughNumberOfRanges) {
        log.debug("getRanges() called. roughNumberOfRanges=" + roughNumberOfRanges);
        List ranges = null;
        int numBeforeAndAfter = roughNumberOfRanges / 2;
        if (pps != null && pps.getTotal() > pps.getCountOnEachPage()) {
            int[] startIndexes = pps.getNextStartIndexes();
            if (startIndexes != null && startIndexes.length > 0) {
                ranges = new ArrayList();

                // ranges before current
                int currStartIndex = pps.getStartIndex();
                int lastCurrStartIndex = currStartIndex;
                for (int i = 0; i < numBeforeAndAfter; i++) {
                    currStartIndex = currStartIndex - pps.getCountOnEachPage();
                    if (currStartIndex < 0 && lastCurrStartIndex != 0) {
                        currStartIndex = 0;
                    }

                    if (currStartIndex >= 0) {
                        Range range = new Range();
                        range.setRecordNum(currStartIndex + 1);
                        range.setText("" + (currStartIndex + 1) + "-" + lastCurrStartIndex);
                        ranges.add(range);
                    }

                    lastCurrStartIndex = currStartIndex;
                }

                // current
                ranges.add(getCurrentRange(pps));

                // ranges after current
                int currEndIndex = pps.getStartIndex() + pps.getCountOnEachPage();
                int lastCurrEndIndex = currEndIndex;
                int maxIndex = pps.getTotal() - 1;
                for (int i = 0; i < numBeforeAndAfter; i++) {
                    currEndIndex = currEndIndex + pps.getCountOnEachPage();
                    if (currEndIndex > maxIndex && lastCurrEndIndex != maxIndex) {
                        currEndIndex = maxIndex;
                    }

                    if (currEndIndex <= maxIndex) {
                        Range range = new Range();
                        range.setRecordNum(lastCurrEndIndex + 1);
                        range.setText("" + (lastCurrEndIndex + 1) + "-" + currEndIndex);
                        ranges.add(range);
                    }

                    lastCurrEndIndex = currEndIndex;
                }
            } else {
                log.debug("nextStartIndexes was null");
            }
        } else {
            log.debug("safelyMoveToPageStartIndexClosestToIndex() shouldn't really be called with null. programming error");
        }

        return ranges;
    }

    private static Range getCurrentRange(PagerPaginationSupport pps) {
        int lastRecordNumOfPage = pps.getStartIndex() + pps.getCountOnEachPage();
        if (lastRecordNumOfPage > pps.getTotal()) {
            lastRecordNumOfPage = pps.getTotal();
        }
        Range range = new Range();
        range.setRecordNum(pps.getStartIndex() + 1);
        range.setText("" + (pps.getStartIndex() + 1) + "-" + lastRecordNumOfPage);
        return range;
    }

    public static void sortRangesByRecordNumAscending(List ranges) {
        Collections.sort(ranges, new RangeComparator());
    }

    private static void addAllAsKeys(List list, Map map) {
        if (list != null && map != null) {
            for (int i = 0; i < list.size(); i++) {
                map.put(list.get(i), "");
            }
        }
    }

    public static void safelyMoveToOldStartIndex(Integer startIndex, PagerPaginationSupport pps) {
        if (startIndex != null) {
            safelyMoveToOldStartIndex(startIndex.intValue(), pps);
        }
    }

    /**
     * If you get pps.getStartIndex() and remove a lot of records, this will ensure you can get back to the location
     * as close to the original index as possible to avoid disorientation.
     */
    public static void safelyMoveToOldStartIndex(int startIndex, PagerPaginationSupport pps) {
        log.debug("safelyMoveToOldStartIndex() called. startIndex=" + startIndex);
        if (pps != null) {
            int closestStartIndex = startIndex;
            int maxIndex = pps.getTotal() - 1;
            if (closestStartIndex > maxIndex) {
                closestStartIndex = maxIndex;
            } else if (closestStartIndex < 0) {
                closestStartIndex = 0;
            }

            pps.setStartIndex(closestStartIndex);
        } else {
            log.debug("safelyMoveToOldStartIndex() shouldn't really be called with null. programming error");
        }
    }

    /**
     * If you get pps.getStartIndex() and remove a lot of records, this will ensure you can get back to the location
     * as close to the original index as possible to avoid disorientation.
     */
    public static void safelyMoveToPageStartIndexClosestToIndex(int startIndex, PagerPaginationSupport pps) {
        log.debug("safelyMoveToPageStartIndexClosestToIndex() called. startIndex=" + startIndex);
        if (pps != null) {
            int[] startIndexes = pps.getNextStartIndexes();
            if (startIndexes != null && startIndexes.length > 0) {
                // 0 is a valid start index not included in nextStartIndexes
                int closestStartIndex = 0;
                for (int i = 0; i < startIndexes.length; i++) {
                    int nextStartIndex = startIndexes[i];
                    int oldAbsoluteDifference = Math.abs(startIndex - closestStartIndex);
                    int absoluteDifference = Math.abs(startIndex - nextStartIndex);

                    //log.debug("is startIndex=" + startIndex + " closer to " + closestStartIndex + " or " + nextStartIndex + " ?");

                    if (absoluteDifference < oldAbsoluteDifference) {
                        //log.debug("now using " + nextStartIndex + " as closest index");
                        closestStartIndex = nextStartIndex;
                    }
                }

                pps.setStartIndex(closestStartIndex);
            } else {
                log.debug("nextStartIndexes was null");
            }
        } else {
            log.debug("safelyMoveToPageStartIndexClosestToIndex() shouldn't really be called with null. programming error");
        }
    }

    public static boolean hasNext(PagerPaginationSupport pps) {
        boolean result = false;

        //log.debug("hasNext() called");
        debug(pps);
        if (pps != null) {
            if (pps.getStartIndex() < (pps.getTotal() - pps.getCountOnEachPage())) {
                result = true;
            }
        } else {
            log.debug("next() shouldn't really be called with null. programming error");
        }

        log.debug("hasNext() returning " + result);

        return result;
    }

    public static void next(PagerPaginationSupport pps) {
        log.debug("next() called");
        debug(pps);
        if (pps != null) {
            if (hasNext(pps)) {
                pps.setStartIndex(pps.getStartIndex() + pps.getCountOnEachPage());
            }
        } else {
            log.debug("next() shouldn't really be called with null. programming error");
        }
    }

    public static boolean hasPrev(PagerPaginationSupport pps) {
        boolean result = false;

        //log.debug("hasPrev() called");
        debug(pps);
        if (pps != null) {
            // 0 is the lowest valid start index
            if (pps.getStartIndex() > 0) {
                result = true;
            }
        } else {
            log.debug("prev() shouldn't really be called with null. programming error");
        }

        log.debug("hasPrev() returning " + result + ".");

        return result;
    }

    public static void prev(PagerPaginationSupport pps) {
        log.debug("prev() called");
        debug(pps);
        if (pps != null) {
            if (pps.getStartIndex() > 0) {
                int newStartIndex = pps.getStartIndex() - pps.getCountOnEachPage();
                if (newStartIndex < 0) {
                    newStartIndex = 0;
                }
                pps.setStartIndex(newStartIndex);
            }
        } else {
            log.debug("prev() shouldn't really be called with null. programming error");
        }
    }

    public static int getPageEndIndex(PagerPaginationSupport pps) {
        int pageEndIndex = pps.getStartIndex() + pps.getCountOnEachPage() - 1;
        if (pageEndIndex > (pps.getTotal() - 1)) {
            pageEndIndex = pps.getTotal() - 1;
        }
        return pageEndIndex;
    }

    private static void debug(PagerPaginationSupport pps) {

        /*
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
                LogUtil.errorWithRemoteUserInfo(log, "Failed to debug PPS", t);
            }
        }
        else {
            sb.append(" PPS was null");
        }
        log.debug(sb.toString());
        */
    }

    private static String intArrayToString(int[] ints) {
        StringBuffer sb = new StringBuffer();
        if (ints != null) {
            for (int i = 0; i < ints.length; i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(ints[i]);
            }
        } else {
            sb.append("null");
        }
        return sb.toString();
    }
}
