/**
 * 
 */
package com.frontier.ports.util;

import java.util.LinkedList;
import java.util.List;

import com.frontier.lib.validation.ObjectValidator;

/**
 * @author mlcs05
 *
 */
public final class DataFragmentor {

	/**
	 * Determines all the index locations within the data array, given 
	 * a chunk size. For example::
	 * byte[] data = { 1,2,3,4,5,6 };
	 * determineIndices(data, 3);
	 * This will return { int[] = {0,2}, int[] = {3,5}}
	 * 
	 * @param data The data to determine the locations inside.
	 * @param chunkSize The amount of data within the data array to break out.
	 * @return An array list of indexes - List of int[2]
	 */
	public List<int[]> determineIndices(byte[] data, int chunkSize) {
		ObjectValidator.raiseIfNull(data);
		if (chunkSize <= 0) {
			throw new IllegalArgumentException("chunkSize must be > 0");
		}
		int start = 0;
		List<int[]> result = new LinkedList<>();
		int[] next = null;
		do {
			next = nextChunk(data, start, chunkSize);
			if (next != null)
				result.add(next);
			start += chunkSize;
		} while (next != null);
		return result;
	}

	private static int[] nextChunk(byte[] data, int start, int length) {
		int[] result = new int[2];
		result[0] = start;
		if (start >= data.length) {
			result = null;
		} else {
			if (start + length > data.length) {
				result[1] = data.length - 1;
			} else {
				result[1] = start + length - 1;
			}
		}
		return result;
	}

}
