package be.lmenten.utils.jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Runtime.Version;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import be.lmenten.utils.jdbc.dumper.TableDumper;

public abstract class Database
	extends DataManager<Data>
	implements AutoCloseable
{
	private HashMap<Class<? extends Data>,DataManager<? extends Data>> managers
		= new HashMap<>();

	// -------------------------------------------------------------------------

	private final String dbName;
	private final String dbUrl;

	// -------------------------------------------------------------------------

	protected final Properties config
		= new Properties();

	protected Connection con;
	
	// =========================================================================
	// =  CONSTRUCTOR(s) =======================================================
	// =========================================================================

	public Database( String dbName )
	{
		if( dbName.indexOf( ';' ) != -1 )
		{
			throw new IllegalArgumentException( "Injection attemp! " + dbName );
		}

		this.dbName = dbName;
		this.dbUrl = "jdbc:" + dbName;

		registerDataManagers();
	}

	// =========================================================================
	// =
	// =========================================================================

	/**
	 * 
	 */
	private void registerDataManagers()
	{
		ServiceLoader<DataManagerDescriptor> sloader
			= ServiceLoader.load( DataManagerDescriptor.class );

		for( DataManagerDescriptor mgr : sloader )
		{
			log.info( "Registering data manager : " + mgr.getName() );

			// -------------------------------------------------------------------

			Class<? extends Data> managedClass
				= mgr.getManagedClass();

			DataManager<? extends Data> managerInstance
				= mgr.getInstance();

			managers.put( managedClass, managerInstance );
		}
	}

	public final DataManager<? extends Data> getDataManager( Class<? extends Data> clazz )
	{
		return managers.get( clazz );
	}

	// =========================================================================
	// = Open Connection =======================================================
	// =========================================================================

	protected boolean connect( Properties config )
	{
		try
		{
			con = DriverManager.getConnection( dbUrl, config );
		}
		catch( SQLException e )
		{
			return false;
		}

		return true;
	}

	// =========================================================================
	// = class: Updatable ======================================================
	// =========================================================================

	protected void internalCreate()
		throws SQLException
	{
		create( this );
		if( createCalled != true )
		{
			// FIXME super.create not called
			log.severe( "super.create() not called !" );
		}

		setCurrentVersion( this.getClass().getName(), getVersion() );

		for( DataManager<? extends Data> mgr : managers.values() )
		{
			mgr.create( this );

			setCurrentVersion( mgr.getClass().getName(), mgr.getVersion() );
		}
	}

	private boolean createCalled = false;

	@Override
	protected void create( Database db )
		throws SQLException
	{
		createCalled = true;
	}

	// -------------------------------------------------------------------------

	protected void internalUpdate()
		throws SQLException
	{
		Version currentVersion = getCurrentVersion( this.getClass().getName() );

		if( currentVersion.compareTo( getVersion() ) != 0 )
		{
			if( currentVersion.compareTo( getVersion() ) > 0 )
			{
				// FIXME downgrading !?
			}

			update( this, currentVersion );
			if( updateCalled != true )
			{
				log.severe( "super.update( java.runtime.Version ) not called !" );
				// FIXME super.update not called
			}

			setCurrentVersion( this.getClass().getName(), getVersion() );
		}

		for( DataManager<? extends Data> mgr : managers.values() )
		{
			currentVersion = getCurrentVersion( mgr.getClass().getName() );

			if( currentVersion.compareTo( mgr.getVersion() ) != 0 )
			{
				if( currentVersion.compareTo( mgr.getVersion() ) > 0 )
				{
					// FIXME downgrading !?
				}

				mgr.update( this, currentVersion );

				setCurrentVersion( mgr.getClass().getName(), mgr.getVersion() );
			}
		}
	}

	private boolean updateCalled = false;

	@Override
	protected void update( Database db, Version oldVersion )
		throws SQLException
	{
		updateCalled = true;
	}

	// -------------------------------------------------------------------------

	protected Version getCurrentVersion( String resourceName )
	{
		String version = getProperty( resourceName );

		return version == null ? null : Version.parse( version );
	}

	protected void setCurrentVersion( String resourceName, Version newVersion )
	{
		String version = newVersion.toString();

		setProperty( resourceName, version );
	}

	// =========================================================================
	// === 
	// =========================================================================

	public abstract void setProperty( String key, String value );

	public String getProperty( String key )
	{
		return getProperty( key, null );
	}

	public abstract String getProperty( String key, String defaultValue );

	// =========================================================================
	// = class: DataManager ====================================================
	// =========================================================================

	@Override
	public final void save( Data entry )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final Data load( long id )
	{
		throw new UnsupportedOperationException();
	}

	// =========================================================================
	// = 
	// =========================================================================

	/**
	 * 
	 * @return
	 */
	public String getDbName()
	{
		return dbName;
	}

	// =========================================================================
	// === Scripts execution ===================================================
	// =========================================================================

	/**
	 * <ul>
	 * 	<li>Skip comment lines starting with '--'</li>
	 * 	<li>Skip white lines</li>
	 * 	<li>Concatenate lines ending with '\'</li>
	 * 	<li>Remove extra blanks</li>
	 * </ul>
	 * 
	 * @param resourceName
	 * @throws IOException
	 */
	public void parseCreateScript( String resourceName )
		throws IOException, SQLException
	{
		parseCreateScript( resourceName, defaultUpdateExecutor );
	}

	/**
	 * <ul>
	 * 	<li>Skip comment lines starting with '--'</li>
	 * 	<li>Skip white lines</li>
	 * 	<li>Concatenate lines ending with '\'</li>
	 * 	<li>Remove extra blanks</li>
	 * </ul>
	 * 
	 * @param resourceName
	 * @param c
	 * @throws IOException
	 */
	public void parseCreateScript( String resourceName, SQLWorker<String> w )
		throws IOException, SQLException
	{
		final InputStream is = getClass().getResourceAsStream( resourceName );
		if( is == null )
		{
			throw new IOException( "Failed to access resource as stream" );
		}

		final InputStreamReader isr = new InputStreamReader( is );
		final BufferedReader br = new BufferedReader( isr );

		// ----------------------------------------------------------------------

		String current = null;
		while( (current = br.readLine()) != null )
		{
			if( (current.trim().length() == 0) || (current.startsWith( "--" ) ) )
			{
				continue;
			}

			final boolean shouldJoin = current.endsWith( "\\" );
			if( shouldJoin )
			{
				String prev = current.substring( 0, current.length() - 1 );
				
				while( (current = br.readLine()) != null )
				{
					if( current.trim().length() == 0 )
					{
						continue;
					}

					final boolean shouldJoin2 = current.endsWith( "\\" );
					if( ! shouldJoin2 )
					{
						current = prev + current;
						break;
					}
					else
					{
						prev += current.substring( 0, current.length() - 1 );
					}
				}					
			}

			current = current.replaceAll( "(\t| )+", " " );

			w.accept( con, current );
		}

		br.close();
		isr.close();
		is.close();
	}

	// -------------------------------------------------------------------------

	private final SQLWorker<String > defaultUpdateExecutor
		= new SQLWorker<String>()
	{
		@Override
		public void accept( Connection con, String request )
			throws SQLException
		{
			Statement stmtCreate = null;

			try
			{
				stmtCreate = con.createStatement();
				stmtCreate.executeUpdate( request );
				stmtCreate.close();
			}
			finally
			{
				if( (stmtCreate != null) && ! stmtCreate.isClosed() )
				{
					stmtCreate.close();
				}
			}
		}
	};

	// =========================================================================
	// === LOGGING =============================================================
	// =========================================================================

	public void dumpTable( String tableName )
	{
		TableDumper.printTable( con, tableName );		
	}

	public void dumpResultSet( ResultSet rs )
	{
		TableDumper.printResultSet( rs );
	}

	// -------------------------------------------------------------------------

	private static final Logger log
		= Logger.getLogger( Database.class.getName() );

}
