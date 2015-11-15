import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;

public class Relation {
	
	// Stores the file path of the .db file.
	private String filePath = "";
	private RandomAccessFile file;
	private FileChannel channel;
	private FileLock lock;
	private String sDefaultFilePath="./src/";
	private int iCurrentTuple;
	private String[] attributes = null;
	
	// This method takes input the location and the name of the file to be created.
	public void createDB(String filepath, String sAttributes) throws IOException{
		File file = new File(filepath);
		
		if(!file.exists()){
			file.createNewFile();
			filePath = filepath;
			
			// To create a file of size 5 MB.
			RandomAccessFile racfile = new RandomAccessFile(filePath, "rw");
			racfile.setLength(Constants.FILE_LENGTH);
			racfile.close();
			
			boolean lockAcquired = openAndLockFile("r");
			
			if(lockAcquired == false)
				return;
			
			writeToDBFile(Constants.ATTRIBUTE_START_POINTER, sAttributes.getBytes());
			writeToDBFile(Constants.ATTRIBUTE_LENGTH_POINTER, ByteBuffer.allocate(4).putInt(sAttributes.length()).array());
			writeToDBFile(Constants.AUTO_INCREMENT_KEY_POINTER, ByteBuffer.allocate(4).putInt(Constants.AUTO_INCREMENT_KEY_INITIAL_VALUE).array());
			closeAndUnlockFile();
		} else {
			filePath = filepath;
		}
		
	}
	
	public Relation(String sTableName, String sAttributes){
		String sFullFilePath = sDefaultFilePath + sTableName + ".db";
		System.out.println(sFullFilePath);
		iCurrentTuple=0;
		try{
			createDB(sFullFilePath,sAttributes);
		}catch(Exception E){}
	}

	public String[] getAttributesList(){
		return attributes;
	}

	public void Open(){
		attributes = getAttributeNames();
		iCurrentTuple=Constants.METADATA_TABLE_POINTER;
	}
	
	public String GetNext(){
		
		// Read the next tuple using the iCurrentTuple value
		byte [] data;
		int endAddress, tempKey, location, length = 0;
		String fileNotAccessible = "NotFound";
		
		boolean lockAcquired = openAndLockFile("r");
		
		if(lockAcquired == false)
			return fileNotAccessible;
		
		endAddress = getMDCurrentLocation();
		
		if(iCurrentTuple < endAddress){
			tempKey = getKey(iCurrentTuple);
			
			if(tempKey == 0){
				while(tempKey == 0){
					iCurrentTuple += Constants.SIZE_OF_KEY_FIELD +
							Constants.SIZE_OF_LENGTH_FIELD + Constants.SIZE_OF_POSITION_FIELD;
					if(iCurrentTuple > endAddress){
						closeAndUnlockFile();
						return Constants.NOT_FOUND;
					}
					tempKey = getKey(iCurrentTuple);
				}
			}

			int startAddress = iCurrentTuple + Constants.SIZE_OF_KEY_FIELD;
		
			location = getLocation(startAddress);
			
			startAddress += Constants.SIZE_OF_POSITION_FIELD;
			length = getLength(startAddress);
			
			data = getData(location, length);
			closeAndUnlockFile();
			iCurrentTuple = iCurrentTuple + Constants.SIZE_OF_KEY_FIELD +
					Constants.SIZE_OF_LENGTH_FIELD + Constants.SIZE_OF_POSITION_FIELD;
			String decoded = new String(data); 
			return decoded;
		}
		else{
			closeAndUnlockFile();
			return Constants.NOT_FOUND;
		}
		
	}
	
	public void Close(){
		
	}
	
	// Check if enough space to insert (key,position,length) in metadata table
	private boolean checkEnoughSpaceInMD(int startAddress){
		if(startAddress + 3 * Constants.SIZE_OF_POINTER <= Constants.METADATA_TABLE_END)
			return true;
		else
			return false;
	}
		
	// Returns true if there is atleast one deleted record in the metadata table
	private boolean checkForIfAnyDeletedRecords(){
		boolean noDeletedRecord = false;
		int endAddress, tempKey = 0;
		int startAddress = Constants.METADATA_TABLE_POINTER;
		
		endAddress = getMDCurrentLocation();
		System.out.println("(checkForIfAnyDeletedRecords)End address for metadata table ::: " + endAddress);
		
		while(startAddress < endAddress){
			tempKey = getKey(startAddress);
			System.out.println("(checkForIfAnyDeletedRecords)Key is ::: " +tempKey);
			if(tempKey == Constants.DELETED_KEY_VALUE){
				System.out.println("(checkForIfAnyDeletedRecords)inside if");
				noDeletedRecord = true;
				break;
			} else {
				startAddress += Constants.SIZE_OF_KEY_FIELD + Constants.SIZE_OF_POSITION_FIELD + Constants.SIZE_OF_LENGTH_FIELD;
			}
		}
		
		return noDeletedRecord;
	}

	// Returns the location for free space in the metadata table.
	private int getMDCurrentLocation(){
		byte[] metadataPosition;
		int metadataPositionFromDB = 0;
		try {
			metadataPosition = readFromDBFile(Constants.METADATA_CURRENT_POINTER, Constants.SIZE_OF_POINTER);
			metadataPositionFromDB = new BigInteger(metadataPosition).intValue();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("(getMDCurrentLocation)Metadata Position in DB ::: " + metadataPositionFromDB);
		
		return metadataPositionFromDB;
	}
	
	// Updates the location in the current metadata pointer
	private void setMDCurrentLocation(int startAddress){
		writeToDBFile(Constants.METADATA_CURRENT_POINTER, ByteBuffer.allocate(4).putInt(startAddress).array());
	}
	
	// Returns the total free space available in the data section
	private int getTotalFreeSpace(){
		byte[] freespace;
		int freespaceFromDB = 0;
		try {
			freespace = readFromDBFile(Constants.FREESPACE_POINTER, Constants.SIZE_OF_POINTER);
			freespaceFromDB = new BigInteger(freespace).intValue();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("(getTotalFreeSpace)Free space in DB ::: " + freespaceFromDB);
		return freespaceFromDB;
	}
	
	// Updates the free space value in the file
	private void setTotalFreeSpace(int freespace){
		writeToDBFile(Constants.FREESPACE_POINTER, ByteBuffer.allocate(4).putInt(freespace).array());
	}

	// Returns the location for free space in the data section.
	private int getDataCurrentLocation(){
		byte[] dataPosition;
		int dataPositionFromDB = 0;
		try {
			dataPosition = readFromDBFile(Constants.DATA_CURRENT_POINTER, Constants.SIZE_OF_POINTER);
			dataPositionFromDB = new BigInteger(dataPosition).intValue();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("(getDataCurrentLocation)Data Position in DB ::: " + dataPositionFromDB);
		return dataPositionFromDB;
	}
	
	// Updates the location in the current data pointer
	private void setDataCurrentLocation(int newAddress){
		writeToDBFile(Constants.DATA_CURRENT_POINTER, ByteBuffer.allocate(4).putInt(newAddress).array());
	}
	
	// Returns the key value at the location
	private int getKey(int startAddress){
		int key = 0;
		try {
			key = new BigInteger(readFromDBFile(startAddress, Constants.SIZE_OF_KEY_FIELD)).intValue();
		} catch (IOException e) {
			System.out.println("(getKey)Unable to read key from metadata table");
			e.printStackTrace();
		}
		return key;
	}
		
	// Writes the key in the metadata table
	private void setKey(int startAddress, int key){
		writeToDBFile(startAddress, ByteBuffer.allocate(4).putInt(key).array());
	}
	
	// Returns the location for the data in the data section
	private int getLocation(int startaddress){
		int location = 0;
		try {
			location = new BigInteger(readFromDBFile(startaddress, Constants.SIZE_OF_POSITION_FIELD)).intValue();
		} catch (IOException e) {
			System.out.println("(getLocation)Unable to read location from metadata table");
			e.printStackTrace();
		}
		return location;
	}
		
	// Writes data location in the metadata table
	private void setLocation(int startAddress, int dataAddress){
		writeToDBFile(startAddress, ByteBuffer.allocate(4).putInt(dataAddress).array());
	}
	
	// Returns the length of the data in the data section
	private int getLength(int startAddress){
		int length = 0;
		try {
			length = new BigInteger(readFromDBFile(startAddress, Constants.SIZE_OF_LENGTH_FIELD)).intValue();
		} catch (IOException e) {
			System.out.println("(getLength)Unable to read location from metadata table");
			e.printStackTrace();
		}
		return length;
	}
		
	// Writes data length in the metadata table
	private void setLength(int startAddress, int length){
		writeToDBFile(startAddress, ByteBuffer.allocate(4).putInt(length).array());
	}
	
	// Returns the data
	private byte[] getData(int startAddress, int length){
		byte[] data = null;
		try {
			data = readFromDBFile(startAddress, length);
		} catch (IOException e) {
			System.out.println("(getData)Unable to read data from data section");
			e.printStackTrace();
		}
		return data;
	}
		
	// Writes the data in the data section
	private void setData(int startAddress, byte[] data){
		writeToDBFile(startAddress, data);
	}
		
	// Returns the address of the next key in metadata table
	private int getNextKeyAddress(){
		return Constants.SIZE_OF_KEY_FIELD + Constants.SIZE_OF_POSITION_FIELD + Constants.SIZE_OF_LENGTH_FIELD;
	}

	public int insert(String tuple){
		int key = readKeyValue();
		byte [] data = tuple.getBytes();
		
		try{
			System.out.println(key);
			Put(key,data);
			incrementKeyValue(key);
			return 1;
		}catch(IOException E)
		{
			return -1;
		}
		
	}
	// This method stores the key and the value in the specified file.
	// Various checks like enough space, fragmentation checks are done before any data is written to the file.
	public void Put(int key, byte[] data) throws IOException{
		boolean lockAcquired = openAndLockFile("rw");
		// Get the location for free space in the metadata table. 
		if(lockAcquired == false)
			return;

		int metadataPositionFromDB = getMDCurrentLocation();
		
		// Check if enough space to write (key,position,length) in the metadata table
		if(checkEnoughSpaceInMD(metadataPositionFromDB) == false && checkForIfAnyDeletedRecords() == false)
			System.out.println("(Put)Not enough space!!!");
		else {
			// If no entries in metadata table set the freespace default value, metadata current pointer value
			// and data current pointer value
			if(metadataPositionFromDB == 0){
				setTotalFreeSpace(Constants.FILE_LENGTH - Constants.METADATA_TABLE_END);
				setDataCurrentLocation(Constants.DATA_TABLE_POINTER);
				setMDCurrentLocation(Constants.METADATA_TABLE_POINTER);
			}
			
			// Get the total free space available in the data section
			int freespaceFromDB = getTotalFreeSpace();
			
			// Get the location for free space in the metadata table. 
			metadataPositionFromDB = getMDCurrentLocation();
			
			//Get the location for free space in data section
			int dataPositionFromDB = getDataCurrentLocation();
			
			//Check if enough space is available
			if(data.length <= freespaceFromDB){
				
				// Check if enough contiguous space is available or not
				if(data.length <= Constants.FILE_LENGTH - dataPositionFromDB){
					writeDataAndMetadata(metadataPositionFromDB, dataPositionFromDB, freespaceFromDB, key, data);
				} else {
					System.out.println("(Put)Not enough contiguous space");
					checkForDeletedRecord(key, data);
				}
			} else {
				System.out.println("(Put)Not enough space");
			}
		}
		
		closeAndUnlockFile();
	}

	private void incrementKeyValue(int Key){
		boolean lockAcquired = openAndLockFile("rw");
		// Get the location for free space in the metadata table. 
		if(lockAcquired == false)
			return;
		writeToDBFile(Constants.AUTO_INCREMENT_KEY_POINTER, ByteBuffer.allocate(4).putInt(Key+1).array());
		closeAndUnlockFile();
	}
	
	private int readKeyValue(){
		boolean lockAcquired = openAndLockFile("rw");
		// Get the location for free space in the metadata table. 
		if(lockAcquired == false)
			return -1;
		
		int increment = 0;
		try {
			increment = new BigInteger(readFromDBFile(Constants.AUTO_INCREMENT_KEY_POINTER, Constants.SIZE_OF_POINTER)).intValue();
		} catch (IOException e) {
			System.out.println("(getLocation)Unable to read location from metadata table");
			e.printStackTrace();
		}
		closeAndUnlockFile();
		return increment;
	}
	
	// This method writes the metadata table, data and updates the pointers in the file.
	private void writeDataAndMetadata(int metadataPositionFromDB, int dataPositionFromDB, int freespaceFromDB, int key, byte[] data){
		//write data to the data section
		setData(dataPositionFromDB, data);
				
		//write key in metadata
		setKey(metadataPositionFromDB, key);
		metadataPositionFromDB += Constants.SIZE_OF_KEY_FIELD;
		System.out.println("(writeDataAndMetadata)Metadata Position in DB after key insert ::: " + metadataPositionFromDB);
		
		//write data location in metadata
		setLocation(metadataPositionFromDB, dataPositionFromDB);
		metadataPositionFromDB += Constants.SIZE_OF_POSITION_FIELD;
		System.out.println("(writeDataAndMetadata)Metadata Position in DB after position insert ::: " + metadataPositionFromDB);
		
		//write length of data to metadata
		System.out.println("(writeDataAndMetadata)The total data size in bytes" + data.length);
		setLength(metadataPositionFromDB, data.length);
		metadataPositionFromDB += Constants.SIZE_OF_LENGTH_FIELD;
		System.out.println("(writeDataAndMetadata)Metadata Position in DB after length insert ::: " + metadataPositionFromDB);
		
		//update the current data pointer
		setDataCurrentLocation(dataPositionFromDB + data.length);

		//update free space value in the metadata section
		setTotalFreeSpace(freespaceFromDB - data.length);

		//update metadata current pointer
		setMDCurrentLocation(metadataPositionFromDB);
	}

	private String[] getAttributeNames(){
		boolean lockAcquired = openAndLockFile("rw");
		byte[] attributeNames = null;
		int attributeLength = 0;
		// Get the location for free space in the metadata table. 
		if(lockAcquired == false)
			return null;
		
		try {
			attributeLength =  new BigInteger(readFromDBFile(Constants.ATTRIBUTE_LENGTH_POINTER, Constants.SIZE_OF_POINTER)).intValue();
			attributeNames = readFromDBFile(Constants.ATTRIBUTE_START_POINTER, attributeLength);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		closeAndUnlockFile();
		
		String tempString = new String(attributeNames);
		String[] attributes = tempString.split(",");
		return attributes;
		
		
	}
	// If there is no space in the file to write new record, this methods checks for any deleted record in the
	// metadata table. If deleted record is found then it checks if enough space is available to write the data or not.
	// If there is not enough space, it checks for any fragmentation which is caused due to event like:
	// space allocated to the data is greater than the the actual data written in that space.
	private void checkForDeletedRecord(int key, byte[] data){
		int startAddress = Constants.METADATA_TABLE_POINTER;
		int tempAddress;
		int endAddress, tempKey, location, length = 0;
		boolean freeSpaceFound = false;
		
		endAddress = getMDCurrentLocation();
		System.out.println("(checkForDeletedRecord)End address for metadata table ::: " + endAddress);
		
		while(startAddress < endAddress){
			tempAddress = 0;
			tempKey = getKey(startAddress);
			System.out.println("(checkForDeletedRecord)Key is ::: " +tempKey);
			if(tempKey == 0){
				System.out.println("(checkForDeletedRecord)inside if for deleted record ");
				tempAddress += startAddress + Constants.SIZE_OF_KEY_FIELD + Constants.SIZE_OF_POSITION_FIELD;
				length = getLength(tempAddress);
				if(data.length <= length){
					freeSpaceFound = true;
					break;
				} else {
					startAddress += getNextKeyAddress();
				}
			} else {
				startAddress += getNextKeyAddress();
			}
		}
		
		if(freeSpaceFound){
			System.out.println("(checkForDeletedRecord)inside second if for deleted record ");
			
			setKey(startAddress, key);
			startAddress += Constants.SIZE_OF_KEY_FIELD;
			
			location = getLocation(startAddress);
			setData(location, data);
			
			startAddress += Constants.SIZE_OF_POSITION_FIELD;
			setLength(startAddress, data.length);
			
			setTotalFreeSpace(getTotalFreeSpace() - data.length);
		} else {
			handleFragmentation();
			checkSpaceAfterFragmentation(key, data);
		}
	}
	
	// If there are chunks of free space distributed in the data section of the file, this method 
	// reallocates the records and creates a free space at the end of the file.
	private void handleFragmentation(){
		int startAddress = Constants.METADATA_TABLE_POINTER;
		int tempAddress = Constants.METADATA_TABLE_POINTER;
		int endAddress, tempKey = 0;
		int tempCurrentDataLocation = Constants.DATA_TABLE_POINTER;
		int[] array = new int[3];

		endAddress = getMDCurrentLocation();
		System.out.println("(handleFragmentation)End address for metadata table ::: " + endAddress);
		
		while(tempAddress < endAddress){
			tempKey = getKey(tempAddress);
			System.out.println("(handleFragmentation)Key is ::: " +tempKey);
			if(tempKey == 0){
				tempAddress += Constants.SIZE_OF_KEY_FIELD;
				
				tempAddress += Constants.SIZE_OF_POSITION_FIELD + Constants.SIZE_OF_LENGTH_FIELD;
				
				if(tempAddress < endAddress) {
					while(getKey(tempAddress) == 0 ){
						tempAddress += getNextKeyAddress();
					}
					
					array = moveDataInMetadata(tempAddress, startAddress, tempCurrentDataLocation);
					tempAddress = array[0];
					startAddress = array[1];
					tempCurrentDataLocation = array[2];
				}
				
			} else {
				array = moveDataInMetadata(tempAddress, startAddress, tempCurrentDataLocation);
				tempAddress = array[0];
				startAddress = array[1];
				tempCurrentDataLocation = array[2];
			}
		}
		
		setMDCurrentLocation(startAddress);
		setDataCurrentLocation(tempCurrentDataLocation);
		setTotalFreeSpace(Constants.FILE_LENGTH - tempCurrentDataLocation);
	}
	
	// When fragmentation is being checked in the metadata table, all the records are reallocated new space.
	// This method copies the rows of the  metadata table and the data to the new allocated space.
	private int[] moveDataInMetadata(int tempAddress, int startAddress, int tempCurrentDataLocation){
		int key, location, length = 0;
		byte[] data;
		int[] array = new int[3];
		 
		key = getKey(tempAddress);
		tempAddress += Constants.SIZE_OF_KEY_FIELD;
		
		location = getLocation(tempAddress);
		tempAddress += Constants.SIZE_OF_POSITION_FIELD;
		
		length = getLength(tempAddress);
		tempAddress += Constants.SIZE_OF_LENGTH_FIELD;
		
		array[0] = tempAddress;
		
		data = getData(location, length);
		setData(tempCurrentDataLocation, data);
		
		setKey(startAddress, key);
		startAddress += Constants.SIZE_OF_KEY_FIELD;
		
		setLocation(startAddress, tempCurrentDataLocation);
		startAddress += Constants.SIZE_OF_POSITION_FIELD;
		
		setLength(startAddress, length);
		startAddress += Constants.SIZE_OF_LENGTH_FIELD;
		
		array[1] = startAddress;
		
		tempCurrentDataLocation = tempCurrentDataLocation + length;
		
		array[2] = tempCurrentDataLocation;
		
		return array;
	}

	// This methods checks for total free space in the data section of the file after fragmentation is resolved.
	private void checkSpaceAfterFragmentation(int key, byte[] data){
		// Get the location for free space in the metadata table. 
		int metadataPositionFromDB = getMDCurrentLocation();
					
		//Get the location for free space in data section
		int dataPositionFromDB = getDataCurrentLocation();

		// Get the total free space available in the data section
		int freespaceFromDB = getTotalFreeSpace();
		
		if(data.length > Constants.FILE_LENGTH - dataPositionFromDB){
			System.out.println("(checkSpaceAfterFragmentation)No free space available");
		} else {
			writeDataAndMetadata(metadataPositionFromDB, dataPositionFromDB, freespaceFromDB, key, data);
		}
	}
	
	
	// This methods opens the file in read-write mode and tries to acquire a lock on the file.
	private boolean openAndLockFile(String mode){
		boolean lockAcquired = false;
		
		//System.out.println("In openAndLockFile function call");
		try{
			file = new RandomAccessFile(filePath, "rw");
			channel = file.getChannel();
			
			try {
				lock = channel.tryLock();
				if(lock != null){
					lockAcquired =  true;
				}
			} catch (OverlappingFileLockException e) {
				System.out.println("Overlapping File Lock Error ::: " + e.getMessage());
			}
		}catch(IOException e){
			System.out.println(e);
		}
		return lockAcquired;
	}
	
	// This methods closes the file and releases the lock on the file.
	private void closeAndUnlockFile(){
		//System.out.println("In closeAndUnlockFile function call");
		try {
			lock.release();
		} catch (IOException e) {
			System.out.println("Unable to release file lock ::: " + e.getMessage());
		}
		try {
			channel.close();
		} catch (IOException e) {
			System.out.println("Unable to close file channel ::: " + e.getMessage());
		}
		try {
			file.close();
		} catch (IOException e) {
			System.out.println("Unable to close file ::: " + e.getMessage());
		}
	}
	
	// This method writes the data to the file by using the position passed as an argument to the method.
	private void writeToDBFile(int position, byte[] data){
		try {
			file.seek(position);
		} catch (IOException e) {
			System.out.println("(writeToDBFile)Cannot seek position to write in file");
		}
		try {
			file.write(data);
		} catch (IOException e) {
			System.out.println("(writeToDBFile)Cannot write to file");
		}
	}
	
	// This method reads the data from the file by using the position and the number of bytes to read as an argument to the method.
	private byte[] readFromDBFile(int position, int size) throws IOException{
		byte[] bytes = new byte[size];
		file.seek(position);
		file.read(bytes);
		return bytes;
		
	}

	// Returns the data associated with the key.
	// This method sequentially searches a key in the metadata table and when key is matched,
	// it access the location and length of the data to be read from the file.
	/*public byte[] Get(int key){
		byte[] data = new byte[]{};
		int startAddress = Constants.METADATA_TABLE_POINTER;
		int endAddress, tempKey, location, length = 0;
		boolean keyFound = false;
		String fileNotAccessible = "File cannot be accessed";
		
		boolean lockAcquired = openAndLockFile("r");
		
		if(lockAcquired == false)
			return fileNotAccessible.getBytes();
		
		endAddress = getMDCurrentLocation();
		System.out.println("(Get)End address for metadata table ::: " + endAddress);
		
		while(startAddress < endAddress){
			tempKey = getKey(startAddress);
			System.out.println("(Get)Key is ::: " +tempKey);
			if(tempKey == key){
				System.out.println("(Get)inside if ");
				keyFound = true;
				break;
			} else {
				startAddress += getNextKeyAddress();
			}
		}
		
		if(keyFound){
			System.out.println("(Get)inside second if ");
			startAddress += Constants.SIZE_OF_KEY_FIELD;
			location = getLocation(startAddress);
			
			startAddress += Constants.SIZE_OF_POSITION_FIELD;
			length = getLength(startAddress);
			
			data = getData(location, length);
		}
			
		closeAndUnlockFile();
		return data;
	}

	*/
	
	public int getAttributeIndex(String sAttributeName){
		System.out.println(attributes.length);
		for(int i = 0; i < attributes.length; i++){
			if(attributes[i].equals(sAttributeName))
				return i;
		}
		
		return -1;
	}

	// Removes the data associated with the key.
	// This method sequentially searches a key in the metadata table and when key is matched,
	// it sets the key value to zero in the metadata table.
	// During fragmentation the key with value 0 is considered as not valid data and the space alloacted for the
	// data in the data sections is reclaimed by the free space pointer.
	public void Remove(String sAttributeName, String sAttributeValue){
		int startAddress = Constants.METADATA_TABLE_POINTER;
		int endAddress, length=0, totalFreeSpace, tempKey = 0, location;
		boolean keyFound = false;
		int currentKeyAddress=0;
		boolean lockAcquired = openAndLockFile("rw");
		if(lockAcquired == false)
			return;
		
		endAddress = getMDCurrentLocation();
		System.out.println("(Remove)End address for metadata table ::: " + endAddress);

		int attributeIndex = getAttributeIndex(sAttributeName);
		if(attributeIndex == -1){
			System.out.println("Couldnot found attribute :: " + sAttributeName);
			return;
		}
		while(startAddress < endAddress){
			currentKeyAddress = startAddress;
			startAddress += Constants.SIZE_OF_KEY_FIELD;
			location = getLocation(startAddress);
			startAddress += Constants.SIZE_OF_POSITION_FIELD;
			length = getLength(startAddress);
		
			// Read data
			byte [] data = getData(location, length);
			
			String sData = new String(data);
			
			// Convert data to string array
			String [] sDataArray = sData.split(",");
			
			// Compare the attribute
			if(sDataArray[attributeIndex].equals(sAttributeValue)){
				
				// if match is found, then delete.
				keyFound=true;
				break;
			}
		}
		
		if(keyFound){
			setKey(currentKeyAddress, Constants.DELETED_KEY_VALUE);
			totalFreeSpace = getTotalFreeSpace();
			setTotalFreeSpace(totalFreeSpace + length);
		}
		closeAndUnlockFile();
	}
}