package PowerUsageOptimizer;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Main {
    
    public static void main(String[] args) {
        // Inicjalizacja taryf G12
        initializeTariffs();
        
        // Inicjalizacja przykładowych urządzeń
        initializeDevices();
        
        // Wyświetlenie informacji o taryfach
        displayTariffs();
        
        // Wyświetlenie informacji o urządzeniach
        displayDevices();
        
        // Uruchomienie algorytmu genetycznego
        runGeneticAlgorithm();
    }
    
    /**
     * Inicjalizuje taryfy G12 zgodnie z tabelą
     */
    private static void initializeTariffs() {
        System.out.println("\n=== Inicjalizacja taryf G12 ===");
        
        // Taryfa nocna: 22:00-6:00 (0.5868 zł/kWh)
        Tariff nightTariff1 = new Tariff("Nocna", 0.5868, 
                                        LocalTime.of(22, 0), 
                                        LocalTime.of(23, 59));
        Tariff.tarrifsList.add(nightTariff1);
        
        Tariff nightTariff2 = new Tariff("Nocna", 0.5868, 
                                        LocalTime.of(0, 0), 
                                        LocalTime.of(6, 0));
        Tariff.tarrifsList.add(nightTariff2);
        
        // Taryfa nocna: 13:00-15:00 (0.5868 zł/kWh)
        Tariff afternoonNightTariff = new Tariff("Nocna", 0.5868, 
                                               LocalTime.of(13, 0), 
                                               LocalTime.of(15, 0));
        Tariff.tarrifsList.add(afternoonNightTariff);
        
        // Taryfa dzienna: pozostałe godziny (0.6212 zł/kWh)
        // 6:00-13:00
        Tariff dayTariff1 = new Tariff("Dzienna", 0.6212, 
                                      LocalTime.of(6, 0), 
                                      LocalTime.of(13, 0));
        Tariff.tarrifsList.add(dayTariff1);
        
        // 15:00-22:00
        Tariff dayTariff2 = new Tariff("Dzienna", 0.6212, 
                                      LocalTime.of(15, 0), 
                                      LocalTime.of(22, 0));
        Tariff.tarrifsList.add(dayTariff2);
        
        System.out.println("Utworzono " + Tariff.tarrifsList.size() + " przedziałów taryfowych");
    }
    
    /**
     * Inicjalizuje przykładowe urządzenia
     */
    private static void initializeDevices() {
        System.out.println("\n=== Inicjalizacja urządzeń ===");
        
        // Przykładowe urządzenia
        Device washingMachine = new Device("Pralka", 2.0, 2.0, true, LocalTime.of(8, 0));
        Device dishwasher = new Device("Zmywarka", 1.8, 1.5, true, LocalTime.of(20, 0));
        Device electricHeater = new Device("Grzejnik elektryczny", 3.0, 4.0, true, LocalTime.of(6, 0));
        Device waterHeater = new Device("Podgrzewacz wody", 2.5, 1.0, false, LocalTime.of(5, 0)); // nieelastyczny
        Device airConditioner = new Device("Klimatyzacja", 2.2, 3.0, true, LocalTime.of(14, 0));
        Device electricOven = new Device("Piekarnik elektryczny", 2.8, 1.0, true, LocalTime.of(18, 0));
        
        Device.deviceList.add(washingMachine);
        Device.deviceList.add(dishwasher);
        Device.deviceList.add(electricHeater);
        Device.deviceList.add(waterHeater);
        Device.deviceList.add(airConditioner);
        Device.deviceList.add(electricOven);
        
        System.out.println("Utworzono " + Device.deviceList.size() + " urządzeń");
    }
    
    /**
     * Wyświetla informacje o taryfach
     */
    private static void displayTariffs() {
        System.out.println("\n=== Taryfy G12 ===");
        System.out.println("Przedziały taryfowe:");
        for (Tariff tariff : Tariff.tarrifsList) {
            System.out.println(String.format("%-8s | %s-%s | %.4f zł/kWh", 
                             tariff.name, 
                             tariff.startTime, 
                             tariff.endTime, 
                             tariff.price));
        }
        
        // Test cen w różnych godzinach
        System.out.println("\nPrzykładowe ceny w różnych godzinach:");
        LocalTime[] testHours = {
            LocalTime.of(2, 0),   // nocna
            LocalTime.of(8, 0),   // dzienna
            LocalTime.of(14, 0),  // nocna (popołudniowa)
            LocalTime.of(18, 0),  // dzienna
            LocalTime.of(23, 0)   // nocna
        };
        
        for (LocalTime hour : testHours) {
            double price = Tariff.getPrice(hour);
            System.out.println(String.format("Godzina %s: %.4f zł/kWh", hour, price));
        }
    }
    
    /**
     * Wyświetla informacje o urządzeniach
     */
    private static void displayDevices() {
        System.out.println("\n=== Urządzenia ===");
        for (int i = 0; i < Device.deviceList.size(); i++) {
            Device device = Device.deviceList.get(i);
            System.out.println(String.format("%-20s | %.1f kWh | %.1f h | %s | Start: %s", 
                             device.name, 
                             device.powerUsage, 
                             device.workingTime,
                             device.isFlexible ? "Elastyczne" : "Nieelastyczne",
                             device.getPreferredStartTime()));
        }
    }
    
    /**
     * Uruchamia algorytm genetyczny na 100 pokoleń
     */
    private static void runGeneticAlgorithm() {
        System.out.println("\n=== Uruchamianie algorytmu genetycznego ===");
        System.out.println("Parametry:");
        System.out.println("- Rozmiar populacji: 50");
        System.out.println("- Rozmiar promocji: 10");
        System.out.println("- Liczba pokoleń: 100");
        System.out.println("- Prawdopodobieństwo mutacji: " + Chromosome.pm);
        
        // Resetowanie licznika generacji
        Generation.resetGenerationCounter();
        
        // Utworzenie pierwszej generacji
        Generation currentGeneration = new Generation(50, 10);
        
        System.out.println("\n=== Ewolucja ===");
        System.out.println("Gen. | Najlepsza fitness | Średnia fitness");
        System.out.println("-----|-------------------|----------------");
        
        // Wyświetlenie statystyk pierwszej generacji
        System.out.println(String.format("%4d | %16.2f | %14.2f", 
                         currentGeneration.getGenerationNumber(), 
                         currentGeneration.getBestFitness(), 
                         currentGeneration.getAverageFitness()));
        
        // Ewolucja przez 99 kolejnych pokoleń (razem 100)
        for (int i = 1; i < 100; i++) {
            currentGeneration = new Generation(currentGeneration, 50, 10);
            
            // Wyświetlenie statystyk co 5 pokoleń lub dla ostatnich 10
            if (i % 5 == 0 || i >= 90) {
                System.out.println(String.format("%4d | %16.2f | %14.2f", 
                                 currentGeneration.getGenerationNumber(), 
                                 currentGeneration.getBestFitness(), 
                                 currentGeneration.getAverageFitness()));
            }
        }
        
        // Szczegółowe wyniki końcowe
        System.out.println("\n=== Wyniki końcowe ===");
        currentGeneration.printGenerationStats();
        
        System.out.println("\n=== Najlepszy chromosom ===");
        Chromosome bestChromosome = currentGeneration.getBestChromosome();
        if (bestChromosome != null) {
            bestChromosome.printChromosome();
            
            // Obliczenie całkowitego kosztu
            double totalCost = bestChromosome.getFitness() / 1.2; // usunięcie penalty
            System.out.println(String.format("\nCałkowity koszt energii: %.2f zł", totalCost));
            
            // Porównanie z naiwnym podejściem
            double naiveCost = calculateNaiveCost();
            System.out.println(String.format("Koszt bez optymalizacji: %.2f zł", naiveCost));
            System.out.println(String.format("Oszczędności: %.2f zł (%.1f%%)", 
                             naiveCost - totalCost, 
                             ((naiveCost - totalCost) / naiveCost) * 100));
        }
        
        // Podsumowanie ewolucji
        System.out.println("\n=== Podsumowanie ewolucji ===");
        List<Generation> allGenerations = Generation.getGenerationList();
        double firstGenBest = allGenerations.get(0).getBestFitness();
        double lastGenBest = allGenerations.get(allGenerations.size() - 1).getBestFitness();
        double improvement = ((firstGenBest - lastGenBest) / firstGenBest) * 100;
        
        System.out.println(String.format("Pierwsza generacja - najlepsza fitness: %.2f", firstGenBest));
        System.out.println(String.format("Ostatnia generacja - najlepsza fitness: %.2f", lastGenBest));
        System.out.println(String.format("Poprawa: %.2f (%.1f%%)", firstGenBest - lastGenBest, improvement));
    }
    
    /**
     * Oblicza koszt przy naiwnym podejściu (bez optymalizacji)
     * @return koszt przy oryginalnych czasach urządzeń
     */
    private static double calculateNaiveCost() {
        double totalCost = 0.0;
        
        for (Device device : Device.deviceList) {
            if (device.getPreferredStartTime() != null) {
                for (int i = 0; i < device.workingTime; i++) {
                    LocalTime hour = device.getPreferredStartTime().plusHours(i);
                    double price = Tariff.getPrice(hour);
                    if (price > 0) {
                        totalCost += price * device.powerUsage;
                    }
                }
            }
        }
        
        return totalCost;
    }
}