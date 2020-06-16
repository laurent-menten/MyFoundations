package be.lmenten.utils.jdbc;

import java.lang.Runtime.Version;

public abstract class Updatable
{
	public abstract Version getVersion();

	// -------------------------------------------------------------------------

	protected abstract void create( Database db )
		throws Exception;

	protected abstract void update( Database db, Version oldVersion )
		throws Exception;
}
