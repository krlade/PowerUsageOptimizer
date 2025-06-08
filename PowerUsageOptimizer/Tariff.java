package PowerUsageOptimizer;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Tariff {
    static List<Tariff> tarrifsList = new ArrayList<>();

    String name;
    double price;
    LocalTime startTime;
    LocalTime endTime;

    Tariff() {}

    Tariff(String name, double price, LocalTime startTime, LocalTime endTime) {
        this.name = name;
        this.price = price;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static void loadFromFile(String filePath) {
        // Implement file loading logic here
        // This method should read tariff data from a file and populate the tarrifsList
        // For now, we will just print a message
        System.out.println("TODO!");
    }

    public static double getPrice(LocalTime hour) {
        for(Tariff tariff : tarrifsList) {
            if (hour.getHour() == 0) {
                hour = hour.plusMinutes(1);
            }
            if ((hour.isAfter(tariff.startTime) || hour.equals(tariff.startTime)) && (hour.isBefore(tariff.endTime) || hour.equals(tariff.endTime))) {
                return tariff.price;
            }
        }

        throw new IllegalArgumentException("No tariff found for the given hour: " + hour);
    }

}
