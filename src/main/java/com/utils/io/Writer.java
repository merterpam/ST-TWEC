package com.utils.io;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Writer {
	BufferedWriter printWriter;
	
	public void openWriter(String outputFile)
	{
		try {
			printWriter = new BufferedWriter
					(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void closeWriter()
	{
		try {
			printWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeLine(String line)
	{
		try {
			printWriter.write(line + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

		
		

}
