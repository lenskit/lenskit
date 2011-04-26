package org.grouplens.lenskit.norm;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.grouplens.lenskit.data.context.PackedRatingSnapshot;
import org.grouplens.lenskit.data.context.RatingSnapshot;
import org.grouplens.lenskit.data.dao.SimpleFileDAO;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Stefan Nelson-Lindall <stefan@cs.umn.edu>
 *
 */
public class UserVarianceNormalizerTest {
	
	final static RatingSnapshot rs;
	final static SparseVector userRatings;
	final static SparseVector uniformUserRatings;
	final static double MIN_DOUBLE_PRECISION = 0.00001;
	
	static {
		long[] keys = {0L, 1L, 2L};
		double[] values = {0., 2., 4.};
		userRatings = SparseVector.wrap(keys, values);
		double[] uniformValues = {2., 2., 2.};
		uniformUserRatings = SparseVector.wrap(keys, uniformValues);
		try {
			File tempFile = File.createTempFile("VURVN_junit", null);
			tempFile.deleteOnExit();
			PrintStream ps = new PrintStream(tempFile);
			ps.println("0,0,0");
			ps.println("0,1,1");
			ps.println("0,2,2");
			ps.println("0,3,3");
			ps.println("0,4,4");
			ps.println("0,5,5");
			ps.println("0,6,6");
			ps.println("1,0,3");
			ps.println("1,1,3");
			ps.println("1,2,3");
			ps.println("1,3,3");
			ps.println("1,4,3");
			ps.println("1,5,3");
			ps.println("1,6,3");
			SimpleFileDAO sfdao = new SimpleFileDAO(tempFile, ",");
			sfdao.openSession();
			rs = PackedRatingSnapshot.make(sfdao);
			sfdao.closeSession();
		} catch (IOException ioe) {
			throw new RuntimeException("Failed to initialize test data");
		}
	}

	@Test
	public void testRatingBuildContextConstructor() {
		UserVarianceNormalizer urvn = new UserVarianceNormalizer(0, null);
		Assert.assertEquals(0.0, urvn.globalVariance, 0.0);
		urvn = new UserVarianceNormalizer(3, rs);
		Assert.assertEquals(3.0, urvn.smoothing, 0.0);
		Assert.assertEquals(2.0, urvn.globalVariance, MIN_DOUBLE_PRECISION);
	}

	@Test
	public void testMakeTransformation() {
		UserVarianceNormalizer urvn;
		urvn = new UserVarianceNormalizer();
		VectorTransformation trans = urvn.makeTransformation(9001, userRatings);
		MutableSparseVector nUR = userRatings.mutableCopy();
		final double mean = 2.0;
		final double stdev = Math.sqrt(8.0 / 3.0);
		trans.apply(nUR);
		//Test apply
		Assert.assertEquals((0.0 - mean) / stdev, nUR.get(0L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals((2.0 - mean) / stdev, nUR.get(1L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals((4.0 - mean) / stdev, nUR.get(2L), MIN_DOUBLE_PRECISION);
		trans.unapply(nUR);
		//Test unapply
		Assert.assertEquals( 0.0, nUR.get(0L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals( 2.0, nUR.get(1L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals( 4.0, nUR.get(2L), MIN_DOUBLE_PRECISION);
	}

	@Test
	public void testUniformRatings() {
		UserVarianceNormalizer urvn;
		urvn = new UserVarianceNormalizer();
		VectorTransformation trans = urvn.makeTransformation(9001, uniformUserRatings);
		MutableSparseVector nUR = userRatings.mutableCopy();
		trans.apply(nUR);
		//Test apply
		Assert.assertEquals( 0.0, nUR.get(0L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals( 0.0, nUR.get(1L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals( 0.0, nUR.get(2L), MIN_DOUBLE_PRECISION);
		trans.unapply(nUR);
		//Test unapply
		Assert.assertEquals( 2.0, nUR.get(0L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals( 2.0, nUR.get(1L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals( 2.0, nUR.get(2L), MIN_DOUBLE_PRECISION);
	}	
	
	@Test
	public void testSmoothing() {
		UserVarianceNormalizer urvn;
		urvn = new UserVarianceNormalizer(3, rs);
		VectorTransformation trans = urvn.makeTransformation(9001, userRatings);
		MutableSparseVector nUR = userRatings.mutableCopy();
		final double mean = 2.0;
		final double stdev = Math.sqrt(7.0/3.0);
		trans.apply(nUR);
		//Test apply
		Assert.assertEquals((0.0 - mean) / stdev, nUR.get(0L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals((2.0 - mean) / stdev, nUR.get(1L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals((4.0 - mean) / stdev, nUR.get(2L), MIN_DOUBLE_PRECISION);
		trans.unapply(nUR);
		//Test unapply
		Assert.assertEquals( 0.0, nUR.get(0L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals( 2.0, nUR.get(1L), MIN_DOUBLE_PRECISION);
		Assert.assertEquals( 4.0, nUR.get(2L), MIN_DOUBLE_PRECISION);
	}

}
