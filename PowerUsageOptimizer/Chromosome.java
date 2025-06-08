package PowerUsageOptimizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.time.LocalTime;

public class Chromosome {

    List<Device> chromosomeList = new ArrayList<>();

    static int dataSize = 11;
    int length;
    public static int maxValue = 1440;
    public static int minValue = 0;
    public static double pm = 0.1;
    private static Random random = new Random();

    public Chromosome() {
        length = Device.deviceList.size();
        // Inicjalizacja chromosomeList
        for (int i = 0; i < length; i++) {
            chromosomeList.add(Device.deviceList.get(i));
        }
    }

    public void setGene(int index, Device device) {
        if (index >= 0 && index < length) {
            chromosomeList.set(index, device);
        } else {
            System.out.println("Index out of bounds");
        }
    }

    public double getFitness() {
        double fitness = 0.0;
        for(Device d: chromosomeList) {
            fitness += d.getPowerPrice();
        }
        return fitness;
    }

    /**
     * Krzyżowanie wielopunktowe dwóch chromosomów
     * @param parent1 pierwszy rodzic
     * @param parent2 drugi rodzic
     * @param crossingCount liczba punktów przecięcia
     * @return tablica dwóch potomków
     */
    public static Chromosome[] crossover(Chromosome parent1, Chromosome parent2, int crossingCount) {
        if (parent1.length != parent2.length) {
            throw new IllegalArgumentException("Chromosomy muszą mieć tę samą długość");
        }
        
        if (crossingCount <= 0 || crossingCount >= parent1.length) {
            throw new IllegalArgumentException("Liczba przecięć musi być większa od 0 i mniejsza od długości chromosomu");
        }

        Chromosome offspring1 = new Chromosome();
        Chromosome offspring2 = new Chromosome();

        // Generowanie punktów przecięcia
        List<Integer> crossoverPoints = new ArrayList<>();
        for (int i = 0; i < crossingCount; i++) {
            int point;
            do {
                point = random.nextInt(parent1.length - 1) + 1; // od 1 do length-1
            } while (crossoverPoints.contains(point));
            crossoverPoints.add(point);
        }
        crossoverPoints.sort(Integer::compareTo);

        // Krzyżowanie
        boolean useParent1 = true;
        int currentCrossoverIndex = 0;
        
        for (int i = 0; i < parent1.length; i++) {
            // Sprawdź czy osiągnęliśmy punkt przecięcia
            if (currentCrossoverIndex < crossoverPoints.size() && 
                i == crossoverPoints.get(currentCrossoverIndex)) {
                useParent1 = !useParent1;
                currentCrossoverIndex++;
            }

            if (useParent1) {
                offspring1.chromosomeList.set(i, copyDevice(parent1.chromosomeList.get(i)));
                offspring2.chromosomeList.set(i, copyDevice(parent2.chromosomeList.get(i)));
            } else {
                offspring1.chromosomeList.set(i, copyDevice(parent2.chromosomeList.get(i)));
                offspring2.chromosomeList.set(i, copyDevice(parent1.chromosomeList.get(i)));
            }
        }

        return new Chromosome[]{offspring1, offspring2};
    }

    /**
     * Mutacja chromosomu - mutuje geny z prawdopodobieństwem pm
     */
    public void mutate() {
        for (int i = 0; i < chromosomeList.size(); i++) {
            if (random.nextDouble() < pm) {
                mutateGene(i);
            }
        }
    }

    /**
     * Mutacja pojedynczego genu (urządzenia) na pozycji index
     * @param index pozycja genu do mutacji
     */
    private void mutateGene(int index) {
        if (index < 0 || index >= chromosomeList.size()) {
            return;
        }

        Device device = chromosomeList.get(index);
        
        // Mutacja tylko dla urządzeń elastycznych
        if (device.isFlexible) {
            // Generowanie nowego czasu startu (w minutach od 00:00)
            int newStartTimeMinutes = random.nextInt(maxValue);
            
            // Konwersja na LocalTime
            int hours = newStartTimeMinutes / 60;
            int minutes = newStartTimeMinutes % 60;
            
            // Sprawdzenie czy wartości są w poprawnym zakresie
            if (hours >= 24) {
                hours = hours % 24;
            }
            if (minutes >= 60) {
                minutes = minutes % 60;
            }
            
            LocalTime newStartTime = LocalTime.of(hours, minutes);
            device.setPreferredStartTime(newStartTime);

            // Konwersja na reprezentację binarną (11 bitów)
            char[] binaryTime = convertToBinary(newStartTimeMinutes);
            device.setStartTime(binaryTime);
        }
    }

    /**
     * Naprawa uszkodzonych genów - sprawdza czy wartości są w poprawnym zakresie
     * i czy nie ma konfliktów czasowych
     */
    public void repairGenes() {
        // Najpierw naprawa podstawowych wartości
        for (Device device : chromosomeList) {
            if (device.startTime != null) {
                // Konwersja z binarnego na dziesiętny
                int timeInMinutes = binaryToDecimal(device.startTime);
                
                // Sprawdzenie czy wartość jest większa od maksymalnej
                if (timeInMinutes > maxValue) {
                    // Naprawa - ustawienie na losową wartość z poprawnego zakresu
                    int repairedTime = new Random().nextInt(maxValue + 1);
                    device.startTime = convertToBinary(repairedTime);
                    
                    // Aktualizacja preferredStartTime
                    int hours = repairedTime / 60;
                    int minutes = repairedTime % 60;
                    if (hours >= 24) hours = hours % 24;
                    if (minutes >= 60) minutes = minutes % 60;
                    
                    device.setPreferredStartTime(LocalTime.of(hours, minutes));
                }
                
                // Sprawdzenie czy wartość jest mniejsza od minimalnej
                if (timeInMinutes < minValue) {
                    device.startTime = convertToBinary(minValue);
                    device.setPreferredStartTime(LocalTime.of(0, 0));
                }
            }
        }
        
        // Następnie naprawa konfliktów czasowych
        repairTimeConflicts();
    }
    
    /**
     * Naprawia konflikty czasowe - zapewnia, że żadne dwa urządzenia nie pracują w tym samym czasie
     */
    public void repairTimeConflicts() {
        List<TimeSlot> occupiedSlots = new ArrayList<>();
        
        // Sortuj urządzenia według priorytetu (nieelastyczne pierwsze, potem według czasu pracy)
        List<DeviceWithIndex> sortedDevices = new ArrayList<>();
        for (int i = 0; i < chromosomeList.size(); i++) {
            sortedDevices.add(new DeviceWithIndex(chromosomeList.get(i), i));
        }
        
        // Sortowanie: nieelastyczne pierwsze, potem według czasu pracy (dłużej pracujące pierwsze)
        sortedDevices.sort((d1, d2) -> {
            if (d1.device.isFlexible != d2.device.isFlexible) {
                return Boolean.compare(d1.device.isFlexible, d2.device.isFlexible);
            }
            return Double.compare(d2.device.workingTime, d1.device.workingTime);
        });
        
        // Sprawdź i napraw konflikty dla każdego urządzenia
        for (DeviceWithIndex deviceWithIndex : sortedDevices) {
            Device device = deviceWithIndex.device;
            int deviceIndex = deviceWithIndex.index;
            
            if (device.preferredStartTime != null && device.workingTime > 0) {
                LocalTime startTime = device.preferredStartTime;
                int startMinutes = startTime.getHour() * 60 + startTime.getMinute();
                int endMinutes = startMinutes + (int)(device.workingTime * 60);
                
                // Sprawdź konflikty z już zarezerwowanymi slotami
                if (hasTimeConflict(startMinutes, endMinutes, occupiedSlots)) {
                    if (device.isFlexible) {
                        // Znajdź pierwszy wolny slot dla elastycznego urządzenia
                        int[] freeSlot = findFirstFreeSlot((int)(device.workingTime * 60), occupiedSlots);
                        if (freeSlot != null) {
                            int newStartMinutes = freeSlot[0];
                            int hours = newStartMinutes / 60;
                            int minutes = newStartMinutes % 60;
                            
                            if (hours >= 24) hours = hours % 24;
                            if (minutes >= 60) minutes = minutes % 60;
                            
                            LocalTime newStartTime = LocalTime.of(hours, minutes);
                            device.setPreferredStartTime(newStartTime);
                            device.setStartTime(convertToBinary(newStartMinutes));
                            
                            // Aktualizuj czasy końcowe
                            startMinutes = newStartMinutes;
                            endMinutes = startMinutes + (int)(device.workingTime * 60);
                        }
                    }
                    // Dla nieelastycznych urządzeń pozostawiamy oryginalny czas
                }
                
                // Dodaj slot do listy zajętych
                occupiedSlots.add(new TimeSlot(startMinutes, endMinutes, deviceIndex));
            }
        }
    }
    
    /**
     * Sprawdza czy dany przedział czasowy koliduje z już zajętymi slotami
     * @param startMinutes początek w minutach
     * @param endMinutes koniec w minutach
     * @param occupiedSlots lista zajętych slotów
     * @return true jeśli jest konflikt
     */
    private boolean hasTimeConflict(int startMinutes, int endMinutes, List<TimeSlot> occupiedSlots) {
        for (TimeSlot slot : occupiedSlots) {
            // Sprawdź czy przedziały się nakładają
            if (!(endMinutes <= slot.startMinutes || startMinutes >= slot.endMinutes)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Znajduje pierwszy wolny slot o podanej długości
     * @param durationMinutes długość potrzebnego slotu w minutach
     * @param occupiedSlots lista zajętych slotów
     * @return tablica [start, end] lub null jeśli nie znaleziono
     */
    private int[] findFirstFreeSlot(int durationMinutes, List<TimeSlot> occupiedSlots) {
        // Sortuj zajęte sloty według czasu rozpoczęcia
        List<TimeSlot> sortedSlots = new ArrayList<>(occupiedSlots);
        sortedSlots.sort(Comparator.comparingInt(slot -> slot.startMinutes));
        
        int currentTime = 0; // Zaczynamy od północy
        
        for (TimeSlot slot : sortedSlots) {
            // Sprawdź czy jest miejsce przed tym slotem
            if (slot.startMinutes - currentTime >= durationMinutes) {
                return new int[]{currentTime, currentTime + durationMinutes};
            }
            currentTime = Math.max(currentTime, slot.endMinutes);
        }
        
        // Sprawdź czy jest miejsce po ostatnim slocie
        if (maxValue - currentTime >= durationMinutes) {
            return new int[]{currentTime, currentTime + durationMinutes};
        }
        
        return null; // Nie znaleziono wolnego miejsca
    }
    
    /**
     * Klasa pomocnicza do przechowywania informacji o zajętym czasie
     */
    private static class TimeSlot {
        int startMinutes;
        int endMinutes;
        int deviceIndex;
        
        TimeSlot(int startMinutes, int endMinutes, int deviceIndex) {
            this.startMinutes = startMinutes;
            this.endMinutes = endMinutes;
            this.deviceIndex = deviceIndex;
        }
    }
    
    /**
     * Klasa pomocnicza do sortowania urządzeń z zachowaniem indeksu
     */
    private static class DeviceWithIndex {
        Device device;
        int index;
        
        DeviceWithIndex(Device device, int index) {
            this.device = device;
            this.index = index;
        }
    }

    /**
     * Konwersja liczby na reprezentację binarną (11 bitów)
     * @param value wartość do konwersji
     * @return tablica znaków reprezentująca liczbę w systemie binarnym
     */
    private char[] convertToBinary(int value) {
        String binary = String.format("%" + dataSize + "s", Integer.toBinaryString(value)).replace(' ', '0');
        if (binary.length() > dataSize) {
            binary = binary.substring(binary.length() - dataSize);
        }
        return binary.toCharArray();
    }

    /**
     * Konwersja z reprezentacji binarnej na liczbę dziesiętną
     * @param binaryArray tablica znaków reprezentująca liczbę binarną
     * @return wartość dziesiętna
     */
    private int binaryToDecimal(char[] binaryArray) {
        int result = 0;
        for (int i = 0; i < binaryArray.length; i++) {
            if (binaryArray[i] == '1') {
                result += Math.pow(2, binaryArray.length - 1 - i);
            }
        }
        return result;
    }

    /**
     * Tworzy kopię urządzenia
     * @param original oryginalne urządzenie
     * @return kopia urządzenia
     */
    private static Device copyDevice(Device original) {
        Device copy = new Device(original.name, original.powerUsage, 
                                original.workingTime, original.isFlexible, 
                                original.preferredStartTime);
        if (original.startTime != null) {
            copy.startTime = original.startTime.clone();
        }
        return copy;
    }

    /**
     * Metoda pomocnicza do wyświetlania chromosomu z informacjami o konfliktach
     */
    public void printChromosome() {
        System.out.println("Chromosome fitness: " + getFitness());
        System.out.println("Harmonogram urządzeń:");
        
        for (int i = 0; i < chromosomeList.size(); i++) {
            Device d = chromosomeList.get(i);
            if (d.preferredStartTime != null) {
                LocalTime startTime = d.getPreferredStartTime();
                LocalTime endTime = startTime.plusMinutes((long)(d.workingTime * 60));
                
                System.out.println("Device " + i + ": " + d.name + 
                                 " | " + startTime + " - " + endTime +
                                 " | Flexible: " + d.isFlexible +
                                 " | Power: " + d.powerUsage + "kWh");
            }
        }
        
        // Sprawdź i wyświetl konflikty
        checkAndDisplayConflicts();
    }
    
    /**
     * Sprawdza i wyświetla konflikty czasowe
     */
    public void checkAndDisplayConflicts() {
        List<String> conflicts = new ArrayList<>();
        
        for (int i = 0; i < chromosomeList.size(); i++) {
            Device device1 = chromosomeList.get(i);
            if (device1.preferredStartTime == null || device1.workingTime <= 0) continue;
            
            int start1 = device1.preferredStartTime.getHour() * 60 + device1.preferredStartTime.getMinute();
            int end1 = start1 + (int)(device1.workingTime * 60);
            
            for (int j = i + 1; j < chromosomeList.size(); j++) {
                Device device2 = chromosomeList.get(j);
                if (device2.preferredStartTime == null || device2.workingTime <= 0) continue;
                
                int start2 = device2.preferredStartTime.getHour() * 60 + device2.preferredStartTime.getMinute();
                int end2 = start2 + (int)(device2.workingTime * 60);
                
                // Sprawdź nakładanie się
                if (!(end1 <= start2 || start1 >= end2)) {
                    conflicts.add("KONFLIKT: " + device1.name + " (" + 
                                device1.preferredStartTime + "-" + 
                                device1.preferredStartTime.plusMinutes((long)(device1.workingTime * 60)) + 
                                ") nakłada się z " + device2.name + " (" +
                                device2.preferredStartTime + "-" + 
                                device2.preferredStartTime.plusMinutes((long)(device2.workingTime * 60)) + ")");
                }
            }
        }
        
        if (conflicts.isEmpty()) {
            System.out.println("✓ Brak konfliktów czasowych");
        } else {
            System.out.println("⚠ Znaleziono konflikty:");
            for (String conflict : conflicts) {
                System.out.println("  " + conflict);
            }
        }
    }
}