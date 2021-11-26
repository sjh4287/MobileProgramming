/////////////////////////////////////////////
//해당 파일 이름 충돌나서 대충 1 붙였는데 1 삭제하셈!//
/////////////////////////////////////////////
package com.bignerdranch.android.myapplication;

public class Terminal
{
    private String Regdate;

    private String Errmsg;

    private TerminalInfo[] TerminalInfo;

    private String Regtime;

    private String Result;

    public String getRegdate ()
    {
        return Regdate;
    }

    public void setRegdate (String Regdate)
    {
        this.Regdate = Regdate;
    }

    public String getErrmsg ()
    {
        return Errmsg;
    }

    public void setErrmsg (String Errmsg)
    {
        this.Errmsg = Errmsg;
    }

    public TerminalInfo[] getTerminalInfo ()
    {
        return TerminalInfo;
    }

    public void setTerminalInfo (TerminalInfo[] TerminalInfo)
    {
        this.TerminalInfo = TerminalInfo;
    }

    public String getRegtime ()
    {
        return Regtime;
    }

    public void setRegtime (String Regtime)
    {
        this.Regtime = Regtime;
    }

    public String getResult ()
    {
        return Result;
    }

    public void setResult (String Result)
    {
        this.Result = Result;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [Regdate = "+Regdate+", Errmsg = "+Errmsg+", TerminalInfo = "+TerminalInfo+", Regtime = "+Regtime+", Result = "+Result+"]";
    }
}
