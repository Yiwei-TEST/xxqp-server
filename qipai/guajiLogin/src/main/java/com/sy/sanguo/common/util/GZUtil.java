package com.sy.sanguo.common.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
/**
 * gz文件类型的压缩与解压缩
 *
 * @author Administrator
 *
 */
public final class GZUtil {
	public static void doCompressFile(String inFileName) {

		try {

			System.out.println("Creating the GZIP output stream.");
			String outFileName = inFileName + ".gz";
			GZIPOutputStream out = null;
			try {
				out = new GZIPOutputStream(new FileOutputStream(outFileName));
			} catch (FileNotFoundException e) {
				System.err.println("Could not create file: " + outFileName);
				System.exit(1);
			}

			System.out.println("Opening the input file.");
			FileInputStream in = null;
			try {
				in = new FileInputStream(inFileName);
			} catch (FileNotFoundException e) {
				System.err.println("File not found. " + inFileName);
				System.exit(1);
			}

			System.out
					.println("Transfering bytes from input file to GZIP Format.");
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();

			System.out.println("Completing the GZIP file");
			out.finish();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	/**
	 * Uncompress the incoming file.
	 * 
	 * @param inFileName
	 *            Name of the file to be uncompressed
	 */
	public static void doUncompressFile(String inFileName) {

		try {

			if (!getExtension(inFileName).equalsIgnoreCase("gz")) {
				System.err.println("File name must have extension of \".gz\"");
				System.exit(1);
			}

			System.out.println("Opening the compressed file.");
			GZIPInputStream in = null;
			try {
				in = new GZIPInputStream(new FileInputStream(inFileName));
			} catch (FileNotFoundException e) {
				System.err.println("File not found. " + inFileName);
				System.exit(1);
			}

			System.out.println("Open the output file.");
			String outFileName = getFileName(inFileName);
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(outFileName);
			} catch (FileNotFoundException e) {
				System.err.println("Could not write to file. " + outFileName);
				System.exit(1);
			}

			System.out
					.println("Transfering bytes from compressed file to the output file.");
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			System.out.println("Finish,Closing the file and stream");
			in.close();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	/**
	 * Used to extract and return the extension of a given file.
	 * 
	 * @param f
	 *            Incoming file to get the extension of
	 * @return <code>String</code> representing the extension of the incoming
	 *         file.
	 */
	public static String getExtension(String f) {
		String ext = "";
		int i = f.lastIndexOf('.');

		if (i > 0 && i < f.length() - 1) {
			ext = f.substring(i + 1);
		}
		return ext;
	}

	/**
	 * Used to extract the filename without its extension.
	 * 
	 * @param f
	 *            Incoming file to get the filename
	 * @return <code>String</code> representing the filename without its
	 *         extension.
	 */
	public static String getFileName(String f) {
		String fname = "";
		int i = f.lastIndexOf('.');

		if (i > 0 && i < f.length() - 1) {
			fname = f.substring(0, i);
		}
		return fname;
	}
	
	public static void main(String[] args) {
		doUncompressFile("D:/mydownloads/GeoLite2-City.mmdb.gz");
	}
}