package be.lmenten.utils.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLWorker<T>
{
	public void accept( Connection con, T object )
		throws SQLException;
}
