/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/

package xxl.core.io.fat.util;

import java.util.StringTokenizer;

/**
 * This class supports operations on strings. Like extract the name of a file from
 * a complete path string.
 */
public class StringOperations
{
	
	/**
	 * Return the name of the file of completePath that is the suffix of completePath.
	 * If no file separator and no ":" exists the completePath string will be returned.
	 * The complete path string is allowed to contain a device name but it don't
	 * has to. In case there is a device name with a file separator but no file or directory
	 * name the empty string is returned. In case there is a ":" in completePath everything
	 * until this ":" is treated as device name.
	 * @param completePath a path to a file with or without device name.
	 * @return the last component of completePath seperated by "\" or the empty string if no 
	 * such component exist.
	 */
	public static String extractFileName(String completePath)
	{
		String deviceName = extractDeviceName(completePath);
		String path = completePath.substring(deviceName.length());
		StringTokenizer stringTokenizer = new StringTokenizer(path, System.getProperty("file.separator"));
		String str = "";
		while(stringTokenizer.hasMoreTokens())
			str = stringTokenizer.nextToken();
			
		return str;
	}	//end extractFileName(String completePath)
	
	
	/**
	 * Return the name of the device of completePath that is the prefix of completePath
	 * until and inclusive the first occurence of ":" in case of a win32 system. If the 
	 * system is unix the returned device looks like "/dev/X", where X is a variable for the
	 * unix name of the device like "fd0" for the first floppy, or "hda0" for the first
	 * disk. If no device name exists the empty string  is returned. In case the device name
	 * contained in the complete path doesn't point to a physical device the ":" is used to cut
	 * the device name from the rest of the path, this is system independant.
	 * @param completePath a path to a file with or without device name.
	 * @return the system dependant device name or the empty string
	 * if no such component exist.
	 */
	public static String extractDeviceName(String completePath)
	{
		int index = completePath.indexOf(58);	//search a ':'
		if (index == -1)	//either completePath doesn't have a device name as prefix or it's a unix device name
		{
			if (completePath.startsWith("/dev/"))
			{
				//found unix device name
				index = completePath.indexOf("/", 5);	//find the "/" after the name of the device ("/dev/hda1/")
				if (index == -1)	//the unix device name has no ending with "/"
				{
					if (completePath.length() > "/dev/".length())
						return completePath.substring(0);	//return unix device name 
					else
						return "";	//no it's also no unix device name
				}
				else
					return completePath.substring(0, index);	//return unix device name without the last "/"
			}
			else
				return "";	//no it's also no unix device name
		}
		return completePath.substring(0, index + 1);	//return the win32 device name inclusive the ":"
	}	//end extractDeviceName(String completePath)

	
	/**
	 * Return the path of completePath. The String completePath is allowed to contain
	 * a device name like 'a:', or 'c:' but it don't has to. In case the completePath
	 * String is only a file name the system dependant file separator ("\\" for win32,
	 * or "/" for unix) is returned, which indicates the root directory.
	 * In case completePath doesn't contain a path the string "\\" on win32 systems or
	 * "/" on unix system, is returned.
	 * @param completePath a path to a file with or without device name.
	 * @return the path of completePath only, that is the string after the ":" exclusive the first "\"
	 * until and exclusive the last component of completePath or the empty string if no 
	 * such component exist.
	 */
	public static String extractPath(String completePath)
	{
		String deviceName = extractDeviceName(completePath);
		String path = completePath.substring(deviceName.length());
		//start tokenizer after the device name
		
		StringTokenizer stringTokenizer = new StringTokenizer(path, System.getProperty("file.separator"));
		//build path from the components of string tokenizer except the last token, which is the name of the file
		String result = "";
		while(stringTokenizer.hasMoreTokens())
		{
			String tmp = stringTokenizer.nextToken();
			if (stringTokenizer.hasMoreTokens())
				result += System.getProperty("file.separator")+tmp;
		}
		
		//do some corrections to return the path in the valid syntax.
		if (result.startsWith(System.getProperty("file.separator")) && result.length() > 1)
			result = result.substring(1);
			
		if (result.length() == 0)
			return System.getProperty("file.separator");
		return result;
	}	//end extractPath(String completePath)
	
	
	/**
	 * Return the complete path without the device name, the backslash at the beginning is
	 * also removed. If completePath consists only of a device name the empty string is returned.
	 * @param completePath a path to a file with or without device name.
	 * @return the completePath removed of the deviceName or the completePath itself if no
	 * deviceName exist.
	 */
	public static String removeDeviceName(String completePath)
	{
		String deviceName = extractDeviceName(completePath);
		String path = completePath.substring(deviceName.length());
		if (path.startsWith(System.getProperty("file.separator")))
			return path.substring(1);
		return path;
	}	//end removeDeviceName(String completePath)		


	/**
	 * Extract the numeric tail of the given file name. The numeric
	 * tail is the number after the '~'. If no such number exist
	 * zero is returned.
	 * @param fileName name of a file or directory
	 * @return the numeric tail of the given fileName or zero if no such number exist.
	 */
	public static long extractNumericTail(String fileName)
	{
		int tildeIndex = fileName.indexOf("~");
		if (tildeIndex >= 0)
		{
			int dotIndex = fileName.lastIndexOf(".");
			String number = fileName.substring(tildeIndex + 1, dotIndex);
			return new Long(number).longValue();
		}

		return 0;
	}	//end extractNumericTail(String fileName)


	/**
	 * Used to test this class
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args)
	{

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
			System.out.println("deviceName:>"+extractDeviceName(cp1)+"<");
			System.out.println("path:>"+extractPath(cp1)+"<");
			System.out.println("fileName:>"+extractFileName(cp1)+"<");
			System.out.println("remove device name:>"+removeDeviceName(cp1)+"<");
			System.out.println();
			
			System.out.println(cp2);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(cp2)+"<");
			System.out.println("path:>"+extractPath(cp2)+"<");
			System.out.println("fileName:>"+extractFileName(cp2)+"<");
			System.out.println("remove device name:>"+removeDeviceName(cp2)+"<");
			System.out.println();
			
			System.out.println(cp3);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(cp3)+"<");
			System.out.println("path:>"+extractPath(cp3)+"<");
			System.out.println("fileName:>"+extractFileName(cp3)+"<");
			System.out.println("remove device name:>"+removeDeviceName(cp3)+"<");
			System.out.println();
			
			System.out.println(cp4);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(cp4)+"<");
			System.out.println("path:>"+extractPath(cp4)+"<");
			System.out.println("fileName:>"+extractFileName(cp4)+"<");
			System.out.println("remove device name:>"+removeDeviceName(cp4)+"<");
			System.out.println();
			
			System.out.println(ncp1);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(ncp1)+"<");
			System.out.println("path:>"+extractPath(ncp1)+"<");
			System.out.println("fileName:>"+extractFileName(ncp1)+"<");
			System.out.println("remove device name:>"+removeDeviceName(ncp1)+"<");
			System.out.println();
			
			System.out.println(ncp2);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(ncp2)+"<");
			System.out.println("path:>"+extractPath(ncp2)+"<");
			System.out.println("fileName:>"+extractFileName(ncp2)+"<");
			System.out.println("remove device name:>"+removeDeviceName(ncp2)+"<");
			System.out.println();
			
			System.out.println(ncp3);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(ncp3)+"<");
			System.out.println("path:>"+extractPath(ncp3)+"<");
			System.out.println("fileName:>"+extractFileName(ncp3)+"<");
			System.out.println("remove device name:>"+removeDeviceName(ncp3)+"<");
			System.out.println();
			
			System.out.println(ncp4);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(ncp4)+"<");
			System.out.println("path:>"+extractPath(ncp4)+"<");
			System.out.println("fileName:>"+extractFileName(ncp4)+"<");
			System.out.println("remove device name:>"+removeDeviceName(ncp4)+"<");
			System.out.println();
			
			System.out.println(ncp5);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(ncp5)+"<");
			System.out.println("path:>"+extractPath(ncp5)+"<");
			System.out.println("fileName:>"+extractFileName(ncp5)+"<");
			System.out.println("remove device name:>"+removeDeviceName(ncp5)+"<");
			System.out.println();
					
			System.out.println(dn1);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(dn1)+"<");
			System.out.println("path:>"+extractPath(dn1)+"<");
			System.out.println("fileName:>"+extractFileName(dn1)+"<");
			System.out.println("remove device name:>"+removeDeviceName(dn1)+"<");
			System.out.println();
			
			System.out.println(dn2);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(dn2)+"<");
			System.out.println("path:>"+extractPath(dn2)+"<");
			System.out.println("fileName:>"+extractFileName(dn2)+"<");
			System.out.println("remove device name:>"+removeDeviceName(dn2)+"<");
			System.out.println();
			
			System.out.println(dn3);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(dn3)+"<");
			System.out.println("path:>"+extractPath(dn3)+"<");
			System.out.println("fileName:>"+extractFileName(dn3)+"<");
			System.out.println("remove device name:>"+removeDeviceName(dn3)+"<");
			System.out.println();
			
			System.out.println(dn4);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(dn4)+"<");
			System.out.println("path:>"+extractPath(dn4)+"<");
			System.out.println("fileName:>"+extractFileName(dn4)+"<");
			System.out.println("remove device name:>"+removeDeviceName(dn4)+"<");
			System.out.println();
			
			System.out.println(ex1);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(ex1)+"<");
			System.out.println("path:>"+extractPath(ex1)+"<");
			System.out.println("fileName:>"+extractFileName(ex1)+"<");
			System.out.println("remove device name:>"+removeDeviceName(ex1)+"<");
			System.out.println();
			
			System.out.println(ex2);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(ex2)+"<");
			System.out.println("path:>"+extractPath(ex2)+"<");
			System.out.println("fileName:>"+extractFileName(ex2)+"<");
			System.out.println("remove device name:>"+removeDeviceName(ex2)+"<");
			System.out.println();
			
			System.out.println(ex3);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(ex3)+"<");
			System.out.println("path:>"+extractPath(ex3)+"<");
			System.out.println("fileName:>"+extractFileName(ex3)+"<");
			System.out.println("remove device name:>"+removeDeviceName(ex3)+"<");
			System.out.println();
		}	//end windows test
		else
		{	//start unix test
			System.out.println(unix1);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(unix1)+"<");
			System.out.println("path:>"+extractPath(unix1)+"<");
			System.out.println("fileName:>"+extractFileName(unix1)+"<");
			System.out.println("remove device name:>"+removeDeviceName(unix1)+"<");
			System.out.println();
			
			System.out.println(unix2);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(unix2)+"<");
			System.out.println("path:>"+extractPath(unix2)+"<");
			System.out.println("fileName:>"+extractFileName(unix2)+"<");
			System.out.println("remove device name:>"+removeDeviceName(unix2)+"<");
			System.out.println();
			
			System.out.println(unix3);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(unix3)+"<");
			System.out.println("path:>"+extractPath(unix3)+"<");
			System.out.println("fileName:>"+extractFileName(unix3)+"<");
			System.out.println("remove device name:>"+removeDeviceName(unix3)+"<");
			System.out.println();
			
			System.out.println(unix4);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(unix4)+"<");
			System.out.println("path:>"+extractPath(unix4)+"<");
			System.out.println("fileName:>"+extractFileName(unix4)+"<");
			System.out.println("remove device name:>"+removeDeviceName(unix4)+"<");
			System.out.println();
			
			System.out.println(unix5);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(unix5)+"<");
			System.out.println("path:>"+extractPath(unix5)+"<");
			System.out.println("fileName:>"+extractFileName(unix5)+"<");
			System.out.println("remove device name:>"+removeDeviceName(unix5)+"<");
			System.out.println();
			
			System.out.println(unix6);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(unix6)+"<");
			System.out.println("path:>"+extractPath(unix6)+"<");
			System.out.println("fileName:>"+extractFileName(unix6)+"<");
			System.out.println("remove device name:>"+removeDeviceName(unix6)+"<");
			System.out.println();
			
			System.out.println(unix7);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(unix7)+"<");
			System.out.println("path:>"+extractPath(unix7)+"<");
			System.out.println("fileName:>"+extractFileName(unix7)+"<");
			System.out.println("remove device name:>"+removeDeviceName(unix7)+"<");
			System.out.println();
			
			System.out.println(unix8);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(unix8)+"<");
			System.out.println("path:>"+extractPath(unix8)+"<");
			System.out.println("fileName:>"+extractFileName(unix8)+"<");
			System.out.println("remove device name:>"+removeDeviceName(unix8)+"<");
			System.out.println();
			
			System.out.println(unix9);
			System.out.println("==============");
			System.out.println("deviceName:>"+extractDeviceName(unix9)+"<");
			System.out.println("path:>"+extractPath(unix9)+"<");
			System.out.println("fileName:>"+extractFileName(unix9)+"<");
			System.out.println("remove device name:>"+removeDeviceName(unix9)+"<");
			System.out.println();
		}
		
		
	}
	
}	//end class StringOperations
