package be.lmenten.utils.jdbc;

import java.lang.Runtime.Version;
import java.sql.SQLException;

import be.lmenten.utils.plugin.Plugin;

public abstract class DataManager<T extends Data>
	extends Updatable
	implements Plugin
{
	@Override
	protected abstract void create( Database db )
		throws SQLException;

	@Override
	protected abstract void update( Database db, Version oldVersion )
		throws SQLException;

	// -------------------------------------------------------------------------

	public abstract T load( long id );
	public abstract void save( T entry );
}
