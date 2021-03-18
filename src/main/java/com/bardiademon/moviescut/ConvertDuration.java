package com.bardiademon.moviescut;

import java.time.Duration;

public final class ConvertDuration
{
    public ConvertDuration ()
    {

    }

    public Time convert (final long duration)
    {
        final Duration nanos = Duration.ofNanos (duration * 1000);
        final int s = (int) nanos.getSeconds () % 60;
        int h = (int) nanos.getSeconds () / 60;
        final int m = h % 60;
        h /= 60;


        return new Time (nanos , h , m , s);
    }

    public static final class Time
    {
        public final int Hour, Minutes, Second;

        public final Duration _Duration;

        private Time ()
        {
            this (null , 0 , 0 , 0);
        }

        private Time (final Duration _Duration , final int H , final int M , final int S)
        {
            this._Duration = _Duration;
            this.Hour = H;
            this.Minutes = M;
            this.Second = S;
        }

        @Override
        public String toString ()
        {
            return String.format ("%s:%s:%s" , toString (Hour) , toString (Minutes) , toString (Second));
        }

        private String toString (final int hms)
        {
            return (hms >= 10 ? String.valueOf (hms) : 0 + String.valueOf (hms));
        }
    }
}

