package com.bardiademon.moviescut;

public abstract class GetSize
{
    public static String Get(Long Byte)
    {
        if (Byte == null) return null;

        float kb = (float) Byte / 1024;
        if (kb >= 1024)
        {
            float mb = (kb / 1024);
            if (mb >= 1024)
            {
                float gb = mb / 1024;
                return String.format("%s GB" , toString(gb));
            }
            else return String.format("%s MB" , toString(mb));
        }
        else return String.format("%s KB" , toString(kb));
    }

    private static String toString(double size)
    {
        return String.format("%.3f" , Math.abs(size));
    }
}
