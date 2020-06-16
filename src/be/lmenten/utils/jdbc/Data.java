package be.lmenten.utils.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * @param <T>
 *
 * @author Laurent Menten
 * @version 1.0, (10 Jun 2020)
 * @since 1.0
 */
public abstract class Data
{
	public static final String COL_ID = "id";

	private long id;

	// =========================================================================
	// ===
	// =========================================================================

	public Data()
	{
	}

	public Data( ResultSet rs )
		throws SQLException
	{
		id = rs.getLong( COL_ID );
	}

	// =========================================================================
	// ===
	// =========================================================================
	
	public void setId( long id )
	{
		this.id = id;
	}

	public long getId()
	{
		return id;
	}
}
