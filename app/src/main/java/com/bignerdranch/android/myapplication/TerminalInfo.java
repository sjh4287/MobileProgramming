package com.bignerdranch.android.myapplication;

import com.google.gson.annotations.SerializedName;
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

    public void setVno (String Vno)
    {
        this.Vno = Vno;
    }

    public String getRackcnt ()
    {
        return Rackcnt;
    }

    public void setRackcnt (String Rackcnt)
    {
        this.Rackcnt = Rackcnt;
    }

    public String getLatitude ()
    {
        return Latitude;
    }

    public void setLatitude (String Latitude)
    {
        this.Latitude = Latitude;
    }

    public String getLongitude ()
    {
        return Longitude;
    }

    public void setLongitude (String Longitude)
    {
        this.Longitude = Longitude;
    }

    public String getEmptycnt ()
    {
        return Emptycnt;
    }

    public void setEmptycnt (String Emptycnt)
    {
        this.Emptycnt = Emptycnt;
    }

    public String getParkcnt ()
    {
        return Parkcnt;
    }

    public void setParkcnt (String Parkcnt)
    {
        this.Parkcnt = Parkcnt;
    }

    public String getTmname ()
    {
        return Tmname;
    }

    public void setTmname (String Tmname)
    {
        this.Tmname = Tmname;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [Vno = "+Vno+", Rackcnt = "+Rackcnt+", Latitude = "+Latitude+", Longitude = "+Longitude+", Emptycnt = "+Emptycnt+", Parkcnt = "+Parkcnt+", Tmname = "+Tmname+"]";
    }
}
