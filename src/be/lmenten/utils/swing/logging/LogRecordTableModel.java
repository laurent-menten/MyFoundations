package be.lmenten.utils.swing.logging;

import java.util.logging.LogRecord;

import javax.swing.table.TableModel;

public interface LogRecordTableModel
	extends TableModel
{
	public int getColumnWidth( int columnIndex );

	public void addRecord( LogRecord record );
}
