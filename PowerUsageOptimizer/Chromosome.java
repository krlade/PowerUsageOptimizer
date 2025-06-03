package PowerUsageOptimizer;

import java.util.Random;

public class Chromosome {

    int length;
    int dataSize;
    int precision;
    double maxValue;
    double minValue;
    int steps;
    double range;
    double pm = 0.1;
    char[][] genes;

    public double fitness;

    public Chromosome() {
    }

    public Chromosome(double minValue, double maxValue, int precision, int length) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.precision = precision;
        this.length = length;
        this.range = maxValue - minValue;
        this.steps = (int) Math.floor(range * precision) + 1;
        this.dataSize = (int) Math.ceil(Math.log(steps) / Math.log(2));
        this.genes = new char[length][];
    }

    public Chromosome(double minValue, double maxValue, int precision, int length, int pm) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.precision = precision;
        this.length = length;
        this.range = maxValue - minValue;
        this.steps = (int) Math.floor(range * precision) + 1;
        this.dataSize = (int) Math.ceil(Math.log(steps) / Math.log(2));
        this.genes = new char[length][];
    }


    public void setGene(int index, double value) {
        if (index >= 0 && index < length) {
            int encodedValue = (int) ((value - minValue) / range * steps);
            String stringValue = String.format("%14s", Integer.toString(encodedValue, 2)).replace(" ", "0");
            genes[index] = stringValue.toCharArray();
        } else {
            System.out.println("Index out of bounds");
        }
    }

    public double decodeGene(String gene) {
        int encodedValue = Integer.parseInt(gene, 2);
        double decodedValue = ((double) encodedValue / steps) * range + minValue;
        return Math.floor(decodedValue * precision) / precision;
    }

    public void setGene(int index, String value) {
        if (index >= 0 && index < length) {
            genes[index] = value.toCharArray();
        } else {
            System.out.println("Index out of bounds");
        }
    }

    public void setGenome(String value) {
        if(value.length() == length*dataSize) {
            for(int i=0; i < length; i++) {
                setGene(i, value.substring(dataSize*i, dataSize*(i+1)));
            }
        }
    }

    public String getGene(int index) {
        if (index >= 0 && index < length) {
            return String.valueOf(genes[index]);
        } else {
            System.out.println("Index out of bounds");
            return null;
        }
    }
    public String getGenome() {
        StringBuilder genome = new StringBuilder();
        for(int i=0; i < length; i++) {
                genome.append(getGene(i));
        }
        return genome.toString();
    }

    public Chromosome makeChild(Chromosome spouse, Random rnd, int crossingCount) {
        int cut;
        Chromosome child = new Chromosome(minValue, maxValue, precision, length);
        String g1, g2;
        g1 = this.getGenome();
        g2 = spouse.getGenome();
        for(int i=0; i < crossingCount; i++) {
            cut = (int) Math.floor(rnd.nextDouble(0, 1)*length*dataSize/crossingCount) + i*length*dataSize/crossingCount;
            g1 = g1.substring(0, cut) +g2.substring(cut, length*dataSize);
        }

        child.setGenome(g1);
        int geneErr = child.checkGenome();
        while(geneErr != -1) {
            repairGenome(geneErr);
            geneErr = child.checkGenome();
        }
        return child;
    }

    public static void initializeChromosom(Chromosome c) {
        Random rnd = new Random();
        double var;

        for(int i=0; i < c.length; i++) {
            var = Math.floor(rnd.nextDouble(c.minValue, c.maxValue)* c.precision)/ c.precision;
            c.setGene(i, var);
        }
    }

    public void mutate(Random rnd) {
        StringBuilder geneCopy;
        char[] gene;
        double odds;
        for(int i=0; i < length; i++) {
            gene = genes[i];
            geneCopy = new StringBuilder();
            for(char bit: gene) {
                odds = rnd.nextDouble(0, 1);
                if(odds < pm) {
                    if (bit == '0') {
                        geneCopy.append('1');
                    } else {
                        geneCopy.append('0');
                    }
                } else {
                    geneCopy.append(bit);
                }
            }
            genes[i] = geneCopy.toString().toCharArray();
        }
    }

    public int checkGenome() {
        for(int i=0; i < length; i++) {
            double val = decodeGene(String.valueOf(genes[i]));
            if (val > maxValue && val < minValue) {
                System.out.printf("Error at gene %d", i);
                return i;
            }
        }
        return -1;
    }

    public void repairGenome(int i) {
        String badGene = getGene(i);
        badGene = badGene.substring(1, dataSize)+"0";
        setGenome(badGene);
    }

    @Override
    public String toString() {
        return getGenome();
    }
}