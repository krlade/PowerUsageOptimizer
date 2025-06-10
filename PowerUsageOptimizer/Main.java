package PowerUsageOptimizer;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

        javax.swing.SwingUtilities.invokeLater(GUI::new);
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
        //Device washingMachine = new Device("Pralka", 2.0, 2.0, true, LocalTime.of(8, 0));
        //Device dishwasher = new Device("Zmywarka", 1.8, 1.5, true, LocalTime.of(20, 0));
        //Device electricHeater = new Device("Grzejnik elektryczny", 3.0, 4.0, true, LocalTime.of(6, 0));
        //Device waterHeater = new Device("Podgrzewacz wody", 2.5, 1.0, false, LocalTime.of(5, 0)); // nieelastyczny
        //Device airConditioner = new Device("Klimatyzacja", 2.2, 3.0, true, LocalTime.of(14, 0));
        //Device electricOven = new Device("Piekarnik elektryczny", 2.8, 1.0, true, LocalTime.of(18, 0));
        //Device tv = new Device("Telewizor", 0.8, 3.0, true, LocalTime.of(20, 45));

        Device.loadFromFile("src/devices.csv");

        //Device.deviceList.add(washingMachine);
        //Device.deviceList.add(dishwasher);
        //Device.deviceList.add(electricHeater);
        //Device.deviceList.add(waterHeater);
        //Device.deviceList.add(airConditioner);
        //Device.deviceList.add(electricOven);
        //Device.deviceList.add(tv);

        System.out.println("Utworzono " + Device.deviceList.size() + " urządzeń");
    }

    /**
     * Wyświetla informacje o taryfach
     */
    private static void displayTariffs() {
        System.out.println("\n=== Taryfy G12 ===");
        System.out.println("Przedziały taryfowe:");
        for (Tariff tariff : Tariff.tarrifsList) {
            System.out.printf("%-8s | %s-%s | %.4f zł/kWh%n",
                             tariff.name,
                             tariff.startTime,
                             tariff.endTime,
                             tariff.price);
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
            System.out.printf("Godzina %s: %.4f zł/kWh%n", hour, price);
        }
    }

    /**
     * Wyświetla informacje o urządzeniach
     */
    private static void displayDevices() {
        System.out.println("\n=== Urządzenia ===");
        for (int i = 0; i < Device.deviceList.size(); i++) {
            Device device = Device.deviceList.get(i);
            System.out.printf("%-20s | %.1f kWh | %.1f h | %s | Start: %s%n",
                             device.name,
                             device.powerUsage,
                             device.workingTime,
                             device.isFlexible ? "Elastyczne" : "Nieelastyczne",
                             device.getPreferredStartTime());
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
        System.out.printf("%4d | %16.2f | %14.2f%n",
                         currentGeneration.getGenerationNumber(),
                         currentGeneration.getBestFitness(),
                         currentGeneration.getAverageFitness());

        // Ewolucja przez 99 kolejnych pokoleń (razem 100)
        for (int i = 1; i < 100; i++) {
            currentGeneration = new Generation(currentGeneration, 50, 10);

            System.out.printf("%4d | %16.2f | %14.2f%n",
                    currentGeneration.getGenerationNumber(),
                    currentGeneration.getBestFitness(),
                    currentGeneration.getAverageFitness());
        }

        // Szczegółowe wyniki końcowe
        System.out.println("\n=== Wyniki końcowe ===");
        currentGeneration.printGenerationStats();

        System.out.println("\n=== Najlepszy chromosom ===");
        Chromosome bestChromosome = currentGeneration.getBestChromosome();

        // Podsumowanie ewolucji
        System.out.println("\n=== Podsumowanie ewolucji ===");
        List<Generation> allGenerations = Generation.getGenerationList();
        double firstGenBest = allGenerations.getFirst().getBestFitness();
        double lastGenBest = allGenerations.getLast().getBestFitness();
        double improvement = ((firstGenBest - lastGenBest) / firstGenBest) * 100;

        System.out.printf("Pierwsza generacja - najlepsza fitness: %.2f%n", firstGenBest);
        System.out.printf("Ostatnia generacja - najlepsza fitness: %.2f%n", lastGenBest);
        System.out.printf("Poprawa: %.2f (%.1f%%)%n", firstGenBest - lastGenBest, improvement);
    }

    /**
     * Eksportuje dane do pliku CSV
     * @param filename nazwa pliku CSV
     * @return true jeśli eksport się powiódł, false w przeciwnym razie
     */
    public static boolean exportToCSV(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            // Nagłówek CSV
            writer.append("Nr_Pokolenia,Najlepsza_Fitness,Koszt_Energii_PLN\n");

            // Dane z każdego pokolenia
            List<Generation> allGenerations = Generation.getGenerationList();
            for (Generation generation : allGenerations) {
                writer.append(String.valueOf(generation.getGenerationNumber())).append(",");
                writer.append(String.format("%.4f", generation.getBestFitness())).append(",");

                // Obliczenie kosztu energii (fitness / 1.2 - usunięcie penalty)
                double energyCost = generation.getBestFitness() / 1.2;
                writer.append(String.format("%.4f", energyCost)).append("\n");
            }

            // Dodanie sekcji z informacjami o urządzeniach
            writer.append("\n\n# Informacje o urzadzeniach\n");
            writer.append("Nazwa,Zuzycie_kWh,Czas_Pracy_h,Elastyczne,Preferowany_Start\n");
            
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            for (Device device : Device.deviceList) {
                writer.append(device.name).append(",");
                writer.append(String.format("%.1f", device.powerUsage)).append(",");
                writer.append(String.format("%.1f", device.workingTime)).append(",");
                writer.append(device.isFlexible ? "TAK" : "NIE").append(",");
                
                if (device.getPreferredStartTime() != null) {
                    writer.append(device.getPreferredStartTime().format(timeFormatter));
                } else {
                    writer.append("BRAK");
                }
                writer.append("\n");
            }

            // Dodanie sekcji z informacjami o taryfach
            writer.append("\n\n# Informacje o taryfach\n");
            writer.append("Nazwa,Cena_PLN_kWh,Poczatek,Koniec\n");
            
            for (Tariff tariff : Tariff.tarrifsList) {
                writer.append(tariff.name).append(",");
                writer.append(String.format("%.4f", tariff.price)).append(",");
                writer.append(tariff.startTime.format(timeFormatter)).append(",");
                writer.append(tariff.endTime.format(timeFormatter)).append("\n");
            }

            // Podsumowanie
            if (!allGenerations.isEmpty()) {
                writer.append("\n\n# Podsumowanie\n");
                writer.append("Parametr,Wartosc\n");
                writer.append("Liczba_pokolen,").append(String.valueOf(allGenerations.size())).append("\n");
                writer.append("Pierwsza_fitness,").append(String.format("%.4f", allGenerations.getFirst().getBestFitness())).append("\n");
                writer.append("Ostatnia_fitness,").append(String.format("%.4f", allGenerations.getLast().getBestFitness())).append("\n");
                
                double improvement = allGenerations.getFirst().getBestFitness() - allGenerations.getLast().getBestFitness();
                double improvementPercent = (improvement / allGenerations.getFirst().getBestFitness()) * 100;
                writer.append("Poprawa_bezwzgledna,").append(String.format("%.4f", improvement)).append("\n");
                writer.append("Poprawa_procentowa,").append(String.format("%.2f", improvementPercent)).append("\n");
                
                double optimizedCost = allGenerations.getLast().getBestFitness() / 1.2;
            }

            System.out.println("Dane zostały wyeksportowane do pliku: " + filename);
            return true;

        } catch (IOException e) {
            System.err.println("Błąd podczas eksportu do CSV: " + e.getMessage());
            return false;
        }
    }

    /**
     * Uruchamia algorytm genetyczny i zwraca listę najlepszych fitness z każdego pokolenia
     * @return lista wartości fitness dla wykresu
     */
    public static List<Double> runGeneticAlgorithmForChart() {
        List<Double> fitnessHistory = new ArrayList<>();
        
        // Resetowanie licznika generacji
        Generation.resetGenerationCounter();

        // Utworzenie pierwszej generacji
        Generation currentGeneration = new Generation(50, 10);
        fitnessHistory.add(currentGeneration.getBestFitness());

        // Ewolucja przez 99 kolejnych pokoleń (razem 100)
        for (int i = 1; i < 100; i++) {
            currentGeneration = new Generation(currentGeneration, 50, 10);
            fitnessHistory.add(currentGeneration.getBestFitness());
        }

        return fitnessHistory;
    }
}