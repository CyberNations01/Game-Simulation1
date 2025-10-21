import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;

public class ChartPanel extends JPanel {
    private JFreeChart chart;
    private DefaultPieDataset dataset;
    private org.jfree.chart.ChartPanel chartPanel;
    
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
    
    public ChartPanel() {
        initializeChart();
        setupLayout();
    }
    
    private void initializeChart() {
        // 创建初始数据集
        dataset = new DefaultPieDataset();
        
        // 创建饼状图（无标题）
        chart = ChartFactory.createPieChart(
            null,                   // 无标题
            dataset,               // 数据集
            true,                  // 显示图例
            true,                  // 显示工具提示
            false                  // 不生成URL
        );
        
        // 设置图表样式
        chart.setBackgroundPaint(Color.WHITE);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        
        // 移除小标签（百分比标签）
        plot.setLabelGenerator(null);
        
        // 设置字体，与GameInfoPanel保持一致
        Font chartFont = new Font("Arial", Font.PLAIN, 12);
        Font labelFont = new Font("Arial", Font.PLAIN, 11);
        
        plot.setLabelFont(labelFont);
        
        // 设置默认颜色
        plot.setSectionPaint("WILDS", STACK_COLORS.get("WILDS"));
        plot.setSectionPaint("WASTES", STACK_COLORS.get("WASTES"));
        plot.setSectionPaint("DEVA", STACK_COLORS.get("DEVA"));
        plot.setSectionPaint("DEVB", STACK_COLORS.get("DEVB"));
        
        // 创建图表面板
        chartPanel = new org.jfree.chart.ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(300, 250));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Stack Distribution Chart"));
        setBackground(new Color(240, 240, 240));
        
        add(chartPanel, BorderLayout.CENTER);
    }
    
    /**
     * 更新图表数据
     * @param stackCounts stack数量映射
     */
    public void updateChart(Map<String, Integer> stackCounts) {
        if (stackCounts == null || stackCounts.isEmpty()) {
            return;
        }
        
        // 清除现有数据
        dataset.clear();
        
        // 添加新数据
        for (Map.Entry<String, Integer> entry : stackCounts.entrySet()) {
            String stackType = entry.getKey();
            Integer count = entry.getValue();
            
            if (count > 0) {
                // 使用友好的标签名称
                String label = getFriendlyLabel(stackType);
                dataset.setValue(label, count);
                
                // 设置精确的颜色
                PiePlot plot = (PiePlot) chart.getPlot();
                Color color = getColorForStackType(stackType);
                plot.setSectionPaint(label, color);
            }
        }
        
        // 如果没有数据，显示默认信息
        if (dataset.getItemCount() == 0) {
            dataset.setValue("No Data", 1);
        }
    }
    
    /**
     * 从游戏数据更新图表
     * @param gameData 游戏数据
     */
    public void updateFromGameData(GameData gameData) {
        if (gameData == null || gameData.getBoard() == null) {
            return;
        }
        
        Map<String, Integer> stackCounts = new HashMap<>();
        
        // 统计各种stack的数量
        for (HexData hex : gameData.getBoard().getHexes()) {
            String stackType = hex.getType();
            stackCounts.put(stackType, stackCounts.getOrDefault(stackType, 0) + 1);
        }
        
        updateChart(stackCounts);
    }
    
    /**
     * 获取stack类型对应的精确颜色
     */
    private Color getColorForStackType(String stackType) {
        if (stackType == null) {
            return Color.GRAY;
        }
        
        // 首先尝试直接匹配
        Color color = STACK_COLORS.get(stackType);
        if (color != null) {
            return color;
        }
        
        // 尝试小写匹配
        color = STACK_COLORS.get(stackType.toLowerCase());
        if (color != null) {
            return color;
        }
        
        // 根据HexagonDrawer的逻辑进行映射
        switch (stackType.toLowerCase()) {
            case "wilds":
            case "wild":
                return new Color(0x51ad2f); // Green
            case "wastes":
            case "waste":
                return new Color(0xa17a6d); // Brown
            case "deva":
                return new Color(0x0ca3dd); // Blue
            case "devb":
                return new Color(0xe172d3); // Pink
            default:
                return Color.GRAY;
        }
    }
    
    /**
     * 获取友好的标签名称
     */
    private String getFriendlyLabel(String stackType) {
        if (stackType == null) {
            return "Unknown";
        }
        switch (stackType) {
            case "WILDS": return "Wilds";
            case "WASTES": return "Wastes";
            case "DEVA": return "DevA";
            case "DEVB": return "DevB";
            default: return stackType;
        }
    }
    
    /**
     * 获取图表面板（用于外部访问）
     */
    public org.jfree.chart.ChartPanel getChartPanel() {
        return chartPanel;
    }
}
