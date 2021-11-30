package com.bignerdranch.android.myapplication;

public class Terminal
{
    private String Regdate;

    private String Errmsg;

    private TerminalInfo[] TerminalInfo;

    private String Regtime;

    private String Result;

    public TerminalInfo[] getTerminalInfo ()
    {
        return TerminalInfo;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [Regdate = "+Regdate+", Errmsg = "+Errmsg+", TerminalInfo = "+TerminalInfo+", Regtime = "+Regtime+", Result = "+Result+"]";
    }
}
