package PowerUsageOptimizer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public class ChartPanel extends JPanel {
    private List<Double> fitnessData;
    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    
    // Marginesy wykresu
    private static final int MARGIN_LEFT = 80;
    private static final int MARGIN_RIGHT = 50;
    private static final int MARGIN_TOP = 50;
    private static final int MARGIN_BOTTOM = 80;
    
    public ChartPanel(List<Double> data, String title, String xAxisLabel, String yAxisLabel) {
        this.fitnessData = new ArrayList<>(data);
        this.title = title;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (fitnessData == null || fitnessData.isEmpty()) {
            g.setColor(Color.BLACK);
            g.drawString("Brak danych do wyświetlenia", getWidth()/2 - 80, getHeight()/2);
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Wymiary obszaru wykresu
        int chartWidth = getWidth() - MARGIN_LEFT - MARGIN_RIGHT;
        int chartHeight = getHeight() - MARGIN_TOP - MARGIN_BOTTOM;
        int chartX = MARGIN_LEFT;
        int chartY = MARGIN_TOP;
        
        // Znajdź min i max wartości
        double minFitness = fitnessData.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxFitness = fitnessData.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        
        // Dodaj margines do osi Y
        double range = maxFitness - minFitness;
        minFitness -= range * 0.1;
        maxFitness += range * 0.1;
        
        // Rysowanie tytułu
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics titleMetrics = g2d.getFontMetrics();
        int titleWidth = titleMetrics.stringWidth(title);
        g2d.drawString(title, (getWidth() - titleWidth) / 2, 25);
        
        // Rysowanie osi
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        
        // Oś X
        g2d.drawLine(chartX, chartY + chartHeight, chartX + chartWidth, chartY + chartHeight);
        // Oś Y
        g2d.drawLine(chartX, chartY, chartX, chartY + chartHeight);
        
        // Etykiety osi
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics labelMetrics = g2d.getFontMetrics();
        
        // Etykieta osi X
        int xLabelWidth = labelMetrics.stringWidth(xAxisLabel);
        g2d.drawString(xAxisLabel, chartX + (chartWidth - xLabelWidth) / 2, getHeight() - 20);
        
        // Etykieta osi Y (obrócona)
        Graphics2D g2dRotated = (Graphics2D) g2d.create();
        g2dRotated.rotate(-Math.PI/2);
        int yLabelWidth = labelMetrics.stringWidth(yAxisLabel);
        g2dRotated.drawString(yAxisLabel, -(chartY + (chartHeight + yLabelWidth) / 2), 20);
        g2dRotated.dispose();
        
        // Rysowanie znaczników na osi X
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        FontMetrics tickMetrics = g2d.getFontMetrics();
        int maxTicks = 10;
        int tickInterval = Math.max(1, fitnessData.size() / maxTicks);
        
        for (int i = 0; i < fitnessData.size(); i += tickInterval) {
            int x = chartX + (i * chartWidth) / (fitnessData.size() - 1);
            g2d.drawLine(x, chartY + chartHeight, x, chartY + chartHeight + 5);
            
            String tickLabel = String.valueOf(i + 1);
            int tickWidth = tickMetrics.stringWidth(tickLabel);
            g2d.drawString(tickLabel, x - tickWidth/2, chartY + chartHeight + 18);
        }
        
        // Rysowanie znaczników na osi Y
        int yTicks = 8;
        for (int i = 0; i <= yTicks; i++) {
            double value = minFitness + (maxFitness - minFitness) * i / yTicks;
            int y = chartY + chartHeight - (i * chartHeight) / yTicks;
            
            g2d.drawLine(chartX - 5, y, chartX, y);
            
            String tickLabel = String.format("%.1f", value);
            int tickWidth = tickMetrics.stringWidth(tickLabel);
            g2d.drawString(tickLabel, chartX - tickWidth - 10, y + 4);
        }
        
        // Rysowanie linii wykresu
        if (fitnessData.size() > 1) {
            g2d.setColor(new Color(30, 144, 255)); // DodgerBlue
            g2d.setStroke(new BasicStroke(2));
            
            for (int i = 0; i < fitnessData.size() - 1; i++) {
                // Pozycje punktów
                int x1 = chartX + (i * chartWidth) / (fitnessData.size() - 1);
                int y1 = chartY + chartHeight - (int)((fitnessData.get(i) - minFitness) * chartHeight / (maxFitness - minFitness));
                
                int x2 = chartX + ((i + 1) * chartWidth) / (fitnessData.size() - 1);
                int y2 = chartY + chartHeight - (int)((fitnessData.get(i + 1) - minFitness) * chartHeight / (maxFitness - minFitness));
                
                g2d.draw(new Line2D.Double(x1, y1, x2, y2));
            }
            
            // Rysowanie punktów
            g2d.setColor(new Color(255, 69, 0)); // OrangeRed
            for (int i = 0; i < fitnessData.size(); i++) {
                int x = chartX + (i * chartWidth) / (fitnessData.size() - 1);
                int y = chartY + chartHeight - (int)((fitnessData.get(i) - minFitness) * chartHeight / (maxFitness - minFitness));
                
                g2d.fillOval(x - 3, y - 3, 6, 6);
            }
        }
        
        // Dodanie siatki
        g2d.setColor(new Color(220, 220, 220));
        g2d.setStroke(new BasicStroke(1));
        
        // Linie pionowe siatki
        for (int i = 0; i < fitnessData.size(); i += tickInterval) {
            int x = chartX + (i * chartWidth) / (fitnessData.size() - 1);
            g2d.drawLine(x, chartY, x, chartY + chartHeight);
        }
        
        // Linie poziome siatki
        for (int i = 0; i <= yTicks; i++) {
            int y = chartY + chartHeight - (i * chartHeight) / yTicks;
            g2d.drawLine(chartX, y, chartX + chartWidth, y);
        }
        
        // Informacje o najlepszym i najgorszym wyniku
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        
        double firstFitness = fitnessData.getFirst();
        double lastFitness = fitnessData.getLast();
        double improvement = firstFitness - lastFitness;
        double improvementPercent = (improvement / firstFitness) * 100;
        
        String[] stats = {
            String.format("Pierwsze pokolenie: %.2f", firstFitness),
            String.format("Ostatnie pokolenie: %.2f", lastFitness),
            String.format("Poprawa: %.2f (%.1f%%)", improvement, improvementPercent),
            String.format("Liczba pokoleń: %d", fitnessData.size())
        };
        
        int statsY = chartY + 20;
        for (String stat : stats) {
            g2d.drawString(stat, chartX + chartWidth - 200, statsY);
            statsY += 15;
        }
    }

    public void updateData(List<Double> newData) {
        this.fitnessData = new ArrayList<>(newData);
        repaint();
    }
}
