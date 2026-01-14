package org.averyocean65.swp;

import java.io.*;
import java.util.Scanner;

public class IO {
    @SuppressWarnings("CallToPrintStackTrace")
    public static Result<String> readFile(String path) {
        File file = new File(path);
        return readFile(file);
    }

    public static Result<String> readFile(File file) {
        StringBuilder output = new StringBuilder();

        try (Scanner scanner = new Scanner(file)) {
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                output.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Exception occurred.");
            e.printStackTrace();

            return new Result<>(false, "");
        }

        return new Result<>(true, output.toString());
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public static Result<File> findFile(String path) {
        File file = new File(path);
        if(!file.exists()) {
            return new Result<>(false, null);
        }
        return new Result<>(true, file);
    }

    public static boolean doesFileExist(String path) {
        if(path == null) return false;
        return doesFileExist(new File(path));
    }

    public static boolean doesFileExist(File file) {
        return file.exists();
    }

    public static boolean writeFile(String path, String content) {
        if(!doesFileExist(path)) {
            return false;
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            writer.write(content);

            writer.close();
        } catch (IOException e) {
            System.out.println("An exception occurred.");
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
