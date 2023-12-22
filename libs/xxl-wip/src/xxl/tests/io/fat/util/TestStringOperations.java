package xxl.tests.io.fat.util;

import xxl.core.io.fat.util.StringOperations;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class StringOperations.
 */
public class TestStringOperations {


	/**
	 * Used to test this class
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {

		///////////////////////////////////
		// Test path deviceName and path //
		///////////////////////////////////

		String cp1 = "e:\\datei";
		String cp2 = "f:\\datei.txt";
		String cp3 = "g:\\dir0\\subdir0";
		String cp4 = "g:\\dir0\\subdir0\\";
		String ncp1 = "datei";
		String ncp2 = "datei.txt";
		String ncp3 = "\\dir0\\datei.txt";
		String ncp4 = "dir0\\subdir0\\datei.txt";
		String ncp5 = "\\dir0\\";
		String dn1 = "";
		String dn2 = "volumeName";
		String dn3 = "volumeName:";
		String dn4 = "volumeName:\\";
		
		String ex1 = "\\\\.\\a:";
		String ex2 = "\\\\.\\a:\\datei.txt";
		String ex3 = "\\\\.\\a:\\dir0\\datei.txt";
		
		String unix1 = "/dev/fd0";
		String unix2 = "/dev/fd0/dir0";
		String unix3 = "/dev/hda0/dir0/datei";
		String unix4 = "/dev/scsi4/dir0/dir1/datei.txt";
		String unix5 = "";
		String unix6 = "/dev/";
		String unix7 = "/dir0/";
		String unix8 = "datei.txt";
		String unix9 = "a:/dir0/dir1/datei.txt";
		
		
		if (System.getProperty("os.name").startsWith("Win"))
		{
			
			System.out.println(cp1);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(cp1)+"<");
			System.out.println("path:>"+StringOperations.extractPath(cp1)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(cp1)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(cp1)+"<");
			System.out.println();
			
			System.out.println(cp2);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(cp2)+"<");
			System.out.println("path:>"+StringOperations.extractPath(cp2)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(cp2)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(cp2)+"<");
			System.out.println();
			
			System.out.println(cp3);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(cp3)+"<");
			System.out.println("path:>"+StringOperations.extractPath(cp3)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(cp3)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(cp3)+"<");
			System.out.println();
			
			System.out.println(cp4);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(cp4)+"<");
			System.out.println("path:>"+StringOperations.extractPath(cp4)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(cp4)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(cp4)+"<");
			System.out.println();
			
			System.out.println(ncp1);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(ncp1)+"<");
			System.out.println("path:>"+StringOperations.extractPath(ncp1)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(ncp1)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(ncp1)+"<");
			System.out.println();
			
			System.out.println(ncp2);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(ncp2)+"<");
			System.out.println("path:>"+StringOperations.extractPath(ncp2)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(ncp2)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(ncp2)+"<");
			System.out.println();
			
			System.out.println(ncp3);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(ncp3)+"<");
			System.out.println("path:>"+StringOperations.extractPath(ncp3)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(ncp3)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(ncp3)+"<");
			System.out.println();
			
			System.out.println(ncp4);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(ncp4)+"<");
			System.out.println("path:>"+StringOperations.extractPath(ncp4)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(ncp4)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(ncp4)+"<");
			System.out.println();
			
			System.out.println(ncp5);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(ncp5)+"<");
			System.out.println("path:>"+StringOperations.extractPath(ncp5)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(ncp5)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(ncp5)+"<");
			System.out.println();
					
			System.out.println(dn1);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(dn1)+"<");
			System.out.println("path:>"+StringOperations.extractPath(dn1)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(dn1)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(dn1)+"<");
			System.out.println();
			
			System.out.println(dn2);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(dn2)+"<");
			System.out.println("path:>"+StringOperations.extractPath(dn2)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(dn2)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(dn2)+"<");
			System.out.println();
			
			System.out.println(dn3);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(dn3)+"<");
			System.out.println("path:>"+StringOperations.extractPath(dn3)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(dn3)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(dn3)+"<");
			System.out.println();
			
			System.out.println(dn4);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(dn4)+"<");
			System.out.println("path:>"+StringOperations.extractPath(dn4)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(dn4)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(dn4)+"<");
			System.out.println();
			
			System.out.println(ex1);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(ex1)+"<");
			System.out.println("path:>"+StringOperations.extractPath(ex1)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(ex1)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(ex1)+"<");
			System.out.println();
			
			System.out.println(ex2);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(ex2)+"<");
			System.out.println("path:>"+StringOperations.extractPath(ex2)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(ex2)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(ex2)+"<");
			System.out.println();
			
			System.out.println(ex3);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(ex3)+"<");
			System.out.println("path:>"+StringOperations.extractPath(ex3)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(ex3)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(ex3)+"<");
			System.out.println();
		}	//end windows test
		else
		{	//start unix test
			System.out.println(unix1);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(unix1)+"<");
			System.out.println("path:>"+StringOperations.extractPath(unix1)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(unix1)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(unix1)+"<");
			System.out.println();
			
			System.out.println(unix2);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(unix2)+"<");
			System.out.println("path:>"+StringOperations.extractPath(unix2)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(unix2)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(unix2)+"<");
			System.out.println();
			
			System.out.println(unix3);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(unix3)+"<");
			System.out.println("path:>"+StringOperations.extractPath(unix3)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(unix3)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(unix3)+"<");
			System.out.println();
			
			System.out.println(unix4);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(unix4)+"<");
			System.out.println("path:>"+StringOperations.extractPath(unix4)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(unix4)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(unix4)+"<");
			System.out.println();
			
			System.out.println(unix5);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(unix5)+"<");
			System.out.println("path:>"+StringOperations.extractPath(unix5)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(unix5)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(unix5)+"<");
			System.out.println();
			
			System.out.println(unix6);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(unix6)+"<");
			System.out.println("path:>"+StringOperations.extractPath(unix6)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(unix6)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(unix6)+"<");
			System.out.println();
			
			System.out.println(unix7);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(unix7)+"<");
			System.out.println("path:>"+StringOperations.extractPath(unix7)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(unix7)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(unix7)+"<");
			System.out.println();
			
			System.out.println(unix8);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(unix8)+"<");
			System.out.println("path:>"+StringOperations.extractPath(unix8)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(unix8)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(unix8)+"<");
			System.out.println();
			
			System.out.println(unix9);
			System.out.println("==============");
			System.out.println("deviceName:>"+StringOperations.extractDeviceName(unix9)+"<");
			System.out.println("path:>"+StringOperations.extractPath(unix9)+"<");
			System.out.println("fileName:>"+StringOperations.extractFileName(unix9)+"<");
			System.out.println("remove device name:>"+StringOperations.removeDeviceName(unix9)+"<");
			System.out.println();
		}
		
		
	}


}
