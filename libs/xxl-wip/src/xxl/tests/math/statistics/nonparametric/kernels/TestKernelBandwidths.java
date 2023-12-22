package xxl.tests.math.statistics.nonparametric.kernels;

import java.util.ArrayList;
import java.util.Random;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.cursors.mappers.ReservoirSampler;
import xxl.core.math.Statistics;
import xxl.core.math.statistics.nonparametric.kernels.EpanechnikowKernel;
import xxl.core.math.statistics.nonparametric.kernels.KernelBandwidths;
import xxl.core.math.statistics.parametric.aggregates.ReservoirSample;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class KernelBandwidths.
 */
public class TestKernelBandwidths {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example                                */
		/*********************************************************************/
		ArrayList<Double> entirety = new ArrayList<Double>();
		Random r=new Random();
		for(int i=0;i<20;i++) {
			entirety.add(r.nextDouble());
		}		
		// drawing sample
		Aggregator it = new ReservoirSampler(entirety.iterator(), new ReservoirSample(4, new ReservoirSample.XType(4)));
		Object[] sample = (Object[]) it.last();
		System.out.println("sample");
		for (int i = 0; i < sample.length; i++) {
			System.out.println(sample[i]);
		}
		System.out.println("---------------------------------");
		// computing bandwidth for the sample		
		System.out.println("normal scale rule applied with Epanechnikow Kernel: "
				+KernelBandwidths.normalScaleRule(sample.length,new EpanechnikowKernel(),Statistics.variance(sample)));
	}

}
