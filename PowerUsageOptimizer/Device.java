package PowerUsageOptimizer;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Device {
    String name;
    double powerUsage; // in kWh
    double workingHours; // in hours
    boolean isFlexible; // true if the device can be used flexibly, false if it has fixed usage time
    LocalTime preferredStartTime;

    static List<Device> devices = new ArrayList<>();

    public Device(String name, double powerUsage, double workingHours, boolean isFlexible, LocalTime preferredStartTime) {
        this.name = name;
        this.powerUsage = powerUsage;
        this.workingHours = workingHours;
        this.isFlexible = isFlexible;
        this.preferredStartTime = preferredStartTime;
    }

    static void loadFromFile(String filePath) {
        // Implement file loading logic here
        // This method should read device data from a file and populate the devices list
        // For now, we will just print a message
        System.out.println("TODO!");
    }
}
