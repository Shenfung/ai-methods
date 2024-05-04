import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class BestFit {

    public static void main(String[] args) {
        Input input = new Input();
        input.getBinsFromTextFile();
        List<Problem> problems = input.problems;
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Problem problem : problems) {
            Result result = solveProblem(problem);
            System.out.println("Total cost for problem " + problem.id + ": " + result.solution.size());
            System.out.println("Iterations for problem " + problem.id + ": " + result.iterations);
            dataset.addValue(result.iterations, "Iterations", " " + problem.id);
            dataset.addValue(result.solution.size(), "Cost", " " + problem.id);
        }
        plotBarChart(dataset);
    }

    private static Result solveProblem(Problem problem) {
        List<Bin> currentSolution = new ArrayList<>();
        List<Item> remainingItems = new ArrayList<>(problem.items);
        int iterationCount = 0;

        while (!remainingItems.isEmpty()) {
            iterationCount++;

            Item currentItem = remainingItems.get(0);

            Bin bestFitBin = null;
            int minRemainingCapacity = Integer.MAX_VALUE;

            for (Bin bin : currentSolution) {
                int remainingCapacity = bin.getRemainingCapacity();
                if (remainingCapacity >= currentItem.weight && remainingCapacity < minRemainingCapacity) {
                    bestFitBin = bin;
                    minRemainingCapacity = remainingCapacity;
                }
            }

            if (bestFitBin != null) {
                bestFitBin.addItem(currentItem.weight, 1);
                remainingItems.remove(currentItem);
            } else {
                Bin newBin = new Bin(problem.capacityOfEachBin);
                newBin.addItem(currentItem.weight, 1);
                currentSolution.add(newBin);
                remainingItems.remove(currentItem);
            }
        }

        return new Result(currentSolution, iterationCount);
    }

    private static class Result {
        List<Bin> solution;
        int iterations;

        Result(List<Bin> solution, int iterations) {
            this.solution = solution;
            this.iterations = iterations;
        }
    }

    private static void plotBarChart(DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart(
                "Best Fit",
                "Problem ID",
                "Iterations",
                dataset
        );
        BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        JFrame frame = new JFrame("Best Fit");
        frame.setContentPane(chartPanel);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
