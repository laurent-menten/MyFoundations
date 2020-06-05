package be.lmenten.utils;

public class NumberUtils
{
	/**
	 * 
	 * @param b
	 * @return
	 */
	public static int propagateMSB( int b )
	{
		int value = b & 0xFF;
		if( ((value & 0b1000_0000) == 0b1000_0000) )
		{
			value |=  ~0xFF;
		}

		return value;
	}

	/**
	 * Get the state of a single bit.
	 * 
	 * @param bit
	 * @return
	 */
	public static boolean bit( int data, int bit )
	{
		if( (bit < 0) || (bit >= 32) )
		{
			throw new IllegalArgumentException( "Bit number " + bit + " out of range" );
		}

		return ((data & (1<<bit)) == (1<<bit));
	}

	public static boolean bit0( int data ) { return bit( data, 0 ); }
	public static boolean bit1( int data ) { return bit( data, 1 ); }
	public static boolean bit2( int data ) { return bit( data, 2 ); }
	public static boolean bit3( int data ) { return bit( data, 3 ); }
	public static boolean bit4( int data ) { return bit( data, 4 ); }
	public static boolean bit5( int data ) { return bit( data, 5 ); }
	public static boolean bit6( int data ) { return bit( data, 6 ); }
	public static boolean bit7( int data ) { return bit( data, 7 ); }

	/**
	 * Set the state of a single bit.
	 * 
	 * @param bit
	 * @param state
	 */
	public static void bit( int data, int bit, final boolean state )
	{
		if( (bit < 0) || (bit >= 32) )
		{
			throw new IllegalArgumentException( "Bit number " + bit + " out of range" );
		}

		if( state )
		{
			data |= (1<<bit);
		}
		else
		{
			data &= ~(1<<bit);
		}
	}

	public static void bit0( int data, boolean state ) { bit( data, 0, state ); }
	public static void bit1( int data, boolean state ) { bit( data, 1, state ); }
	public static void bit2( int data, boolean state ) { bit( data, 2, state ); }
	public static void bit3( int data, boolean state ) { bit( data, 3, state ); }
	public static void bit4( int data, boolean state ) { bit( data, 4, state ); }
	public static void bit5( int data, boolean state ) { bit( data, 5, state ); }
	public static void bit6( int data, boolean state ) { bit( data, 6, state ); }
	public static void bit7( int data, boolean state ) { bit( data, 7, state ); }

}
