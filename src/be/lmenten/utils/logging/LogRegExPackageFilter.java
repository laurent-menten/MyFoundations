package be.lmenten.utils.logging;

import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogRegExPackageFilter
	implements Filter
{
	public static final String DEFAULT_MATCH_PATTERN = ".*";
	public static final Level DEFAULT_LOG_LEVEL = Level.ALL;

	// ------------------------------------------------------------------------

	private Pattern pattern;

	// ========================================================================
	// ===
	// ========================================================================

	public LogRegExPackageFilter()
	{
		this( DEFAULT_MATCH_PATTERN );
	}

	public LogRegExPackageFilter( String pattern )
	{
		setPattern( pattern );
	}

	// ========================================================================
	// ===
	// ========================================================================

	public String getPattern()
	{
		return pattern.toString();
	}

	public void setPattern( String pattern )
	{
		this.pattern = Pattern.compile( pattern );
	}

	// ========================================================================
	// ===
	// ========================================================================

	@Override
	public boolean isLoggable( LogRecord record )
	{
		Matcher matcher = pattern.matcher( record.getSourceClassName() );
		if( ! matcher.find() )
		{
			return false;
		}

		return true;
	}
	// ========================================================================
	// === INSTALLER ==========================================================
	// ========================================================================

	private static final String ROOT_LOGGER_NAME = "";

	public static LogRegExPackageFilter install()
	{
		return install( DEFAULT_MATCH_PATTERN, DEFAULT_LOG_LEVEL );
	}

	public static LogRegExPackageFilter install( String regex )
	{
		return install( regex, DEFAULT_LOG_LEVEL );
	}

	public static LogRegExPackageFilter install( Level level )
	{
		return install( DEFAULT_MATCH_PATTERN, level );
	}

	// ------------------------------------------------------------------------

	public static LogRegExPackageFilter install( String regex, Level level )
	{
		LogRegExPackageFilter filter = new LogRegExPackageFilter( regex );

		Logger rootLogger = Logger.getLogger( ROOT_LOGGER_NAME );
		rootLogger.setFilter( filter );

		if( level != null )
		{
			rootLogger.setLevel( level );
		}

		for( Handler rawHandler : rootLogger.getHandlers() )
		{
			rawHandler.setFilter( filter );

			if( level != null )
			{
				rawHandler.setLevel( level );
			}
		}

		return filter;
	}
}
