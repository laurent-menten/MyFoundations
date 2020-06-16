package be.lmenten.utils.h2;

import java.io.IOException;
import java.lang.Runtime.Version;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import be.lmenten.utils.jdbc.Database;

public abstract class H2Database
	extends Database
{
	// =========================================================================
	// === CONSTRUCTOR(s) ======================================================
	// =========================================================================

	public H2Database( String dbName )
	{
		super( "h2:" + dbName );
	}

	// =========================================================================
	// = OPEN ==================================================================
	// =========================================================================

	public void open()
			throws SQLException
	{

		internalOpen( config );
	}

	public void open( String username, String password )
		throws SQLException
	{
		Properties config = new Properties();

		if( username != null )
		{
			config.put( "USER", username );
		}

		if( password != null )
		{
			config.put( "PASSWORD", password );
		}

		internalOpen( config );
	}

	// -------------------------------------------------------------------------

	public void open( String username, String password, String filePassword )
		throws SQLException
	{
		Properties config = new Properties();

		if( username != null )
		{
			config.put( "USER", username );
		}

		if( password != null )
		{
			config.put( "PASSWORD", password );
		}

		if( filePassword != null )
		{
			config.put( "CIPHER", "AES" );
			config.put( "PASSWORD", password + " " + filePassword );
		}

		internalOpen( config );
	}

	// -------------------------------------------------------------------------

	private void internalOpen( Properties config )
		throws SQLException
	{
		log.info( "Opening database " + getDbName() );

		config.put( "IFEXISTS", "TRUE" );
		if( connect( config ) )
		{
			internalUpdate();
		}
		else
		{
			config.remove( "IFEXISTS" );
			if( connect( config ) )
			{
				log.info( "Creating database version " + getVersion() );
				internalCreate();
			}
			else
			{
				// FIXME report open error
				throw new SQLException( "open failure" );
			}
		}
	}

	// =========================================================================
	// === class : Database ====================================================
	// =========================================================================

	@Override
	public abstract Version getVersion();

	// -------------------------------------------------------------------------

	private static final String INIT_SCRIPT
		= "/be/lmenten/utils/h2/sql/create.sql";

	@Override
	protected void create( Database db )
		throws SQLException
	{
		super.create( db );

		try
		{
			parseCreateScript( INIT_SCRIPT  );
		}
		catch( IOException e )
		{
			throw new SQLException( "Failed to read script '" + INIT_SCRIPT + "'",  e );
		}
	}

	@Override
	protected void update( Database db, Version oldVersion )
		throws SQLException
	{
		super.update( db, oldVersion );
	}

	// =========================================================================
	// === class: Database =====================================================
	// =========================================================================

	private static final String TABLE_PROPERTIES = "properties";
	private static final String COL_PROPERTIES_NAME = "name";
	private static final String COL_PROPERTIES_VALUE = "value";

	// -------------------------------------------------------------------------

	private static final String SQL_SET_PROPERTY
		=	"MERGE INTO " + TABLE_PROPERTIES + " "
		+	"VALUES( ?, ? )"
		+	";"
	;

	@Override
	public final void setProperty( String key, String value )
	{
		try( PreparedStatement stmt = con.prepareStatement( SQL_SET_PROPERTY ) )
		{
			stmt.setString( 1 , key );
			stmt.setString( 2, value );
			stmt.execute();
		}
		catch( SQLException e )
		{
		}
	}

	// -------------------------------------------------------------------------
	
	private static final String SQL_GET_PROPERTY
		=	"SELECT " + COL_PROPERTIES_VALUE + " "
		+	"FROM " + TABLE_PROPERTIES + " "
		+	"WHERE " + COL_PROPERTIES_NAME + " = ? "
		+	";"
		;

	@Override
	public String getProperty( String key, String defaultValue )
	{
		try( PreparedStatement stmt = con.prepareStatement( SQL_GET_PROPERTY ) )
		{
			stmt.setString( 1 , key );

			ResultSet rs = stmt.executeQuery();
			if( rs.next()  )
			{
				return rs.getString( 1 );
			}
		}
		catch( SQLException e )
		{
		}

		return defaultValue;
	}

	// =========================================================================
	// === interface: AutoCloseable ============================================
	// =========================================================================

	@Override
	public void close()
		throws Exception
	{
		if( (con != null) && ! con.isClosed() )
		{
			con.close();
		}
	}

	// =========================================================================
	// === LOGGING =============================================================
	// =========================================================================

	private static final Logger log
		= Logger.getLogger( H2Database.class.getName() );
}
