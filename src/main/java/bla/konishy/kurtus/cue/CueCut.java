package bla.konishy.kurtus.cue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CueCut {

    private static Logger log = LoggerFactory.getLogger(CueCut.class);

    private final String cuePath, srcPath, ext;

    public CueCut(String cuePath, String srcPath, String ext) {
        this.cuePath = cuePath;
        this.srcPath = srcPath;
        this.ext = ext.startsWith(".") ? ext : "." + ext;
    }

    public void generateCmd() throws IOException {
        String[] cmd = {""};
        boolean[] filesLineReached = {false};
        String[] trackvals = new String[4];
        Files.lines(Paths.get(cuePath)).filter(line -> !line.startsWith("REM")).forEach(line -> {
            line = line.trim();
            if (filesLineReached[0] || line.startsWith("FILE")) {
                filesLineReached[0] = true;
                if (line.startsWith("TRACK")) {
                    if (trackvals[3] != null) {
                        cmd[0] = cmd[0].replace("{}", trackvals[3]);
                        cmd[0] += "ffmpeg -i " + srcPath + " -ss " +
                                trackvals[3] + " -to " + "{} \"" +
                                trackvals[0] + "_" +
                                trackvals[1] + "_" +
                                trackvals[2] + ext + "\"\n";
                    }
                    trackvals[0] = line.split(" ")[1];
                } else if (line.startsWith("TITLE")) {
                    trackvals[2] = line.substring(5)
                            .replace("\"", "")
                            .trim()
                            .replace(" ", "_")
                            .replace(".", "");
                } else if (line.startsWith("PERFORMER")) {
                    trackvals[1] = line.substring(9)
                            .replace("\"", "")
                            .trim()
                            .replace(" ", "_")
                            .replace(".", "");
                } else if (line.startsWith("INDEX 01")) {
                    trackvals[3] = toSeconds(line.split(" ")[2]);
                }
            }
        });
        System.out.println(cmd[0].replace("-to {} ", ""));
    }

    private String toSeconds(String cueTime) {
        String[] pts = cueTime.split(":");
        String t = "" + (Double.parseDouble(pts[0]) * 60 + Double.parseDouble(pts[1]) + (Double.parseDouble(pts[2]) / 75.0));
        int idx = t.indexOf(".");
        return idx == -1 ? t : t.substring(0, Math.min(idx + 4, t.length()));
    }

    public static void main(String[] args) throws IOException {
        new CueCut(args[0], "\"" + args[1] + "\"", args[2]).generateCmd();
    }


}
