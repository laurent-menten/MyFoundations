package be.lmenten.utils.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogFormatter
	extends Formatter
{
	protected static final DateFormat df
		= new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss.SSS" );

	// ========================================================================
	// ===
	// ========================================================================

	@Override
	public String getHead( Handler h )
	{
		return super.getHead( h );
	}

	@Override
	public String format( LogRecord record )
	{
		StringBuilder s = new StringBuilder();

		// --------------------------------------------------------------------

		s.append( record.getThreadID() )
			.append( "." )
			.append( record.getSequenceNumber() )
			.append( ": " );

		s.append( df.format( new Date( record.getMillis() ) ) )
			.append( " [" )
			.append( record.getSourceClassName() )
			.append( "." )
			.append( record.getSourceMethodName() )
			.append( "]");

		// --------------------------------------------------------------------

		s.append(" - [");
		s.append( record.getLevel() );
		s.append( "]" );

		// --------------------------------------------------------------------

		s.append( " - ")
			.append( formatMessage( record ) )
			.append("\n");

		// --------------------------------------------------------------------

		Object [] params = record.getParameters();
		if( params != null )
		{
			s.append( "\n\t" )
				.append( "Parameters : \n" );

			for( int  i = 0 ; i < params.length ; i++ )
			{
				s.append( "\t\t" )
					.append( i )
					.append( ": " )
					.append( params[i].toString() )
					.append( '\n' );
			}

			s.append( '\n' );
		}

		// --------------------------------------------------------------------

		Throwable ex = record.getThrown();
		if( ex != null )
		{
			s.append( '\t' )
				.append( "EXCEPTION : " );
			s.append( ex.getClass().getName() );
			s.append( " : " )
				.append( ex.getMessage() )
				.append( "\n\n" );

			for( StackTraceElement el : ex.getStackTrace() )
			{
				s.append( '\t' )

				 .append( el.getClassName() )
				 .append( "." )
				 .append( el.getMethodName() )

				 .append( " - " );

				if( el.getFileName() != null )
				{
					s.append( "[" )
					.append( el.getFileName() )
					.append( ":" )
					.append( el.getLineNumber() )
					.append( "]" );
				}
				else
				{
					s.append( "[ ? ]" );
				}
				
				s.append( "\n" );
			}

			s.append( "\n" );
		}

		return s.toString();
	}

	@Override
	public String getTail( Handler h )
	{
		return super.getTail( h );
	}

    // ========================================================================
	// === INSTALLERS =========================================================
	// ========================================================================

	public static final String ROOT_LOGGER_NAME = "";

	/**
	 * 
	 * @return
	 */
	public static LogFormatter install()
	{
		return LogFormatter.install( null );
	}

	/**
	 * 
	 * @param level
	 * @return
	 */
	public static LogFormatter install( Level level )
	{
		LogFormatter formatter = new LogFormatter();

		Logger rootLogger = Logger.getLogger( ROOT_LOGGER_NAME );
		if( level != null )
		{
			rootLogger.setLevel( level );
		}

		for( Handler rawHandler : rootLogger.getHandlers() )
		{
			rawHandler.setFormatter( formatter );
			if( level != null )
			{
				rawHandler.setLevel( level );
			}
		}

		return formatter;
	}

	/**
	 * 
	 * @param level
	 */
	public static void setLevel( Level level )
	{
		Logger rootLogger = Logger.getLogger( ROOT_LOGGER_NAME );
		if( level != null )
		{
			rootLogger.setLevel( level );
		}

		for( Handler rawHandler : rootLogger.getHandlers() )
		{
			if( level != null )
			{
				rawHandler.setLevel( level );
			}
		}
	}
}
