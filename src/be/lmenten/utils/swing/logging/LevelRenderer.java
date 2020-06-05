package be.lmenten.utils.swing.logging;

import java.awt.Color;
import java.awt.Component;
import java.util.logging.Level;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class LevelRenderer
	extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 1L;

	private Color defaultColor;

	public LevelRenderer()
	{
		defaultColor = getBackground();
	}

	/**
	 *
	 */
	@Override
	public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
	{
		if( value instanceof Level )
		{
			Level level = (Level)value;
			if( level == Level.SEVERE )
			{
				setBackground( Color.RED );
			}
			else if( level == Level.WARNING )
			{
				setBackground( Color.ORANGE );
			}
			else if( level == Level.INFO )
			{
				setBackground( Color.GREEN );
			}
			else
			{
				setBackground( defaultColor );
			}
		}

		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
}
