import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYDataset;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class LineChartPanel extends JPanel {
    private JFreeChart chart;
    private XYSeriesCollection dataset;
    private org.jfree.chart.ChartPanel chartPanel;
    private XYPlot plot;
    
    // Stack颜色映射，与HexagonDrawer完全一致
    private static final Map<String, Color> STACK_COLORS = new HashMap<>();
    static {
        STACK_COLORS.put("WILDS", new Color(0x51ad2f));    // Green: #51ad2f
        STACK_COLORS.put("WASTES", new Color(0xa17a6d));   // Brown: #a17a6d  
        STACK_COLORS.put("DEVA", new Color(0x0ca3dd));      // Blue: #0ca3dd
        STACK_COLORS.put("DEVB", new Color(0xe172d3));      // Pink: #e172d3
        // 添加小写变体
        STACK_COLORS.put("wilds", new Color(0x51ad2f));
        STACK_COLORS.put("wastes", new Color(0xa17a6d));
        STACK_COLORS.put("deva", new Color(0x0ca3dd));
        STACK_COLORS.put("devb", new Color(0xe172d3));
    }
    
    // 数据系列
    private XYSeries wildsSeries;
    private XYSeries wastesSeries;
    private XYSeries devASeries;
    private XYSeries devBSeries;
    
    // 动态绘制相关
    private boolean dynamicMode = false;
    private int currentDynamicRound = 0;
    private MultiRoundGameData multiRoundData; // 保存完整数据引用
    private boolean axisRangeSet = false; // 标记是否已设置轴范围
    
    public LineChartPanel() {
        initializeChart();
        setupLayout();
    }
    
    private void initializeChart() {
        // 创建数据系列
        wildsSeries = new XYSeries("Wilds");
        wastesSeries = new XYSeries("Wastes");
        devASeries = new XYSeries("DevA");
        devBSeries = new XYSeries("DevB");
        
        // 创建数据集
        dataset = new XYSeriesCollection();
        dataset.addSeries(wildsSeries);
        dataset.addSeries(wastesSeries);
        dataset.addSeries(devASeries);
        dataset.addSeries(devBSeries);
        
        // 创建折线图
        chart = ChartFactory.createXYLineChart(
            null,                   // 无标题
            "Round",                // X轴标签
            "Stack Count",          // Y轴标签
            dataset,                // 数据集
            PlotOrientation.VERTICAL,
            true,                   // 显示图例
            true,                   // 显示工具提示
            false                   // 不生成URL
        );
        
        // 设置图表样式
        chart.setBackgroundPaint(Color.WHITE);
        plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        
        // 设置字体
        Font labelFont = new Font("Arial", Font.PLAIN, 11);
        plot.getDomainAxis().setLabelFont(labelFont);
        plot.getRangeAxis().setLabelFont(labelFont);
        plot.getDomainAxis().setTickLabelFont(labelFont);
        plot.getRangeAxis().setTickLabelFont(labelFont);
        
        // 设置线条颜色
        plot.getRenderer().setSeriesPaint(0, STACK_COLORS.get("WILDS"));   // Wilds
        plot.getRenderer().setSeriesPaint(1, STACK_COLORS.get("WASTES"));  // Wastes
        plot.getRenderer().setSeriesPaint(2, STACK_COLORS.get("DEVA"));    // DevA
        plot.getRenderer().setSeriesPaint(3, STACK_COLORS.get("DEVB"));    // DevB
        
        // 设置线条粗细
        plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));
        plot.getRenderer().setSeriesStroke(1, new BasicStroke(2.0f));
        plot.getRenderer().setSeriesStroke(2, new BasicStroke(2.0f));
        plot.getRenderer().setSeriesStroke(3, new BasicStroke(2.0f));
        
        // 创建图表面板
        chartPanel = new org.jfree.chart.ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 250));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Stack Trends"));
        setBackground(new Color(240, 240, 240));
        
        add(chartPanel, BorderLayout.CENTER);
    }
    
    /**
     * 更新折线图数据
     * @param roundData 轮次数据，key为轮次，value为stack数量映射
     */
    public void updateChart(Map<Integer, Map<String, Integer>> roundData) {
        if (roundData == null || roundData.isEmpty()) {
            return;
        }
        
        // 清除现有数据
        wildsSeries.clear();
        wastesSeries.clear();
        devASeries.clear();
        devBSeries.clear();
        
        // 添加新数据
        for (Map.Entry<Integer, Map<String, Integer>> roundEntry : roundData.entrySet()) {
            int round = roundEntry.getKey();
            Map<String, Integer> stackCounts = roundEntry.getValue();
            
            wildsSeries.add(round, stackCounts.getOrDefault("WILDS", 0));
            wastesSeries.add(round, stackCounts.getOrDefault("WASTES", 0));
            devASeries.add(round, stackCounts.getOrDefault("DEVA", 0));
            devBSeries.add(round, stackCounts.getOrDefault("DEVB", 0));
        }
        
        // 如果不是动态模式，恢复自动缩放
        if (!dynamicMode) {
            restoreAutoScaling();
        }
    }
    
    /**
     * 从多轮游戏数据更新图表
     * @param multiRoundData 多轮游戏数据
     */
    public void updateFromMultiRoundData(MultiRoundGameData multiRoundData) {
        this.multiRoundData = multiRoundData; // 保存引用，用于动态模式
        
        if (multiRoundData == null || multiRoundData.getRounds() == null) {
            return;
        }
        
        Map<Integer, Map<String, Integer>> roundData = new HashMap<>();
        
        for (RoundData round : multiRoundData.getRounds()) {
            int roundNumber = round.getRound_number();
            Map<String, Integer> stackCounts = new HashMap<>();
            
            // 统计各种stack的数量
            for (HexData hex : round.getBoard().getHexes()) {
                String stackType = hex.getType();
                stackCounts.put(stackType, stackCounts.getOrDefault(stackType, 0) + 1);
            }
            
            roundData.put(roundNumber, stackCounts);
        }
        
        updateChart(roundData);
        
        // 如果当前是动态模式，需要重新设置轴范围
        if (dynamicMode) {
            axisRangeSet = false; // 重置轴范围标记，下次添加数据时会重新设置
        }
    }
    
    /**
     * 获取图表面板（用于外部访问）
     */
    public org.jfree.chart.ChartPanel getChartPanel() {
        return chartPanel;
    }
    
    /**
     * 设置动态模式
     */
    public void setDynamicMode(boolean enabled) {
        this.dynamicMode = enabled;
        if (enabled) {
            // 重置动态绘制状态
            currentDynamicRound = 0;
            clearAllSeries();
            axisRangeSet = false; // 重置轴范围标记
        } else {
            // 退出动态模式时，恢复自动缩放
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRange(true);
            axisRangeSet = false;
        }
    }
    
    /**
     * 检查是否处于动态模式
     */
    public boolean isDynamicMode() {
        return dynamicMode;
    }
    
    /**
     * 获取当前动态轮次
     */
    public int getCurrentDynamicRound() {
        return currentDynamicRound;
    }
    
    
    /**
     * 清空所有数据系列
     */
    private void clearAllSeries() {
        wildsSeries.clear();
        wastesSeries.clear();
        devASeries.clear();
        devBSeries.clear();
    }
    
    /**
     * 动态添加一轮数据
     */
    public void addDynamicRoundData(int roundNumber, Map<String, Integer> stackCounts) {
        System.out.println("=== addDynamicRoundData ===");
        System.out.println("dynamicMode: " + dynamicMode);
        System.out.println("roundNumber: " + roundNumber);
        System.out.println("currentDynamicRound: " + currentDynamicRound);
        System.out.println("stackCounts: " + stackCounts);
        
        if (!dynamicMode) return;
        
        // 如果这是第一轮数据，或者轮次是连续的，就添加数据
        if (currentDynamicRound == 0 || roundNumber == currentDynamicRound + 1) {
            System.out.println("Adding data for round " + roundNumber);
            wildsSeries.add(roundNumber, stackCounts.getOrDefault("WILDS", 0));
            wastesSeries.add(roundNumber, stackCounts.getOrDefault("WASTES", 0));
            devASeries.add(roundNumber, stackCounts.getOrDefault("DEVA", 0));
            devBSeries.add(roundNumber, stackCounts.getOrDefault("DEVB", 0));
            
            currentDynamicRound = roundNumber;
            
            System.out.println("Series sizes: Wilds=" + wildsSeries.getItemCount() + 
                             ", Wastes=" + wastesSeries.getItemCount() + 
                             ", DevA=" + devASeries.getItemCount() + 
                             ", DevB=" + devBSeries.getItemCount());
            
            // 第一次添加数据时，设置合适的Y轴范围
            if (!axisRangeSet && multiRoundData != null && multiRoundData.getRounds() != null) {
                System.out.println("Setting initial axis range...");
                setInitialAxisRange();
                axisRangeSet = true;
            }
            
            // 刷新图表
            chartPanel.repaint();
            System.out.println("Chart repainted");
        } else {
            System.out.println("Skipping round " + roundNumber + " (expected " + (currentDynamicRound + 1) + ")");
        }
        System.out.println("=== end addDynamicRoundData ===");
    }
    
    /**
     * 恢复自动缩放
     */
    public void restoreAutoScaling() {
        if (plot != null) {
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
            
            rangeAxis.setAutoRange(true);
            domainAxis.setAutoRange(true);
        }
    }
    
    
    /**
     * 设置初始轴范围（基于完整数据）
     */
    private void setInitialAxisRange() {
        System.out.println("=== setInitialAxisRange ===");
        if (multiRoundData == null || multiRoundData.getRounds() == null || multiRoundData.getRounds().isEmpty()) {
            System.out.println("No multiRoundData available");
            return;
        }
        
        // 计算所有轮次中的最大stack数量
        int maxStacks = 0;
        int totalRounds = multiRoundData.getRounds().size();
        System.out.println("Total rounds: " + totalRounds);
        
        for (RoundData round : multiRoundData.getRounds()) {
            // 统计当前轮的stack数量
            Map<String, Integer> stackCounts = new HashMap<>();
            for (HexData hex : round.getBoard().getHexes()) {
                String stackType = hex.getType();
                stackCounts.put(stackType, stackCounts.getOrDefault(stackType, 0) + 1);
            }
            
            System.out.println("Round " + round.getRound_number() + " stacks: " + stackCounts);
            
            // 更新最大值
            for (int count : stackCounts.values()) {
                maxStacks = Math.max(maxStacks, count);
            }
        }
        
        System.out.println("Max stacks found: " + maxStacks);
        
        // 设置Y轴范围，留一些边距
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRange(false);
        rangeAxis.setRange(0, maxStacks * 1.5); // 留50%的边距，确保不会冲出画面
        
        // 设置X轴范围
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRange(false);
        domainAxis.setRange(1, totalRounds); // 从1开始，到totalRounds
        
        System.out.println("Set axis ranges: X=[1, " + totalRounds + "], Y=[0, " + (maxStacks * 1.5) + "]");
        System.out.println("=== end setInitialAxisRange ===");
    }
    
    /**
     * 重置动态绘制状态
     */
    public void resetDynamicState() {
        currentDynamicRound = 0;
        axisRangeSet = false; // 重置轴范围标记
        if (dynamicMode) {
            clearAllSeries();
        }
    }
    
    /**
     * 设置动态图表到指定轮次（用于回退）
     */
    public void setDynamicRoundTo(int roundNumber) {
        if (!dynamicMode) {
            return;
        }
        
        // 清除所有数据
        clearAllSeries();
        currentDynamicRound = 0;
        axisRangeSet = false; // 重置轴范围标记，让下次添加数据时重新计算
        
        // 重新添加从第1轮到指定轮次的所有数据
        if (multiRoundData != null && multiRoundData.getRounds() != null) {
            for (int i = 0; i < roundNumber && i < multiRoundData.getRounds().size(); i++) {
                RoundData round = multiRoundData.getRounds().get(i);
                Map<String, Integer> stackCounts = new HashMap<>();
                
                // 统计各种stack的数量
                for (HexData hex : round.getBoard().getHexes()) {
                    String stackType = hex.getType();
                    stackCounts.put(stackType, stackCounts.getOrDefault(stackType, 0) + 1);
                }
                
                addDynamicRoundData(i + 1, stackCounts);
            }
        }
    }
    
    /**
     * 强制重新计算动态图表的轴范围（用于加载新数据时）
     */
    public void forceRecalculateAxisRange() {
        if (dynamicMode && multiRoundData != null) {
            axisRangeSet = false; // 重置轴范围标记
            // 重新计算轴范围
            setInitialAxisRange();
        }
    }
    
    /**
     * 设置multiRoundData引用（不显示图表）
     */
    public void setMultiRoundData(MultiRoundGameData multiRoundData) {
        this.multiRoundData = multiRoundData;
    }
    
    /**
     * 保存图表为PNG图片
     */
    public void saveChartAsPNG(File file) throws IOException {
        if (chart == null) {
            throw new IllegalStateException("No chart to save");
        }
        
        // 设置图表尺寸
        int width = 800;
        int height = 600;
        
        // 创建BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 绘制图表
        chart.draw(g2, new Rectangle(width, height));
        g2.dispose();
        
        // 保存为PNG
        ImageIO.write(image, "PNG", file);
    }
}
