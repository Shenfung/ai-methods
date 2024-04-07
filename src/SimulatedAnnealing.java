import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

public class SimulatedAnnealing {
    private static final int INITIAL_TEMPERATURE = 1000;
    private static final double COOLING_RATE = 0.0003;

    public static void main(String[] args) {
        Input input = new Input();
        input.getBinsFromTextFile();

        List<Problem> problems = input.problems;
        for (Problem problem : problems) {
            List<Bin> initialSolution = new ArrayList<>();
            for (int i = 0; i < problem.items.size(); i++) {
                Bin bin = new Bin(problem.capacityOfEachBin);
                bin.addItem(problem.items.get(i).weight, 1);
                initialSolution.add(bin);
            }

            List<Bin> bestSolution = simulatedAnnealing(initialSolution, problem);
            System.out.println("Total cost for problem " + problem.id + ": " + calculateTotalCost(bestSolution));
        }
    }

    private static List<Bin> simulatedAnnealing(List<Bin> initialSolution, Problem problem) {
        List<Bin> currentSolution = new ArrayList<>(initialSolution);
        List<Bin> bestSolution = new ArrayList<>(currentSolution);
        double temperature = INITIAL_TEMPERATURE;
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("Current solution");
        while (temperature > 1) {
            List<Bin> newSolution = getNeighborSolution(currentSolution);
            double currentEnergy = calculateTotalCost(currentSolution);
            double newEnergy = calculateTotalCost(newSolution);

            if (acceptanceProbability(currentEnergy, newEnergy, temperature) > Math.random()) {
                currentSolution = newSolution;
                if (newEnergy < calculateTotalCost(bestSolution)) {
                    bestSolution = new ArrayList<>(newSolution);
                }
            }
            series.add(temperature, calculateTotalCost(currentSolution));
            temperature *= 1 - COOLING_RATE;
        }
        dataset.addSeries(series);
        plotChart(dataset, problem);
        return bestSolution;
    }

    private static List<Bin> getNeighborSolution(List<Bin> solution) {
        List<Bin> neighborSolution = new ArrayList<>(solution);
        Random random = new Random();

        Bin bin1 = neighborSolution.get(random.nextInt(neighborSolution.size()));
        Bin bin2 = neighborSolution.get(random.nextInt(neighborSolution.size()));
        if (!bin1.items.isEmpty()) {
            Item item = bin1.items.get(random.nextInt(bin1.items.size()));
            if (bin2.getRemainingCapacity() >= item.weight) {
                bin1.removeItem(item.weight);
                bin2.addItem(item.weight, 1);
            }
        }
        neighborSolution.removeIf(bin -> bin.items.isEmpty());
        return neighborSolution;
    }

    private static double calculateTotalCost(List<Bin> solution) {
        return solution.size();
    }

    private static double acceptanceProbability(double currentEnergy, double newEnergy, double temperature) {
        if (newEnergy < currentEnergy) {
            return 1.0;
        }
        return Math.exp((currentEnergy - newEnergy) / temperature);
    }

    private static void plotChart(XYSeriesCollection dataset, Problem problem) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Simulated Annealing for problem " + problem.id,
                "Temperature",
                "Cost",
                dataset
        );
        chart.getXYPlot().getDomainAxis().setInverted(true);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        JFrame frame = new JFrame("Cost function");
        frame.setContentPane(chartPanel);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
