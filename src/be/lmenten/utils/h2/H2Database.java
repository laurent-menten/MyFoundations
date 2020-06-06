package be.lmenten.utils.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

import org.h2.api.ErrorCode;

public abstract class H2Database
	implements AutoCloseable
{
	private final String dbName;
	private final String dbUrl;

	protected Connection con;
	
	// =========================================================================
	// === CONSTRUCTOR(s) ======================================================
	// =========================================================================

	public H2Database( String dbName )
	{
		if( dbName.indexOf( ';' ) != -1 )
		{
			throw new IllegalArgumentException( "Injection attemp! " + dbName );
		}

		this.dbName = dbName;
		this.dbUrl = "jdbc:h2:" + dbName;
	}

	// =========================================================================
	// =
	// =========================================================================

	public String getDbName()
	{
		return dbName;
	}

	// =========================================================================
	// =
	// =========================================================================

	public void open()
			throws SQLException
	{
		Properties config = new Properties();

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
		config.put( "IFEXISTS", "TRUE" );

		log.info( "Opening database " + dbName + " (" + dbUrl + ")" );

		try
		{
			con = DriverManager.getConnection( dbUrl, config );

			internalUpdate();
		}
		catch( SQLException e )
		{
			if( e.getErrorCode() != ErrorCode.DATABASE_NOT_FOUND_WITH_IF_EXISTS_1 )
			{
				throw e;
			}

			log.info( "Creating database version " + getDatabaseVersion() );

			config.remove( "IFEXISTS" );
			con = DriverManager.getConnection( dbUrl, config );

			internalCreate();
		}
	}

	// =========================================================================
	// === 
	// =========================================================================

	private static final String TABLE_INTERNAL_INFOS = "internal_infos";

	private static final String COL_NAME = "name";
	private static final String COL_VALUE = "value";

	private static final String SQL_INTERNAL_INFOS
		=	"CREATE TABLE " + TABLE_INTERNAL_INFOS + " "
		+	"( "
		+		COL_NAME + " VARCHAR_IGNORECASE PRIMARY KEY, "
		+		COL_VALUE + " VARCHAR "
		+	");"
		;

	private static final String KEY_DATABASE_VERSION = "database.version";

	private static final String SQL_INSERT_DATABASE_VERSION
		=	"INSERT INTO " + TABLE_INTERNAL_INFOS + " "
		+	"VALUES( ?, ? )"
		+	";";

	private static final String SQL_UPDATE_DATABASE_VERSION
		=	"UPDATE " + TABLE_INTERNAL_INFOS + " "
		+	"SET " + COL_VALUE + " = ? "
		+	"WHERE " + COL_NAME + " = ? "
		+	";"
		;

	private static final String SQL_SELECT_DATABASE_VERSION
		=	"SELECT " + COL_VALUE + " "
		+	"FROM " + TABLE_INTERNAL_INFOS + " "
		+	"WHERE " + COL_NAME + " = ? "
		+	";"
		;

	// -------------------------------------------------------------------------

	public abstract int getDatabaseVersion();

	protected abstract void create()
		throws SQLException;

	protected abstract void upgrade( int oldVersion )
		throws SQLException;

	// -------------------------------------------------------------------------

	private void internalCreate()
		throws SQLException
	{
		Statement stmtCreate = con.createStatement();
		stmtCreate.executeUpdate( SQL_INTERNAL_INFOS );
		stmtCreate.close();

		create();

		PreparedStatement stmtSetVersion = con.prepareStatement( SQL_INSERT_DATABASE_VERSION );
		stmtSetVersion.setString( 1 , KEY_DATABASE_VERSION );
		stmtSetVersion.setInt( 2, getDatabaseVersion() );
		stmtSetVersion.execute();
		stmtSetVersion.close();		
	}

	private void internalUpdate()
		throws SQLException
	{
		PreparedStatement stmtSetVersion = con.prepareStatement( SQL_SELECT_DATABASE_VERSION );
		stmtSetVersion.setString( 1 , KEY_DATABASE_VERSION );

		ResultSet rs = stmtSetVersion.executeQuery();
		if( ! rs.next()  )
		{
			throw new SQLException( "Invalid database structure" );
		}

		int currentVersion = getDatabaseVersion();
		int oldVersion = rs.getInt( 1 );

		if( oldVersion != currentVersion )
		{
			if( oldVersion < currentVersion )
			{
				log.info( "Upgrading from version " + oldVersion + " to version " + currentVersion );
		
				upgrade( oldVersion );
			}
			else if( oldVersion > currentVersion )
			{
				log.warning( "Downgrading from version " + oldVersion + " to version " + currentVersion );
		
				upgrade( oldVersion );
			}
			else
			{
			}

			PreparedStatement stmtUpdateVersion = con.prepareStatement( SQL_UPDATE_DATABASE_VERSION );
			stmtUpdateVersion.setInt( 1, currentVersion );
			stmtUpdateVersion.setString( 2 , KEY_DATABASE_VERSION );
			stmtUpdateVersion.execute();
			stmtUpdateVersion.close();	
		}
		else
		{
			log.info( "Database version : " + oldVersion );
		}
	}

	// =========================================================================
	// === interface: AutoCloseable ============================================
	// =========================================================================

	@Override
	public void close()
		throws Exception
	{
	}

	// =========================================================================
	// === LOGGING =============================================================
	// =========================================================================

	private static final Logger log
		= Logger.getLogger( H2Database.class.getName() );
}
