package PowerUsageOptimizer;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class GUI {
    private JFrame frame;
    private JTextArea outputArea;
    private JLabel deviceCountLabel;
    private JButton runAlgorithmButton;
    private JButton displayDevicesButton;
    private JButton showBestScheduleButton;
    private JButton exitButton;
    private JButton showChartButton;
    private List<Double> lastFitnessData;

    public GUI() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Power Usage Optimizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout());

        runAlgorithmButton = new JButton("Uruchom Algorytm");
        displayDevicesButton = new JButton("Pokaż Urządzenia");
        showBestScheduleButton = new JButton("Pokaż Optymalny Harmonogram");
        showChartButton = new JButton("Pokaż Wykres Ewolucji");
        exitButton = new JButton("Wyjdź");
        deviceCountLabel = new JLabel();

        buttonPanel.add(runAlgorithmButton);
        buttonPanel.add(displayDevicesButton);
        buttonPanel.add(showBestScheduleButton);
        buttonPanel.add(deviceCountLabel);
        buttonPanel.add(showChartButton);
        buttonPanel.add(exitButton);


        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        addButtonActions();

        //promptForDeviceData();
        updateDeviceCountLabel();

        frame.setVisible(true);
    }

    private void addButtonActions() {
        runAlgorithmButton.addActionListener(e -> runAlgorithm());
        displayDevicesButton.addActionListener(e -> displayDevices());
        showBestScheduleButton.addActionListener(e -> showBestSchedule());
        showChartButton.addActionListener(e -> showEvolutionChart());
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void showEvolutionChart() {
        if (lastFitnessData == null || lastFitnessData.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "Najpierw uruchom algorytm genetyczny, aby wygenerować dane do wykresu.",
                    "Brak danych",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Utworzenie nowego okna dla wykresu
        JFrame chartFrame = new JFrame("Wykres Ewolucji - Najlepsze Rozwiązania");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.setSize(900, 700);
        chartFrame.setLocationRelativeTo(frame);

        // Dodanie panelu wykresu
        ChartPanel chartPanel = new ChartPanel(
                lastFitnessData,
                "Ewolucja Najlepszego Rozwiązania",
                "Numer Pokolenia",
                "Wartość Fitness"
        );

        chartFrame.add(chartPanel);
        chartFrame.setVisible(true);
    }


    private void runAlgorithm() {
        outputArea.setText("");
        appendToOutput("=== Uruchamianie algorytmu genetycznego ===");

        Generation.resetGenerationCounter();
        Generation currentGeneration = new Generation(50, 10);
        List<Generation> generations = new ArrayList<>();
        generations.add(currentGeneration);

        List<Double> fitnessHistory = new ArrayList<>();
        appendToOutput("\n=== Ewolucja ===");
        appendToOutput("Gen. | Najlepsza fitness | Średnia fitness");
        appendToOutput("-----|-------------------|----------------");
        appendToOutput(String.format("%4d | %16.2f | %14.2f",
                currentGeneration.getGenerationNumber(),
                currentGeneration.getBestFitness(),
                currentGeneration.getAverageFitness()));

        fitnessHistory.add(currentGeneration.getBestFitness());
        for (int i = 0; i < 100; i++) {
            currentGeneration = new Generation(currentGeneration, 50, 10);
            fitnessHistory.add(currentGeneration.getBestFitness());
            generations.add(currentGeneration);
                appendToOutput(String.format("%4d | %16.2f | %14.2f",
                        currentGeneration.getGenerationNumber(),
                        currentGeneration.getBestFitness(),
                        currentGeneration.getAverageFitness()));
        }

        appendToOutput("\n=== Wyniki końcowe ===");

        Chromosome bestChromosome = currentGeneration.getBestChromosome();

        if (generations.size() >= 2) {
            double firstBest = generations.getFirst().getBestFitness();
            double lastBest = generations.getLast().getBestFitness();
            double improvement = firstBest - lastBest;
            double improvementPercent = (improvement / firstBest) * 100;

            appendToOutput("\n=== Postęp ewolucji ===");
            appendToOutput(String.format("Pierwsza generacja - najlepsza fitness: %.2f", firstBest));
            appendToOutput(String.format("Ostatnia generacja - najlepsza fitness: %.2f", lastBest));
            appendToOutput(String.format("Poprawa: %.2f (%.1f%%)", improvement, improvementPercent));
        }
        this.lastFitnessData = fitnessHistory;
    }

    private void displayDevices() {
        outputArea.setText("");
        appendToOutput("=== Lista urządzeń ===");

        for (Device device : Device.deviceList) {
            String start = device.getPreferredStartTime() != null
                    ? device.getPreferredStartTime().toString()
                    : "Brak (dowolny czas)";
            appendToOutput(String.format("%-20s | %.1f kWh | %.1f h | %s | Start: %s",
                    device.name,
                    device.powerUsage,
                    device.workingTime,
                    device.isFlexible ? "Elastyczne" : "Nieelastyczne",
                    start));
        }
    }

    private void showBestSchedule() {
        outputArea.setText("");
        appendToOutput("=== Optymalny harmonogram urządzeń ===");

        Generation currentGeneration = new Generation(50, 10);
        for (int i = 1; i < 100; i++) {
            currentGeneration = new Generation(currentGeneration, 50, 10);
        }

        Chromosome bestChromosome = currentGeneration.getBestChromosome();
        if (bestChromosome != null) {
            appendToOutput("Harmonogram urządzeń:");

            for (int i = 0; i < bestChromosome.chromosomeList.size(); i++) {
                Device d = bestChromosome.chromosomeList.get(i);
                if (d.getPreferredStartTime() != null) {
                    LocalTime startTime = d.getPreferredStartTime();
                    LocalTime endTime = startTime.plusMinutes((long) (d.workingTime * 60));
                    appendToOutput(String.format("Device %d: %s | %s - %s | Flexible: %s | Power: %.1fkWh",
                            i, d.name, startTime, endTime, d.isFlexible, d.powerUsage));
                } else {
                    appendToOutput(String.format("Device %d: %s | Elastyczne (czas dobierany) | Power: %.1fkWh",
                            i, d.name, d.powerUsage));
                }
            }

            List<String> conflicts = new ArrayList<>();
            for (int i = 0; i < bestChromosome.chromosomeList.size(); i++) {
                Device d1 = bestChromosome.chromosomeList.get(i);
                if (d1.getPreferredStartTime() == null) continue;

                int start1 = d1.getPreferredStartTime().getHour() * 60 + d1.getPreferredStartTime().getMinute();
                int end1 = start1 + (int) (d1.workingTime * 60);

                for (int j = i + 1; j < bestChromosome.chromosomeList.size(); j++) {
                    Device d2 = bestChromosome.chromosomeList.get(j);
                    if (d2.getPreferredStartTime() == null) continue;

                    int start2 = d2.getPreferredStartTime().getHour() * 60 + d2.getPreferredStartTime().getMinute();
                    int end2 = start2 + (int) (d2.workingTime * 60);

                    if (!(end1 <= start2 || start1 >= end2)) {
                        conflicts.add("KONFLIKT: " + d1.name + " (" + d1.getPreferredStartTime() +
                                "-" + d1.getPreferredStartTime().plusMinutes((long) (d1.workingTime * 60)) +
                                ") nakłada się z " + d2.name + " (" + d2.getPreferredStartTime() +
                                "-" + d2.getPreferredStartTime().plusMinutes((long) (d2.workingTime * 60)) + ")");
                    }
                }
            }

            if (conflicts.isEmpty()) {
                appendToOutput("\n✓ Brak konfliktów czasowych");
            } else {
                appendToOutput("\n⚠ Znaleziono konflikty:");
                for (String conflict : conflicts) {
                    appendToOutput("  " + conflict);
                }
            }
        }
    }

    private double calculateNaiveCost() {
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

    private void appendToOutput(String text) {
        outputArea.append(text + "\n");
    }

    private void promptForDeviceData() {
        try {
            int count = Integer.parseInt(JOptionPane.showInputDialog(frame, "Ile urządzeń chcesz dodać?", "Dane urządzeń", JOptionPane.QUESTION_MESSAGE));
            for (int i = 0; i < count; i++) {
                String name = JOptionPane.showInputDialog(frame, "Nazwa urządzenia #" + (i + 1));
                double powerUsage = Double.parseDouble(JOptionPane.showInputDialog(frame, "Zużycie energii (kWh)"));
                double workingTime = Double.parseDouble(JOptionPane.showInputDialog(frame, "Czas pracy (h)"));
                boolean isFlexible = JOptionPane.showConfirmDialog(frame, "Czy urządzenie jest elastyczne?", "Elastyczność", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

                LocalTime startTime = null;
                String timeStr = JOptionPane.showInputDialog(
                        frame,
                        isFlexible
                                ? "Preferowana godzina rozpoczęcia (HH:mm)\n(Pozostaw puste, aby pozostawić dowolny czas)"
                                : "Godzina rozpoczęcia (HH:mm)"
                );

                if (timeStr != null && !timeStr.isBlank()) {
                    startTime = LocalTime.parse(timeStr);
                }

                Device.deviceList.add(new Device(name, powerUsage, workingTime, isFlexible, startTime));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Błąd w danych wejściowych urządzeń: " + e.getMessage());
        }
    }

    private void updateDeviceCountLabel() {
        deviceCountLabel.setText("Urządzenia: " + Device.deviceList.size());
    }
}
