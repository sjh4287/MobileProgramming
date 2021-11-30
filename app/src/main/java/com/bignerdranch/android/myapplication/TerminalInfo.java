package com.bignerdranch.android.myapplication;

public class TerminalInfo
{
    private String Vno;

    private String Rackcnt;

    private String Latitude;

    private String Longitude;

    private String Emptycnt;

    private String Parkcnt;

    private String Tmname;


    public String getVno ()
    {
        return Vno;
    }

    public String getLatitude ()
    {
        return Latitude;
    }

    public String getLongitude ()
    {
        return Longitude;
    }

    public String getEmptycnt ()
    {
        return Emptycnt;
    }

    public String getParkcnt ()
    {
        return Parkcnt;
    }

    public String getTmname ()
    {
        return Tmname;
    }


    @Override
    public String toString()
    {
        return "ClassPojo [Vno = "+Vno+", Rackcnt = "+Rackcnt+", Latitude = "+Latitude+", Longitude = "+Longitude+", Emptycnt = "+Emptycnt+", Parkcnt = "+Parkcnt+", Tmname = "+Tmname+"]";
    }
}
