package xxl.tests.io.raw;

import xxl.core.io.raw.RAMRawAccess;
import xxl.core.io.raw.RawAccessArrayFilesystemOperations;
import xxl.core.io.raw.RawAccessUtils;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RawAccessArrayFilesystemOperations.
 */
public class TestRawAccessArrayFilesystemOperations {
	
	/**
	 * Tests the file system.
	 * 
     * @param args the arguments
	 */
	public static void main(String args[]) {
		RawAccessArrayFilesystemOperations fs = 
			new RawAccessArrayFilesystemOperations(
				// 4+1 Partitions, Last partition has 17-3-2-3-4 = 5 sectors
				RawAccessUtils.rawAccessPartitioner(new RAMRawAccess(17),new int[]{3,2,3,4}), 
				// here, we do not work with RandomAccessFiles, so it works.
				null,
				true
			);
		
		for (int runs=1; runs<=3; runs++) {
			System.out.println("Run number: "+runs);
			for (int i=1; i<=5; i++) 
				if (!fs.createFile(String.valueOf(i)))
					throw new RuntimeException("Files could not be created");
			if (fs.createFile("6"))
				throw new RuntimeException("Files creation should not have been possible");
			for (int i=1; i<=5; i++) 
				if (!fs.fileExists(String.valueOf(i)))
					throw new RuntimeException("Files does not exist");
			for (int i=5; i>=1; i--) 
				if (!fs.deleteFile(String.valueOf(i)))
					throw new RuntimeException("Files could not become deleted");
		}
		System.out.println("Test completed successfully");
	}

}
