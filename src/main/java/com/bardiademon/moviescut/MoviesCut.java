package com.bardiademon.moviescut;


import com.xuggle.xuggler.IContainer;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

public final class MoviesCut
{

    private static final String DIR_LIB = System.getProperty ("user.dir") + File.separator + "lib";
    private static final String FFMPEG_EXE = DIR_LIB + File.separator + "ffmpeg" + File.separator + "bin" + File.separator + "ffmpeg.exe";

    private long minPart;
    private String strMinPart;
    private final ConvertDuration convertDuration;

    private final BufferedReader reader;

    public MoviesCut ()
    {
        convertDuration = new ConvertDuration ();

        final JFileChooser chooser = new JFileChooser ();
        chooser.setMultiSelectionEnabled (true);

        reader = new BufferedReader (new InputStreamReader (System.in));

        new Thread (() ->
        {
            while (true)
            {
                while (true)
                {
                    System.out.print ("Divide into multi-minute parts <:exit> ? ");
                    try
                    {
                        strMinPart = reader.readLine ();
                        try
                        {
                            if (strMinPart.equals (":exit")) System.exit (0);

                            minPart = Integer.parseInt (strMinPart);
                            if (minPart > 0)
                            {
                                minPart = (Duration.ofSeconds (60 * minPart)).toNanos ();
                                break;
                            }
                            else throw new Exception ("invalid min part");
                        }
                        catch (Exception e)
                        {
                            System.err.println (e.getMessage ());
                        }
                    }
                    catch (IOException e)
                    {
                        System.err.println (e.getMessage ());
                    }
                }

                final AtomicInteger result = new AtomicInteger ();

                SwingUtilities.invokeLater (() ->
                {
                    result.set (chooser.showOpenDialog (null));
                    synchronized (MoviesCut.this)
                    {
                        MoviesCut.this.notify ();
                        MoviesCut.this.notifyAll ();
                    }
                });

                synchronized (MoviesCut.this)
                {
                    try
                    {
                        MoviesCut.this.wait ();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace ();
                    }
                }
                if (result.get () == JFileChooser.OPEN_DIALOG)
                {
                    final List <File> movies;

                    if (chooser.isMultiSelectionEnabled ())
                        movies = Arrays.asList (chooser.getSelectedFiles ());
                    else
                        movies = Collections.singletonList (chooser.getSelectedFile ());

                    for (final File movie : movies)
                    {
                        final IContainer container = IContainer.make ();
                        container.open (movie.getAbsolutePath () , IContainer.Type.READ , null);
                        System.out.println (movie.getName ());
                        final ConvertDuration.Time convert = convertDuration.convert (container.getDuration ());

                        final int l = (int) (convert._Duration.toNanos () / minPart);
                        System.out.println ("number of 10 => " + l);
                        System.out.println (convert.Hour + ":" + convert.Minutes + ":" + convert.Second);
                        System.out.println (GetSize.Get (container.getFileSize ()));
                        if (l >= 1)
                        {
                            System.out.println ("=======");
                            cut (new ToCut (convert , l , movie));
                        }
                        else System.out.println ("This movie < select time");
                        System.out.println ("===========================");
                    }
                }
                else break;
            }
        }).start ();

    }

    private static final class ToCut
    {
        private final ConvertDuration.Time Time;
        private final int NumberOfMin;
        private final File Movie;

        public ToCut (final ConvertDuration.Time Time , final int NumberOfMin , final File Movie)
        {
            this.Time = Time;
            this.NumberOfMin = NumberOfMin;
            this.Movie = Movie;
        }
    }

    private int start, progress;
    private int counterName;

    private void cut (final ToCut toCut)
    {
        System.out.println ("Cutting " + toCut.Movie.getName ());
        counterName = 0;
        this.start = 0;
        this.progress = (int) (minPart / 1000);
        // ffmpeg.exe -i movie.mp4 -ss 00:00:03 -t 00:00:08 -async 1 cut.mp4

        final File output = new File (toCut.Movie.getParent () + File.separator + "_" + toCut.Movie.getName () + "_");
        if (output.exists () || output.mkdir ())
        {
            for (int i = 0; i < toCut.NumberOfMin; i++) doCut (toCut , output.getPath ());

            progress -= (int) (minPart / 1000);

            /*
             * end ro tabdil be nano mikonam , duration * 1000 = nano
             */
            if ((progress * 1000) <= toCut.Time._Duration.toNanos ())
            {
                start += progress;
                progress = Math.abs ((((int) (toCut.Time._Duration.toNanos () / 1000)) - start));
                doCut (toCut , output.getPath ());
            }
        }
        else System.out.println ("Cannot create file  " + output.getPath ());
    }

    private final String exec = FFMPEG_EXE + " -i :MOVIE -ss :START -t :PROGRESS -async 1 :OUT_MOVIE";

    private void doCut (final ToCut toCut , final String output)
    {
        final ConvertDuration.Time start = convertDuration.convert (this.start), progress = convertDuration.convert (this.progress);
        try
        {
            final String outputFile = output + File.separator + "cut_" + (++counterName) + "_" + toCut.Movie.getName ();

            final String replace = exec.replace (":MOVIE" , toCut.Movie.getPath ())
                    .replace (":OUT_MOVIE" , outputFile)
                    .replace (":START" , start.toString ())
                    .replace (":PROGRESS" , progress.toString ());
            System.out.println ("Cutting " + start.toString () + " - " + convertDuration.convert (this.start + this.progress).toString ());
            System.out.println ("exec " + replace);
            final ProcessBuilder processBuilder =
                    new ProcessBuilder (FFMPEG_EXE , "-i" , toCut.Movie.getPath () , "-ss" , start.toString () , "-t" , progress.toString () , "-async" , "1" , outputFile);
            final Process execResult = processBuilder.start ();
            final BufferedReader reader = new BufferedReader (new InputStreamReader (execResult.getErrorStream ()));

            String line;
            while ((line = reader.readLine ()) != null) System.out.println (line);

            System.out.println ("Cut " + toCut.Movie.getName ());
            this.start += this.progress;
        }
        catch (IOException e)
        {
            e.printStackTrace ();
        }
    }

}
