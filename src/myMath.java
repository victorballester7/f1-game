public class myMath {
  public static double[] sum(double[]... arrays) { // sum of arrays
    double[] result = new double[arrays[0].length];
    for (double[] array : arrays) {
      for (int i = 0; i < array.length; i++)
        result[i] += array[i];
    }
    return result;
  }

  public static double[] scalarProd(double a, double... values) { // arrays multiplied by a constant 'a'
    double[] result = new double[values.length];
    for (int i = 0; i < values.length; i++)
      result[i] = values[i] * a;
    return result;
  }
}
