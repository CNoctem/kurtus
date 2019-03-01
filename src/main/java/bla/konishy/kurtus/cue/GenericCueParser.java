package bla.konishy.kurtus.cue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GenericCueParser {

    private String content;

    private List<Track> trackList = new ArrayList<>();

    private int idx = 0, savedIdx = 0;

    private StringBuilder time;

    private GenericCueParser() {

    }

    private void loadFile(String file) {
        try (Stream<String> lines =
                     Files.lines(Paths.get(file))) {
            StringBuilder sb = new StringBuilder();
            lines.forEach(sb::append);
            content = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cut() {
        findTimeStamps();
    }

    private void findTimeStamps() {
        char ch;
        while (idx < content.length()) {
            ch = content.charAt(idx);
            time = new StringBuilder();
            while (Character.isDigit(ch) || ch == ':') {
                time.append(ch);
                ch = content.charAt(++idx);
            }
            if (time.length() > 0 && time.toString().contains(":")) {
                trackList.add(new Track());
                getLast(0).ifPresent(tr -> {
                    tr.ss = time.toString();
                });
                getLast(1).ifPresent(tr -> {
                    tr.to = time.toString();
                    tr.title = content.substring(savedIdx, idx - time.length());
                });
                savedIdx = idx;
            }
            idx++;
        }
        getLast(0).ifPresent(tr -> tr.title = content.substring(savedIdx));
    }

    private Optional<Track> getLast(int offset) {
        if (trackList.size() > offset) return Optional.of(trackList.get(trackList.size() - 1 - offset));
        else return Optional.empty();
    }

    public List<Track> getTrackList() {
        return trackList;
    }

    class Track {
        String ss, title, to;

        @Override
        public String toString() {
            return "T: " + ss + " '" + title + "' " + to;
        }
    }

    public static class Builder {

        private String cueFile;

        public GenericCueParser build() {
            GenericCueParser gcc = new GenericCueParser();
            gcc.loadFile(cueFile);

            return gcc;
        }

        public Builder(String cueFile) {
            this.cueFile = cueFile;
        }

    }


    public static void main(String[] args) {
        GenericCueParser gcc = new GenericCueParser.Builder(args[0]).build();
        gcc.cut();

        Instant t0 = Instant.now();
        new Cutter.Runner()
                .format(".mp3")
                .input(args[1])
                .outDir(".")
                .tracks(gcc.getTrackList())
                .mode(Cutter.MODE.PARA)
                .script(args[2])
                .run();
        System.out.println(Duration.between(t0, Instant.now()));
    }


}

