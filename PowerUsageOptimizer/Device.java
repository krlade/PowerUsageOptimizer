package PowerUsageOptimizer;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Device {
    public String name;
    public double powerUsage; // in kWh
    public double workingTime; // in hours
    public boolean isFlexible; // true if the device can be used flexibly
    public char[] startTime; // used by genetic algorithm
    public LocalTime preferredStartTime; // actual starting time for scheduling

    public static List<Device> deviceList = new ArrayList<>();

    public Device() {}

    public Device(String name, double powerUsage, double workingHours, boolean isFlexible, LocalTime preferredStartTime) {
        this.name = name;
        this.powerUsage = powerUsage;
        this.workingTime = workingHours;
        this.isFlexible = isFlexible;

        if (preferredStartTime == null) {
            int randomMinutes = (int)(Math.random() * 1440); // 0-1439
            preferredStartTime = LocalTime.of(randomMinutes / 60, randomMinutes % 60);
        }

        setPreferredStartTime(preferredStartTime);
    }

    // Utility for genetic algorithm use: encodes time to 11-bit binary char array
    private char[] encodeStartTime(LocalTime time) {
        if (time == null) return null; // ⬅⬅⬅ zapobiega NullPointerException
        int totalMinutes = time.getHour() * 60 + time.getMinute();
        String binary = String.format("%11s", Integer.toBinaryString(totalMinutes)).replace(' ', '0');
        return binary.toCharArray();
    }


    // For use by genetic algorithm (binary time decoding)
    public LocalTime getStartTimeFromGene() {
        if (startTime == null || startTime.length != 11) return null;
        int minutes = Integer.parseInt(new String(startTime), 2);
        int hour = minutes / 60;
        int minute = minutes % 60;
        return LocalTime.of(hour % 24, minute % 60); // Ensure valid time
    }

    public void setStartTime(char[] startTime) {
        if (startTime != null && startTime.length == 11) {
            this.startTime = startTime;
            this.preferredStartTime = getStartTimeFromGene();
        } else {
            System.out.println("Invalid start time: must be 11-bit binary or non-null.");
            this.startTime = null;
            this.preferredStartTime = null;
        }
    }

    public double getPowerPrice() {
        double price = 0.0;
        if (preferredStartTime == null) return 0.0;
        for (int i = 0; i < (int)Math.ceil(workingTime); i++) {
            LocalTime hour = preferredStartTime.plusHours(i);
            price += Tariff.getPrice(hour) * powerUsage;
        }
        return price;
    }

    public LocalTime getPreferredStartTime() {
        return preferredStartTime;
    }

    public void setPreferredStartTime(LocalTime preferredStartTime) {
        this.preferredStartTime = preferredStartTime;
        this.startTime = encodeStartTime(preferredStartTime);
    }

    public static void loadFromFile(String filePath) {
        // TODO: Implement file loading if needed
        System.out.println("Loading devices from file not implemented.");
    }
}
