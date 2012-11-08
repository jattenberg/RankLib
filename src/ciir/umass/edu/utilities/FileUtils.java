/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This class provides some file processing utilities such as read/write files, obtain files in a
 * directory...
 * @author Van Dang
 * @version 1.3 (July 29, 2008)
 */
public class FileUtils {
	/**
	 * Read the content of a file.
	 * @param filename The file to read.
	 * @param encoding The encoding of the file.
	 * @return The content of the input file.
	 */
	public static String read(String filename, String encoding) 
	{
		BufferedReader in;
		String content = "";
		try{
			in = new BufferedReader(
	            new InputStreamReader(
	            new FileInputStream(filename), encoding));
			char[] newContent = new char[40960];
			int numRead=-1;
			while((numRead=in.read(newContent)) != -1)
			{
				content += new String(newContent, 0, numRead);
			}
			in.close();
		}
		catch(Exception e)
		{
			content = "";
		}
		return content;
	}
	
	public static List<String> readLine(String filename, String encoding) 
	{
		List<String> lines = new ArrayList<String>();
		try {
			String content = "";
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(filename), encoding));
			
			while((content = in.readLine()) != null)
			{
				content = content.trim();
				if(content.length() == 0)
					continue;
				lines.add(content);
			}
			in.close();
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
		return lines;
	}
	/**
	 * Write a text to a file.
	 * @param filename The output filename.
	 * @param encoding The encoding of the file.
	 * @param strToWrite The string to write.
	 * @return TRUE if the procedure succeeds; FALSE otherwise.
	 */
	public static boolean write(String filename, String encoding, String strToWrite) 
	{
		BufferedWriter out = null;
		try{
			
			out = new BufferedWriter(
			          new OutputStreamWriter(new FileOutputStream(filename), encoding));
			out.write(strToWrite);
			out.close();
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}
	/**
	 * Get all file (non-recursively) from a directory.
	 * @param directory The directory to read.
	 * @return A list of filenames (without path) in the input directory.
	 */
	public static String[] getAllFiles(String directory)
	{
		File dir = new File(directory);
		String[] fns = dir.list();
		return fns;
	}
	/**
	 * Get all file (non-recursively) from a directory.
	 * @param directory The directory to read.
	 * @return A list of filenames (without path) in the input directory.
	 */
	public static List<String> getAllFiles2(String directory)
	{
		File dir = new File(directory);
		String[] fns = dir.list();
		List<String> files = new ArrayList<String>();
		if(fns != null)
			for(int i=0;i<fns.length;i++)
				files.add(fns[i]);
		return files;
	}
	/**
	 * Test whether a file/directory exists.
	 * @param file the file/directory to test.
	 * @return TRUE if exists; FALSE otherwise.
	 */
	public static boolean exists(String file)
	{
		File f = new File(file);
		return f.exists();
	}
	/**
	 * Copy a file.
	 * @param srcFile The source file.
	 * @param dstFile The copied file.
	 */
	public static void copyFile(String srcFile, String dstFile)
	{
		try {
		    FileInputStream fis  = new FileInputStream(new File(srcFile));
		    FileOutputStream fos = new FileOutputStream(new File(dstFile));
		    try
		    {
		    	byte[] buf = new byte[40960];
		    	int i = 0;
		    	while ((i = fis.read(buf)) != -1) {
		    		fos.write(buf, 0, i);
		    	}
		    } 
		    catch (Exception e)
		    {
		    	System.out.println("Error in FileUtils.copyFile: " + e.toString());
		    }
		    finally
		    {
		    	if (fis != null) fis.close();
		    	if (fos != null) fos.close();
		    }
		}
		catch(Exception ex)
		{
			System.out.println("Error in FileUtils.copyFile: " + ex.toString());
		}
	}
	/**
	 * Copy all files in the source directory to the target directory.
	 * @param srcDir The source directory.
	 * @param dstDir The target directory.
	 * @param files The files to be copied. NOTE THAT this list contains only names (WITHOUT PATH).
	 */
	public static void copyFiles(String srcDir, String dstDir, List<String> files)
	{
		for(int i=0;i<files.size();i++)
			FileUtils.copyFile(srcDir+files.get(i), dstDir+files.get(i));
	}
	public static final int BUF_SIZE = 51200;
    /**
     * Gunzip an input file.
     * @param file_input	Input file to gunzip.
     * @param dir_output	Output directory to contain the ungzipped file (whose name = file_input - ".gz")
     * @return 1 if succeed, 0 otherwise.
     */
	public static int gunzipFile (File file_input, File dir_output) {
        // Create a buffered gzip input stream to the archive file.
    	GZIPInputStream gzip_in_stream;
        try {
        	FileInputStream in = new FileInputStream(file_input);
        	BufferedInputStream source = new BufferedInputStream (in);
        	gzip_in_stream = new GZIPInputStream(source);
        }
		catch (IOException e) {
			System.out.println("Error in gunzipFile(): " + e.toString());
			return 0;
		}

        // Use the name of the archive for the output file name but
        // with ".gz" stripped off.
		String file_input_name = file_input.getName ();
		String file_output_name = file_input_name.substring (0, file_input_name.length () - 3);

        // Create the decompressed output file.
		File output_file = new File (dir_output, file_output_name);

        // Decompress the gzipped file by reading it via
        // the GZIP input stream. Will need a buffer.
		byte[] input_buffer = new byte[BUF_SIZE];
		int len = 0;
		try {
			// Create a buffered output stream to the file.
			FileOutputStream out = new FileOutputStream(output_file);
			BufferedOutputStream destination = new BufferedOutputStream (out, BUF_SIZE);

         	//Now read from the gzip stream, which will decompress the data,
          	//and write to the output stream.
			while ((len = gzip_in_stream.read (input_buffer, 0, BUF_SIZE)) != -1)
				destination.write (input_buffer, 0, len);
			destination.flush (); // Insure that all data is written to the output.
			out.close ();
		}
        catch (IOException e) {
        	System.out.println("Error in gunzipFile(): " + e.toString());
        	return 0;
        }

        try {
        	gzip_in_stream.close ();
        }
        catch (IOException e) {
        	return 0;
        }
        return 1;
    }
	/**
	 * Gzip an input file.
	 * @param inputFile The input file to gzip.
	 * @param gzipFilename The gunzipped file's name.
	 * @return 1 if succeeds, 0 otherwise
	 */
	public static int gzipFile(String inputFile, String gzipFilename)
    {
		try {
	    	// Specify gzip file name
			GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(gzipFilename));
	    	
	    	// Specify the input file to be compressed
	    	FileInputStream in = new FileInputStream(inputFile);
	    	
	    	// Transfer bytes from the input file 
	    	// to the gzip output stream
	    	byte[] buf = new byte[BUF_SIZE];
	    	int len;
	    	while ((len = in.read(buf)) > 0) {
	    		out.write(buf, 0, len);
	    	}
	    	in.close();
	    	
	    	// Finish creation of gzip file
	    	out.finish();
	    	out.close();
		}
		catch (Exception ex)
		{
			return 0;
		}
		return 1;
    }
}
