package PowerUsageOptimizer;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Device {
    String name;
    double powerUsage; // in kWh
    double workingTime; // in hours
    boolean isFlexible; // true if the device can be used flexibly
    char[] startTime;
    LocalTime preferredStartTime;

    static List<Device> deviceList = new ArrayList<>();

    public Device() {}
    public Device(String name, double powerUsage, double workingHours, boolean isFlexible, LocalTime preferredStartTime) {
        this.name = name;
        this.powerUsage = powerUsage;
        this.workingTime = workingHours;
        this.isFlexible = isFlexible;
        this.preferredStartTime =preferredStartTime;
        this.startTime = String.valueOf((preferredStartTime.getHour()*60)+preferredStartTime.getMinute()).toCharArray();
    }

    static void loadFromFile(String filePath) {
        // Implement file loading logic here
        // This method should read device data from a file and populate the devices list
        // For now, we will just print a message
        System.out.println("TODO!");
    }

    public LocalTime getStartTime() {
        int minute = Integer.parseInt(Arrays.toString(startTime), 2);

        int hour = minute / 60;
        minute = minute % 60;
        return LocalTime.of(hour, minute);
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
    }
}
