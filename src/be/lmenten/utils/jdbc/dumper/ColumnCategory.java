package be.lmenten.utils.jdbc.dumper;

import java.sql.Types;

public enum ColumnCategory
{
	OTHER,
	STRING,
	INTEGER,
	DOUBLE,
	DATETIME,
	BOOLEAN,
	;

	public static ColumnCategory whichCategory( int type )
   {
      switch (type) {
          case Types.BIGINT:
          case Types.TINYINT:
          case Types.SMALLINT:
          case Types.INTEGER:
              return INTEGER;

          case Types.REAL:
          case Types.DOUBLE:
          case Types.DECIMAL:
              return DOUBLE;

          case Types.DATE:
          case Types.TIME:
          case Types.TIME_WITH_TIMEZONE:
          case Types.TIMESTAMP:
          case Types.TIMESTAMP_WITH_TIMEZONE:
              return DATETIME;

          case Types.BOOLEAN:
              return BOOLEAN;

          case Types.VARCHAR:
          case Types.NVARCHAR:
          case Types.LONGVARCHAR:
          case Types.LONGNVARCHAR:
          case Types.CHAR:
          case Types.NCHAR:
              return STRING;

          default:
              return OTHER;
      }
  }
}
