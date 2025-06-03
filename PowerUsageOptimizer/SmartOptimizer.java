package PowerUsageOptimizer;

import java.util.ArrayList;
import java.util.List;

public class SmartOptimizer {

    List<SmartOptimizer> generationList = new ArrayList<>();
    static int currentGeneration = 0;

    SmartOptimizer(SmartOptimizer ancestor) {
        // Copy constructor to create a new instance based on an ancestor
        this.generationList = new ArrayList<>(ancestor.generationList);
    }

}
