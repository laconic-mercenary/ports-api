package com.frontier.ports.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.frontier.lib.NullParameterException;

public class DataFragmentorTest {

	@Test
	public void testSimpleFrag() {
		DataFragmentor df = new DataFragmentor();
		byte[] data = new byte[5];
		data[0] = 1;
		data[1] = 2;
		data[2] = 3;
		data[3] = 4;
		data[4] = 5;
		List<int[]> result = df.determineIndices(data, 2);
		Assert.assertEquals(3, result.size());
		Assert.assertEquals(0, result.get(0)[0]);
		Assert.assertEquals(2, result.get(1)[0]);
		Assert.assertEquals(4, result.get(2)[0]);
	}
	
	@Test
	public void testSimpleFrag2() {
		DataFragmentor df = new DataFragmentor();
		byte[] data = new byte[5];
		data[0] = 1;
		data[1] = 2;
		data[2] = 3;
		data[3] = 4;
		data[4] = 5;
		List<int[]> result = df.determineIndices(data, 3);
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(0, result.get(0)[0]);
		Assert.assertEquals(2, result.get(0)[1]);
		Assert.assertEquals(3, result.get(1)[0]);
		Assert.assertEquals(4, result.get(1)[1]);
	}
	
	@Test
	public void testSimpleFrag3() {
		DataFragmentor df = new DataFragmentor();
		byte[] data = new byte[5];
		data[0] = 1;
		data[1] = 2;
		data[2] = 3;
		data[3] = 4;
		data[4] = 5;
		List<int[]> result = df.determineIndices(data, 5);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(0, result.get(0)[0]);
		Assert.assertEquals(4, result.get(0)[1]);
	}
	

	@Test
	public void testSimpleFrag4() {
		DataFragmentor df = new DataFragmentor();
		byte[] data = new byte[5];
		data[0] = 1;
		data[1] = 2;
		data[2] = 3;
		data[3] = 4;
		data[4] = 5;
		List<int[]> result = df.determineIndices(data, 6);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(0, result.get(0)[0]);
		Assert.assertEquals(4, result.get(0)[1]);
	}
	
	@Test
	public void testSimpleFrag5() {
		DataFragmentor df = new DataFragmentor();
		byte[] data = new byte[5];
		data[0] = 1;
		data[1] = 2;
		data[2] = 3;
		data[3] = 4;
		data[4] = 5;
		List<int[]> result = df.determineIndices(data, 8);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(0, result.get(0)[0]);
		Assert.assertEquals(4, result.get(0)[1]);
	}
	
	@Test
	public void testSimpleFrag6() {
		DataFragmentor df = new DataFragmentor();
		byte[] data = new byte[5];
		data[0] = 1;
		data[1] = 2;
		data[2] = 3;
		data[3] = 4;
		data[4] = 5;
		List<int[]> result = df.determineIndices(data, 4);
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(0, result.get(0)[0]);
		Assert.assertEquals(3, result.get(0)[1]);
		Assert.assertEquals(4, result.get(1)[0]);
		Assert.assertEquals(4, result.get(1)[1]);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSimpleFrag7() {
		DataFragmentor df = new DataFragmentor();
		byte[] data = new byte[5];
		data[0] = 1;
		data[1] = 2;
		data[2] = 3;
		data[3] = 4;
		data[4] = 5;
		df.determineIndices(data, 0);
	}
	
	@Test(expected=NullParameterException.class)
	public void testSimpleFrag8() {
		DataFragmentor df = new DataFragmentor();
		df.determineIndices(null, 1);
	}

	@Test
	public void testFrag() {
		DataFragmentor df = new DataFragmentor();
		byte[] data = new byte[550];
		for (int i = 0; i < 550; i++)
			data[i] = (byte)i;
		List<int[]> result = df.determineIndices(data, 100);
		Assert.assertEquals(6, result.size());
		Assert.assertEquals(0, result.get(0)[0]);
		Assert.assertEquals(99, result.get(0)[1]);
		Assert.assertEquals(100, result.get(1)[0]);
		Assert.assertEquals(199, result.get(1)[1]);
		Assert.assertEquals(200, result.get(2)[0]);
		Assert.assertEquals(299, result.get(2)[1]);
		Assert.assertEquals(300, result.get(3)[0]);
		Assert.assertEquals(399, result.get(3)[1]);
		Assert.assertEquals(400, result.get(4)[0]);
		Assert.assertEquals(499, result.get(4)[1]);
		Assert.assertEquals(500, result.get(5)[0]);
		Assert.assertEquals(549, result.get(5)[1]);
	}
	
	@Test
	public void testFrag2() {
		DataFragmentor df = new DataFragmentor();
		byte[] data = new byte[551];
		for (int i = 0; i < data.length; i++)
			data[i] = (byte)i;
		List<int[]> result = df.determineIndices(data, 100);
		Assert.assertEquals(6, result.size());
		Assert.assertEquals(0, result.get(0)[0]);
		Assert.assertEquals(99, result.get(0)[1]);
		Assert.assertEquals(100, result.get(1)[0]);
		Assert.assertEquals(199, result.get(1)[1]);
		Assert.assertEquals(200, result.get(2)[0]);
		Assert.assertEquals(299, result.get(2)[1]);
		Assert.assertEquals(300, result.get(3)[0]);
		Assert.assertEquals(399, result.get(3)[1]);
		Assert.assertEquals(400, result.get(4)[0]);
		Assert.assertEquals(499, result.get(4)[1]);
		Assert.assertEquals(500, result.get(5)[0]);
		Assert.assertEquals(550, result.get(5)[1]);
	}

}
