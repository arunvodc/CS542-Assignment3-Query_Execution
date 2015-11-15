
public class Constants {

		// Points to the first byte address where the current metadata pointer starts in the file.
		// Size is of 4 bytes. The 4 bytes contains the address to the location where next free space is available
		// in the metadata table.
		public static final int METADATA_CURRENT_POINTER = 0;
		
		// Points to the first byte address where the current data pointer starts in the file.
		// Size is of 4 bytes. The 4 bytes contains the address to the location where next free space is available
		// in the data section.
		public static final int DATA_CURRENT_POINTER = 4;
		
		// Points to the first byte address where the free space pointer starts in the file.
		// Size is of 4 bytes. The 4 bytes contains the value of total free space available in the data section.
		public static final int FREESPACE_POINTER = 8;
		
		//key value related for the database. Not to be confused with value entered by the user.
		public static final int AUTO_INCREMENT_KEY_POINTER = 12;

		//The byte location from where the attributes are stored.
		public static final int ATTRIBUTE_START_POINTER = 20;
		
		//The pointer gives the byte address where the total length of the attributes is stored.
		public static final int ATTRIBUTE_LENGTH_POINTER = 16;
		
		// The number of bytes allocated for each of the pointers.
		public static final int SIZE_OF_POINTER = 4;
		
		// Points to the address where the metadata table starts in the file.
		public static final int METADATA_TABLE_POINTER = 1024;
		
		// Points to the address where the metadata table ends in the file.
		public static final int METADATA_TABLE_END = 1048575;
		
		//Points to the address where the data section starts in a file.
		public static final int DATA_TABLE_POINTER = 1048576;
		
		// Byte length of key column of the metadata table.
		// Stores the key value.
		public static final int SIZE_OF_KEY_FIELD = 4;
		
		// Byte length of position column of the metadata table.
		// Stores the location of the data in the data section corresponding to the key.
		public static final int SIZE_OF_POSITION_FIELD = 4;
		
		// Byte length of length column of the metadata table.
		// Stores the total length of data in the data section corresponding to the key.
		public static final int SIZE_OF_LENGTH_FIELD = 4;
		
		// Total number of bytes allocated to the entire file (pointers + metadata + data)
		public static final int FILE_LENGTH = 5242880;
		
		// Default value for key when it is deleted
		public static final int DELETED_KEY_VALUE = 0;
		
		public static final String NOT_FOUND = "NotFound";
		
		//Default value for the database internal key when the table is created for the first time. 
		public static final int AUTO_INCREMENT_KEY_INITIAL_VALUE=1;
		
		//Factor by which database internal key is incremented after each insert statement.
		public static final int AUTO_INCREMENT_KEY_FACTOR=1;
}
