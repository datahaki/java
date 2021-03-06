// code by jph
package ch.ethz.idsc.tensor.fig;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.api.ScalarUnaryOperator;
import ch.ethz.idsc.tensor.ext.HomeDirectory;
import ch.ethz.idsc.tensor.pdf.RandomVariate;
import ch.ethz.idsc.tensor.pdf.UniformDistribution;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ enum ListPlotDemo {
  ;
  private static final ScalarUnaryOperator suoX = s -> Quantity.of(s, "s");
  private static final ScalarUnaryOperator suoY = s -> Quantity.of(s, "m");

  public static void main(String[] args) throws IOException {
    Tensor values1 = RandomVariate.of(UniformDistribution.unit(), 5);
    Tensor values2 = RandomVariate.of(UniformDistribution.unit(), 15);
    Tensor values3 = RandomVariate.of(UniformDistribution.unit(), 10);
    VisualSet visualSet = new VisualSet();
    Tensor domain1 = RandomVariate.of(UniformDistribution.unit(), values1.length());
    VisualRow visualRow1 = visualSet.add(domain1.map(suoX), values1.map(suoY));
    visualRow1.setLabel("first");
    Tensor domain2 = RandomVariate.of(UniformDistribution.unit(), values2.length());
    visualSet.add(domain2.map(suoX), values2.map(suoY));
    Tensor domain3 = RandomVariate.of(UniformDistribution.unit(), values3.length());
    visualSet.add(domain3.map(suoX), values3.map(suoY));
    Tensor domain4 = Tensors.vector(1, 3, 2, 5, 4).multiply(RealScalar.of(0.2));
    visualSet.add(domain4.map(suoX), domain4.map(suoY));
    /* amodeus specific */
    // ChartFactory.setChartTheme(ChartTheme.STANDARD);
    {
      JFreeChart jFreeChart = ListPlot.of(visualSet, true);
      jFreeChart.setBackgroundPaint(Color.WHITE);
      File file = HomeDirectory.Pictures(ListPlot.class.getSimpleName() + ".png");
      ChartUtils.saveChartAsPNG(file, jFreeChart, 500, 300);
    }
  }
}
