import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

//Add arg for binning based on certain filenamne ex: -binOpt="snapchat"
//full tool cli: PictureTool.java {directoryPath} -binOpt="arg,arg,arg"
public class PictureTool {
    static String root;
    static String removedFilesPath;
    static String filteredFilesPath;

    static HashMap<String, File> pictureMap = new HashMap<String, File>();

    public static void main(String[] args) {
        handleArgs(args);

        removedFilesPath = root + "/!RemovedFiles/";
        filteredFilesPath = root + "/!BinnedFiles/";

        File pictureDir = openFile(root);
        // only create new folder if the main folder has been opened
        File filteredFilesDir = makeDir(filteredFilesPath);
        System.out.println("Is !BinnedFiles a dir: " + filteredFilesDir.isDirectory());
        File removedFilesDir = makeDir(removedFilesPath);
        System.out.println("Is /!RemovedFiles/ a dir: " + removedFilesDir.isDirectory());

        iterateSubFolder(pictureDir, filteredFilesDir, removedFilesDir);
    }

    private static void handleArgs(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean hasBinOpts = false;
        String escapeCmd = "!done";

        if (args == null || args.length == 0) {
            // no path defined
            while (root == null || root.isEmpty()) {
                System.out.println("Please provide a directory path for the tool, or quit using 'exit':\n");
                String userIn = sc.nextLine();
                if (userIn.equalsIgnoreCase("exit")) {
                    sc.close();
                    return;
                } else {
                    root = userIn;
                }
            }
        } else if (args.length <= 1) {
            root = args[0];
            // no bin options defined
            System.out.println("Would you like to add a binning option? \n"
                    + "This will create a file with the specified option name and prioritize binning pictures that contain the bin name");

            while (!hasBinOpts) {
                System.out.println("Type a binning option and press enter to add. Enter " + escapeCmd
                        + " with case sensitivity to continue.");
                String userIn = sc.nextLine();
                if (userIn.equals(escapeCmd)) {
                    hasBinOpts = true;
                } else {
                    PictureBin.addBinOption(userIn);
                    System.out.println("Current bins: " + PictureBin.getBinOptions().toString());
                }
            }
        } else {
            // path and bin options are defined.
            root = args[0];
            PictureBin.processBinOptions(args[1]);
        }
        sc.close();
    }

    private static void iterateSubFolder(File subFolder, File filteredFilesDir, File removedFilesDir) {
        if (subFolder.getPath().equals(removedFilesPath) || subFolder.getPath().equals(filteredFilesPath)) {
            System.out.println("Skipping default tool folders");
        } else {
            System.out.println("Parsing subfolder: " + subFolder.getPath());
            for (File file : subFolder.listFiles()) {
                if (file.isDirectory()) {
                    iterateSubFolder(file, filteredFilesDir, removedFilesDir);
                } else {
                    try {
                        if (pictureMap.containsKey(file.getName())) {
                            System.out.println("Moving '" + file.getName() + "' to " + removedFilesPath);
                            Files.move(Paths.get(file.getPath()), Paths.get(removedFilesDir.getPath(), file.getName()),
                                    StandardCopyOption.REPLACE_EXISTING);
                        } else {
                            pictureMap.put(file.getName(), file);
                            PictureBin.binPicture(file, filteredFilesPath);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static File makeDir(String path) {
        System.out.println("Creating directory: " + path);
        File newDir = new File(path);
        newDir.mkdirs();
        return newDir;
    }

    private static File openFile(String path) {
        return new File(path);
    }
}