package PowerUsageOptimizer;

import java.util.*;

public class Generation {
    
    // Statyczne pola klasy
    private static final List<Generation> generationList = new ArrayList<>();
    private static int generationIterator = 0;
    private static final Comparator<Chromosome> fitnessComparator = 
        Comparator.comparingDouble(Chromosome::getFitness);
    
    // Pola instancji
    private List<Chromosome> chromosomes;
    private int populationSize = 20;
    private int promotionSize = 5;
    private int generationNumber = 100;
    private Random random;
    
    /**
     * Konstruktor dla pierwszej generacji - tworzy losową populację
     * @param populationSize rozmiar populacji
     * @param promotionSize liczba najlepszych chromosomów do promocji
     */
    public Generation(int populationSize, int promotionSize) {
        this.populationSize = populationSize;
        this.promotionSize = promotionSize;
        this.generationNumber = generationIterator++;
        this.chromosomes = new ArrayList<>();
        this.random = new Random();
        
        // Tworzenie losowej pierwszej generacji
        generateRandomPopulation();
        
        // Dodanie do listy wszystkich generacji
        generationList.add(this);
    }
    
    /**
     * Konstruktor dla kolejnych generacji - tworzy nową generację na podstawie poprzedniej
     * @param previousGeneration poprzednia generacja
     * @param populationSize rozmiar nowej populacji
     * @param promotionSize liczba najlepszych chromosomów do promocji
     */
    public Generation(Generation previousGeneration, int populationSize, int promotionSize) {
        this.populationSize = populationSize;
        this.promotionSize = promotionSize;
        this.generationNumber = generationIterator++;
        this.chromosomes = new ArrayList<>();
        this.random = new Random();
        
        // Pobranie najlepszych chromosomów z poprzedniej generacji
        List<Chromosome> bestChromosomes = previousGeneration.getBestChromosomes(promotionSize);
        
        // Krzyżowanie najlepszych chromosomów
        crossoverBestChromosomes(bestChromosomes);
        
        // Uzupełnienie populacji do pełnego rozmiaru
        fillPopulationToSize();
        
        // Mutacja całej populacji
        mutatePopulation();
        
        // Naprawa genów
        repairAllGenes();
        
        // Dodanie do listy wszystkich generacji
        generationList.add(this);
    }

    /**
     * Generuje losową populację dla pierwszej generacji
     */
    private void generateRandomPopulation() {
        for (int i = 0; i < populationSize; i++) {
            Chromosome chromosome = new Chromosome();
            // Inicjalizacja chromosomu losowymi wartościami
            initializeRandomChromosome(chromosome);
            chromosomes.add(chromosome);
        }
    }
    
    /**
     * Inicjalizuje chromosom losowymi wartościami
     * @param chromosome chromosom do inicjalizacji
     */
    private void initializeRandomChromosome(Chromosome chromosome) {
        for (int i = 0; i < chromosome.chromosomeList.size(); i++) {
            Device device = chromosome.chromosomeList.get(i);
            if (device.isFlexible) {
                // Losowy czas startu dla elastycznych urządzeń
                int randomStartTime = random.nextInt(Chromosome.maxValue + 1);
                int hours = randomStartTime / 60;
                int minutes = randomStartTime % 60;
                
                if (hours >= 24) hours = hours % 24;
                if (minutes >= 60) minutes = minutes % 60;
                
                device.setPreferredStartTime(java.time.LocalTime.of(hours, minutes));
                device.setStartTime(convertToBinary(randomStartTime));
            }
        }
    }
    
    /**
     * Krzyżuje najlepsze chromosomy między sobą
     * @param bestChromosomes lista najlepszych chromosomów
     */
    private void crossoverBestChromosomes(List<Chromosome> bestChromosomes) {
        // Dodanie najlepszych chromosomów do nowej populacji (elityzm)
        for (Chromosome chromosome : bestChromosomes) {
            chromosomes.add(copyChromosome(chromosome));
        }
        
        // Krzyżowanie najlepszych chromosomów
        int crossoverCount = populationSize - promotionSize;
        int currentOffspring = 0;
        
        while (currentOffspring < crossoverCount && chromosomes.size() < populationSize) {
            // Wybór dwóch losowych rodziców z najlepszych
            int parent1Index = random.nextInt(bestChromosomes.size());
            int parent2Index = random.nextInt(bestChromosomes.size());
            
            // Upewnienie się, że rodzice są różni
            while (parent1Index == parent2Index && bestChromosomes.size() > 1) {
                parent2Index = random.nextInt(bestChromosomes.size());
            }
            
            Chromosome parent1 = bestChromosomes.get(parent1Index);
            Chromosome parent2 = bestChromosomes.get(parent2Index);
            
            // Krzyżowanie
            Chromosome[] offspring = Chromosome.crossover(parent1, parent2, Device.deviceList.size()-1);
            
            // Dodanie potomków do populacji
            if (chromosomes.size() < populationSize) {
                chromosomes.add(offspring[0]);
                currentOffspring++;
            }
            if (chromosomes.size() < populationSize) {
                chromosomes.add(offspring[1]);
                currentOffspring++;
            }
        }
    }
    
    /**
     * Uzupełnia populację do pełnego rozmiaru losowymi chromosomami
     */
    private void fillPopulationToSize() {
        while (chromosomes.size() < populationSize) {
            Chromosome randomChromosome = new Chromosome();
            initializeRandomChromosome(randomChromosome);
            chromosomes.add(randomChromosome);
        }
    }
    
    /**
     * Mutuje całą populację
     */
    private void mutatePopulation() {
        for (Chromosome chromosome : chromosomes) {
            chromosome.mutate();
        }
    }
    
    /**
     * Naprawia geny we wszystkich chromosomach
     */
    private void repairAllGenes() {
        for (Chromosome chromosome : chromosomes) {
            chromosome.repairGenes();
        }
    }
    
    /**
     * Zwraca n najlepszych chromosomów z generacji
     * @param n liczba chromosomów do zwrócenia
     * @return lista n najlepszych chromosomów
     */
    public List<Chromosome> getBestChromosomes(int n) {
        if (n <= 0) {
            return new ArrayList<>();
        }
        
        // Sortowanie chromosomów według fitness (rosnąco - mniejsze fitness = lepsze)
        List<Chromosome> sortedChromosomes = new ArrayList<>(chromosomes);
        sortedChromosomes.sort(fitnessComparator);
        
        // Zwrócenie n najlepszych
        int count = Math.min(n, sortedChromosomes.size());
        return new ArrayList<>(sortedChromosomes.subList(0, count));
    }
    
    /**
     * Zwraca najlepszy chromosom z generacji
     * @return najlepszy chromosom
     */
    public Chromosome getBestChromosome() {
        List<Chromosome> best = getBestChromosomes(1);
        return best.isEmpty() ? null : best.getFirst();
    }
    
    /**
     * Zwraca średnią fitness populacji
     * @return średnia fitness
     */
    public double getAverageFitness() {
        if (chromosomes.isEmpty()) {
            return 0.0;
        }
        
        double sum = chromosomes.stream()
                .mapToDouble(Chromosome::getFitness)
                .sum();
        return sum / chromosomes.size();
    }
    
    /**
     * Zwraca najlepszą fitness w generacji
     * @return najlepsza fitness
     */
    public double getBestFitness() {
        Chromosome best = getBestChromosome();
        return best != null ? best.getFitness() : Double.MAX_VALUE;
    }
    
    /**
     * Tworzy kopię chromosomu
     * @param original oryginalny chromosom
     * @return kopia chromosomu
     */
    private Chromosome copyChromosome(Chromosome original) {
        Chromosome copy = new Chromosome();
        for (int i = 0; i < original.chromosomeList.size(); i++) {
            Device originalDevice = original.chromosomeList.get(i);
            Device copyDevice = new Device(originalDevice.name, originalDevice.powerUsage,
                    originalDevice.workingTime, originalDevice.isFlexible,
                    originalDevice.preferredStartTime);
            if (originalDevice.startTime != null) {
                copyDevice.startTime = originalDevice.startTime.clone();
            }
            copy.chromosomeList.set(i, copyDevice);
        }
        return copy;
    }
    
    /**
     * Konwersja liczby na reprezentację binarną (11 bitów)
     */
    private char[] convertToBinary(int value) {
        String binary = String.format("%" + Chromosome.dataSize + "s", 
                Integer.toBinaryString(value)).replace(' ', '0');
        if (binary.length() > Chromosome.dataSize) {
            binary = binary.substring(binary.length() - Chromosome.dataSize);
        }
        return binary.toCharArray();
    }
    
    /**
     * Wyświetla statystyki generacji
     */
    public void printGenerationStats() {
        System.out.println("=== Generacja " + generationNumber + " ===");
        System.out.println("Rozmiar populacji: " + chromosomes.size());
        System.out.println("Najlepsza fitness: " + getBestFitness());
        System.out.println("Średnia fitness: " + String.format("%.2f", getAverageFitness()));
        System.out.println("Rozmiar promocji: " + promotionSize);
    }
    
    /**
     * Wyświetla najlepsze chromosomy
     * @param count liczba chromosomów do wyświetlenia
     */
    public void printBestChromosomes(int count) {
        List<Chromosome> best = getBestChromosomes(count);
        System.out.println("=== " + count + " najlepszych chromosomów ===");
        for (int i = 0; i < best.size(); i++) {
            System.out.println((i + 1) + ". Fitness: " + best.get(i).getFitness());
        }
    }
    
    // Gettery i settery
    public static List<Generation> getGenerationList() {
        return new ArrayList<>(generationList);
    }
    
    public static int getGenerationIterator() {
        return generationIterator;
    }
    
    public static Comparator<Chromosome> getFitnessComparator() {
        return fitnessComparator;
    }
    
    public List<Chromosome> getChromosomes() {
        return new ArrayList<>(chromosomes);
    }
    
    public int getPopulationSize() {
        return populationSize;
    }
    
    public int getPromotionSize() {
        return promotionSize;
    }
    
    public int getGenerationNumber() {
        return generationNumber;
    }
    
    /**
     * Resetuje licznik generacji (przydatne do testów)
     */
    public static void resetGenerationCounter() {
        generationIterator = 0;
        generationList.clear();
    }
}