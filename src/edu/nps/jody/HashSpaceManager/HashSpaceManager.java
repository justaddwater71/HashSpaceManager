package edu.nps.jody.HashSpaceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

public class HashSpaceManager 
{
	//Data Members
	/*File largeFile;
	Reader largeReader;
	BufferedReader largeBufferedReader;*/
	
	File largeToSmallHashMapFile;
	HashMap<Integer, Integer> largeToSmallHashMap;
	Integer mapMax;
	
	//Constructor
	HashSpaceManager()
	{
		largeToSmallHashMap = new HashMap<Integer, Integer>();
	}
	
	HashSpaceManager(String largeToSmallHashMapFileName) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		initlializeLargeToSmallHashMap(largeToSmallHashMapFileName);
	}
	
	//Methods
	public BufferedReader loadLargeSVMFile(File largeFile) throws FileNotFoundException
	{
		Reader largeReader = new FileReader(largeFile);
		BufferedReader largeBufferedReader = new BufferedReader(largeReader);
		
		return largeBufferedReader;
	}
	
	
	@SuppressWarnings("unchecked")
	public void initlializeLargeToSmallHashMap(String filename) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		File largeToSmallHashMapFile = new File(filename);
		
		if ( largeToSmallHashMapFile.isFile())
		{
				InputStream inputStream = new FileInputStream(largeToSmallHashMapFile);
				ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
				
				largeToSmallHashMap = (HashMap<Integer, Integer>)objectInputStream.readObject();
				objectInputStream.close();
				Set<Integer> mapSet = new TreeSet<Integer>(largeToSmallHashMap.keySet());
				mapMax = Collections.max(mapSet);
		}
		else
		{
			largeToSmallHashMap = new HashMap<Integer, Integer>();
			mapMax = 1;
		}
	}
	
	public void writeSmallSVMFile(File largeFile, BufferedReader largeBufferedReader)
	{
		String smallFilename = largeFile.getAbsolutePath();
		
		File smallFile = new File(smallFilename + ".small");
		
		String oldLine;
		String newLine = null;
		
		try 
		{
			smallFile.createNewFile();
			PrintWriter smallPrintWriter = new PrintWriter(smallFile);
			
			while ((oldLine = largeBufferedReader.readLine()) != null)
			{
				newLine = convert(oldLine);
				smallPrintWriter.println(newLine);
			}
			
			smallPrintWriter.flush();
		}
		catch (FileNotFoundException f)
		{
			System.out.println("Count not create output file " + smallFile.getAbsolutePath());
		}
		catch (IOException e) 
		{
			System.out.println("There has been an IO error");
		}
	}
	
	public String convert(String oldLine)
	{
		String newLine;
		String pair;
		String feature;
		String count;
		String smallFeature;
		Integer key;
		int colonIndex;
		SortedMap<Integer, String> sortedMap = new TreeMap<Integer, String>();
		
		StringTokenizer tokenizer = new StringTokenizer(oldLine);
		
		//Get the id of this line -- no safety check here, yet
		newLine = tokenizer.nextToken();
		
		while (tokenizer.hasMoreElements())
		{
			pair = tokenizer.nextToken();
			
			colonIndex = pair.indexOf(':');
			
			if (colonIndex < 0)
			{
				System.out.println("Malformed pair");
				return null;
			}
			
			feature = pair.substring(0, colonIndex);
			count = pair.substring(colonIndex + 1);
			
			smallFeature = checkFeature(feature);
			
			sortedMap.put(Integer.valueOf(smallFeature), count);
			//newLine = newLine + " " + smallFeature + ":" + count;
		}
		
		Iterator<Integer> iterator = sortedMap.keySet().iterator();
		
		while (iterator.hasNext())
		{
			key = iterator.next();
			newLine = newLine + " " + key + ":" + sortedMap.get(key);
		}
		
		return newLine;
	}

	public String checkFeature(String feature)
	{
		Integer intFeature = Integer.valueOf(feature);
		Integer intSmallFeature;
		
		if (largeToSmallHashMap.containsKey(intFeature))
		{
			intSmallFeature = largeToSmallHashMap.get(intFeature);
		}
		else
		{
			intSmallFeature = mapMax;
			mapMax = mapMax + 1;
			largeToSmallHashMap.put(intFeature, intSmallFeature);
		}
		
		return intSmallFeature.toString();
	}
	
	public void processLargeSVMDirectory(String directoryName) throws FileNotFoundException
	{
		File[] fileArray;
		
		File directory = new File(directoryName);
		
		if (directory.isDirectory())
		{
			fileArray = directory.listFiles();
		}
		else
		{
			fileArray = new File[1];
			fileArray[0] = directory;
		}
		
		for (int i=0; i < fileArray.length; i++)
		{
			System.out.println("Processing " + fileArray[i].getAbsolutePath());
			processLargeSVMFile(fileArray[i]);
		}
	}
	
	public void processLargeSVMFile(File largeSVMFile) throws FileNotFoundException
	{
		if (largeSVMFile.isDirectory())
		{
			processLargeSVMDirectory(largeSVMFile.getAbsolutePath());
		}
		BufferedReader largeBufferedReader =new BufferedReader(new FileReader(largeSVMFile));
		
		writeSmallSVMFile(largeSVMFile, largeBufferedReader);
	}
	
	public void writeLargeToSmallHashMap() throws IOException
	{
		OutputStream outputStream = null;
		ObjectOutputStream objectOutputStream = null;
		try 
		{
			outputStream = new FileOutputStream(largeToSmallHashMapFile);
		} 
		catch (FileNotFoundException e) 
		{
				largeToSmallHashMapFile.createNewFile();
				outputStream = new FileOutputStream(largeToSmallHashMapFile);
		}
		
			objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.writeObject(largeToSmallHashMap);
			objectOutputStream.close();
	}
	
	
	/*public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		String largeSVMFile = args[0];
		String hashMapFile = args[1];
		
		HashSpaceManager hsm = new HashSpaceManager(hashMapFile);
				
		hsm.processLargeSVMDirectory(largeSVMFile);

		hsm.writeLargeToSmallHashMap();
	}*/

}
