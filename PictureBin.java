import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PictureBin {

    private static ArrayList<String> binOptions = new ArrayList<String>();

    public static ArrayList<String> getBinOptions() {
        return binOptions;
    }

    public static void setBinOptions(ArrayList<String> newBinOptions) {
        binOptions = newBinOptions;
    }

    public static void addBinOption(String binOption) {
        binOptions.add(binOption);
    }

    public static void removeBinOption(String binOption) {
        binOptions.remove(binOption);
    }

    public static void processBinOptions(String optsArg) {
        binOptions.clear();
        String regex = "^-binOpts=(.+)(,.+)*$";
        if (optsArg.trim().matches(regex)) {
            System.out.println("binOpts are good");
            String opts = optsArg.replace("-binOpts=", "").toLowerCase();
            binOptions = new ArrayList<String>(Arrays.asList(opts.split(",")));
        } else {
            System.out.println(
                    "binOpts does not follow correct format.\nPlease use: -binOpts=arg,arg,arg or -binOpts=\"arg,arg,arg\"");
            // Binning options failure
            // we should test for windows and mac file character exeptions
        }
    }

    public static void binPicture(File file, String filteredFilesPath) {
        try {
            for (String binOpt : binOptions) {
                if (file.getName().toLowerCase().contains(binOpt)) {
                    File binDir = new File(filteredFilesPath + binOpt + "/");
                    if (!binDir.exists() || !binDir.isDirectory()) {
                        binDir.mkdirs();
                    }
                    Files.move(Paths.get(file.getPath()), Paths.get(binDir.getPath(), file.getName()),
                            StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
            }

            BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            long milliseconds = attributes.lastModifiedTime().to(TimeUnit.MILLISECONDS);
            if ((milliseconds > Long.MIN_VALUE) && (milliseconds < Long.MAX_VALUE)) {
                Date modifiedDate = new Date(milliseconds);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(modifiedDate);
                int modifiedYear = calendar.get(Calendar.YEAR);
                String modifiedMonth = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.ENGLISH);

                File binDir = new File(filteredFilesPath + modifiedYear + "/" + modifiedMonth + "/");
                if (!binDir.exists() || !binDir.isDirectory()) {
                    binDir.mkdirs();
                }
                Files.move(Paths.get(file.getPath()), Paths.get(binDir.getPath(), file.getName()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}