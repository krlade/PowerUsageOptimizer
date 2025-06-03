package PowerUsageOptimizer;

import java.util.ArrayList;
import java.util.List;

public class Tariff {
    static List<Tariff> tarrifsList = new ArrayList<>();

    String name;
    double price;
    int startTime;
    int endTime;

    Tariff() {}

    Tariff(String name, double price, int startTime, int endTime) {
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

    public double getPrice(int hour) {
        for(Tariff tariff : tarrifsList) {
            if (hour >= tariff.startTime && hour < tariff.endTime) {
                return tariff.price;
            }
        }

        return -1;
    }

}
