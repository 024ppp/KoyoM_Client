package com.example.administrator.koyom_client;

/**
 * Created by Administrator on 2017/05/25.
 */

public enum ProcessCommand {
    COMMAND_LENGTH("COMMAND_LENGTH", 3),
    SAG("SAG", 1001),
    KIK("KIK", 1002),
    KOB("KOB", 1003),
    WAK("WAK", 1004),
    AMI("AMI", 1005),
    UPD("UPD", 1006),
    ERR("ERR", 9999)
    ;

    private final String text;
    private final int id;

    private ProcessCommand(final String text,  final int id) {
        this.text = text;
        this.id = id;
    }

    public String getString() {
        return this.text;
    }

    public int getInt() {
        return this.id;
    }

    //COMMAND_LENGTHからの呼び出しにのみ対応
    public String getCmdText(String txt) {
        String cmd = "";
        if (this.text.equals("COMMAND_LENGTH")) {
            cmd = txt.substring(0, this.id);
        }
        return cmd;
    }

    public String getExcludeCmdText(String txt) {
        String excmd = "";
        if (this.text.equals("COMMAND_LENGTH")) {
            excmd = txt.substring(this.id);
        }
        return excmd;
    }

    public boolean isSameString(String txt) {
        if (this.text.equals(txt)) {
            return true;
        }
        else {
            return false;
        }
    }
}
