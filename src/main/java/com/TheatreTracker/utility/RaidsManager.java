package com.TheatreTracker.utility;

import com.TheatreTracker.RoomData;


import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class RaidsManager {
    private static final String raidsFolder = System.getProperty("user.home").replace("\\", "/") + "/.runelite/theatretracker/raids/";

    public static ArrayList<RaidsArrayWrapper> getRaidsSets() {
        ArrayList<RaidsArrayWrapper> raidSets = new ArrayList<>();
        File folder = new File(raidsFolder);
        if (!folder.exists()) folder.mkdirs();
        try {
            for (File entry : folder.listFiles()) {
                if (entry.isFile()) {
                    if (entry.getAbsolutePath().endsWith(".raids")) {
                        ArrayList<RoomData> raids = new ArrayList<>();
                        try {
                            Scanner raidsReader = new Scanner(Files.newInputStream(entry.toPath()));
                            ArrayList<String> raid = new ArrayList<>();
                            boolean raidActive = false;
                            while (raidsReader.hasNextLine()) {
                                String line = raidsReader.nextLine();
                                String[] lineSplit = line.split(",");
                                if (!raidActive) {
                                    if (lineSplit.length > 3) {
                                        if (Integer.parseInt(lineSplit[3]) == 0) {
                                            raid.add(line);
                                            raidActive = true;
                                        }
                                    }
                                } else {
                                    if (lineSplit.length > 3) {
                                        if (Integer.parseInt(lineSplit[3]) == 99) {
                                            raid.add(line);
                                        } else if (Integer.parseInt(lineSplit[3]) == 4) {
                                            raid.add(line);
                                            raidActive = false;
                                            raids.add(new RoomData(raid.toArray(new String[raid.size()])));
                                            raid.clear();
                                        } else {
                                            raid.add(line);
                                        }
                                    }
                                }
                            }
                            raidsReader.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        raidSets.add(new RaidsArrayWrapper(raids, entry.getName()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return raidSets;
    }

    public static boolean doesRaidExist(String name) {
        File folder = new File(raidsFolder);
        try {
            for (File entry : folder.listFiles()) {
                if (entry.getName().equals(name + ".raids")) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void saveOverwriteRaids(String name, ArrayList<RoomData> raids) {
        try {
            File directory = new File(raidsFolder);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File raidsFile = new File(raidsFolder + name + ".raids");

            if (raidsFile.exists()) {
                raidsFile.delete();
            }
            raidsFile.createNewFile();
            BufferedWriter raidsWriter = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(raidsFolder + name + ".raids"))));
            for (RoomData raid : raids) {
                for (String s : raid.raidDataRaw) {
                    raidsWriter.write(s);
                    raidsWriter.newLine();
                }
            }
            raidsWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveRaids(String name, ArrayList<RoomData> raids) {
        try {
            File directory = new File(raidsFolder);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File raidsFile = new File(raidsFolder + name + ".raids");
            if (!raidsFile.exists()) {
                raidsFile.createNewFile();
            }
            BufferedWriter raidsWriter = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(raidsFolder + name + ".raids"))));
            for (RoomData raid : raids) {
                for (String s : raid.raidDataRaw) {
                    raidsWriter.write(s);
                    raidsWriter.newLine();
                }
            }
            raidsWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
