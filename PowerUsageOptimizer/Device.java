package PowerUsageOptimizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.FileReader;

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

    public void setStartTime(char[] startTime) {
        char[] newStartTime = new char[11];

        for(int i=11-startTime.length; i>0; i--) {
            Arrays.fill(newStartTime, '0');
            System.arraycopy(startTime, 0, newStartTime, 11-startTime.length, startTime.length);
        }
        this.startTime = newStartTime;

    }

    public double getPowerPrice() {
        double price = 0.0;
        for (int i = 0; i < workingTime; i++) {
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
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                // Pomijamy nagłówek
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] fields = line.split(",");

                String name = fields[0].trim();
                double power = Double.parseDouble(fields[1].trim());
                double workingHours = Double.parseDouble(fields[2].trim()); // Domyślna wartość, można zmienić w przyszłości
                boolean isElastic = Boolean.parseBoolean(fields[3].trim());
                int minutes = Integer.parseInt(fields[4].trim());
                LocalTime preferedStartTime = LocalTime.of(minutes/60, minutes % 60);

                Device device = new Device(name, power, workingHours, isElastic, preferedStartTime);
                Device.deviceList.add(device);
            }
        } catch (IOException e) {
            System.err.println("Błąd podczas wczytywania pliku: " + e.getMessage());
        }
    }
}
