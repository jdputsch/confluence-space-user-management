package raju.kadam.confluence.permissionmgmt.util;

/**
 * (c) 2007 Duke University
 * User: gary.weaver@duke.edu
 * Date: Jul 24, 2007
 * Time: 11:14:09 AM
 */
public class Range {

    private String text;
    private int recordNum;
    private boolean selected;


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getRecordNum() {
        return recordNum;
    }

    public void setRecordNum(int recordNum) {
        this.recordNum = recordNum;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
