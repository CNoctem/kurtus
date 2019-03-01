package bla.konishy.kurtus.cue;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Cutter {

    public enum MODE {
        SCRIPT, SEQ, PARA
    }

    public static class Runner {
        private List<GenericCueParser.Track> trackList;
        private String inputFile;
        private String outDir;
        private String format;
        private String script;

        private MODE mode;

        public Runner tracks(List<GenericCueParser.Track> tracks) {
            trackList = tracks;
            return this;
        }

        public Runner input(String file) {
            inputFile = file;
            return this;
        }

        public Runner outDir(String file) {
            outDir = file;
            return this;
        }

        public Runner mode(MODE m) {
            mode = m;
            return this;
        }

        public Runner script(String s) {
            script = s;
            return this;
        }

        public Runner format(String format) {
            this.format = format;
            return this;
        }

        public void run() {
            Cutter c = new Cutter(trackList, inputFile, outDir, mode, format, script);
        }

    }

    private Cutter(List<GenericCueParser.Track> tracks,
                   String in, String out,
                   MODE mode,
                   String format,
                   String script) {

        if (!format.startsWith(".")) format = "." + format;
        List<String[]> commands = new ArrayList<>(tracks.size());
        for (int i = 0; i < tracks.size(); i++) {
            GenericCueParser.Track t = tracks.get(i);
            commands.add(t.to != null ?
                    new String[]{"ffmpeg", "-i", in, "-ss", t.ss, "-to", t.to,
                            normalizeTitle(i + 1, t.title , format)}:
                    new String[]{"ffmpeg", "-i", in, "-ss", t.ss,
                            normalizeTitle(i + 1, t.title , format)}
                    );
        }

        switch (mode) {
            case SCRIPT:
                File scriptFile = new File(script);
                if (scriptFile.exists()) {
                    System.out.printf("%s: Already exists.\n", script);
                    commands.forEach(System.out::println);
                } else {
                    try (FileOutputStream fout = new FileOutputStream(scriptFile)) {
                        for (String[] cmd : commands) {
//                            fout.write(cmd.getBytes());
                            fout.write('\n');
                            fout.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case SEQ:
                for (String[] cmd : commands) {
                    try {
                        System.out.println("Starting command " + Arrays.toString(cmd));
                        Process p = Runtime.getRuntime().exec(cmd);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        String l;
                        while ((l = reader.readLine()) != null) {
                            System.out.println(l);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PARA:
                try {
                    ExecutorService serv = Executors.newFixedThreadPool(4);
                    serv.invokeAll(createCallables(commands));

                    awaitTerminationAfterShutdown(serv);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }

    private List<Callable<Void>> createCallables(List<String[]> commands) {
        List<Callable<Void>> list = new ArrayList<>(commands.size());
        for (String[] cmd : commands) {
            list.add(() -> {
                System.out.println("Starting command " + Arrays.toString(cmd));
                Process p = Runtime.getRuntime().exec(cmd);
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String l;
                while ((l = reader.readLine()) != null) {
                    System.out.println(l);
                }
                return null;
            });
        }
        return list;
    }

    private static final List<Character> prefixes = Arrays.asList('_', '-');

    private static String normalizeTitle(int trackno, String title, String format) {
        title = title.replace(" ", "");
        return String.format("%02d", trackno) + (prefixes.contains(title.charAt(0)) ? "" : "_") + title + format;
    }

    public void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
