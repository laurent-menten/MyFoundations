package be.lmenten.utils.jdbc.dumper;

import java.util.ArrayList;
import java.util.List;

public class Column
{
   private String label;
   private int type;
   private String typeName;
   private int width = 0;

   private List<String> values = new ArrayList<>();

   private String justifyFlag = "";

   private ColumnCategory typeCategory;

   public Column( String label, int type, String typeName )
   {
       this.label = label;
       this.type = type;
       this.typeName = typeName;
   }

   public String getLabel()
   {
       return label;
   }

   public int getType()
   {
       return type;
   }

   public String getTypeName()
   {
       return typeName;
   }

   public int getWidth()
   {
       return width;
   }

   public void setWidth( int width )
   {
       this.width = width;
   }

   public void addValue(String value)
   {
       values.add(value);
   }

   public String getValue(int i)
   {
       return values.get(i);
   }

   public String getJustifyFlag()
   {
       return justifyFlag;
   }

   public void justifyLeft()
   {
       this.justifyFlag = "-";
   }


   public ColumnCategory getTypeCategory()
   {
       return typeCategory;
   }

   public void setTypeCategory( ColumnCategory typeCategory )
   {
       this.typeCategory = typeCategory;
   }
}
