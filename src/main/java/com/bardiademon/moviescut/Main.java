package com.bardiademon.moviescut;

import javax.swing.UnsupportedLookAndFeelException;
import java.util.Locale;

public final class Main
{
    private static final String DEFAULT_LOOK_AND_FEEL_INFO = "windows";

    public static void main(final String[] args)
    {
        bardiademon.run();
        configView();
        new MoviesCut();
    }

    private static void configView()
    {
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if (DEFAULT_LOOK_AND_FEEL_INFO.equals(info.getName().toLowerCase(Locale.ROOT)))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex)
        {
            System.out.println(ex.getMessage());
        }
    }
}
