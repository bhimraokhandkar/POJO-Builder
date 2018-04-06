public class PojoColumnBean
{
	private String columnName ;
	
	private String fieldName ;
	
	private String referenceTable;
	
	private String referenceClassName;
	
	private String reversedClassName;
	
	private String reversedColumnName;
	
	private String sqlType ;
	
	private String javaType ;
	
	private String hbmJavaType ;
	
	private boolean isNullable ;
	
	private boolean isPrimaryKey ;
	
	private boolean isForeignKey ;
	
	private boolean isUniqueKey;
	
	private boolean isAutoIncr ;
	
	private int length = 0;

	public String getColumnName()
	{
		return columnName;
	}

	public void setColumnName(String columnName) 
	{
		this.columnName = columnName;
	}

	public String getFieldName()
	{
		return fieldName;
	}

	public void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}
	
	public String getReferenceTable()
	{
		return referenceTable;
	}
	
	public void setReferenceTable(String referenceTable) 
	{
		this.referenceTable = referenceTable;
	}

	public String getReferenceClassName() 
	{
		return referenceClassName;
	}

	public void setReferenceClassName(String referenceClassName) 
	{
		this.referenceClassName = referenceClassName;
	}
	
	public String getReversedClassName() 
	{
		return reversedClassName;
	}

	public void setReversedClassName(String reversedClassName)
	{
		this.reversedClassName = reversedClassName;
	}

	public String getReversedColumnName()
	{
		return reversedColumnName;
	}

	public void setReversedColumnName(String reversedColumnName)
	{
		this.reversedColumnName = reversedColumnName;
	}

	public String getSqlType() 
	{
		return sqlType;
	}

	public void setSqlType(String sqlType)
	{
		this.sqlType = sqlType;
	}

	public String getJavaType()
	{
		return javaType;
	}

	public void setJavaType(String javaType)
	{
		this.javaType = javaType;
	}
	
	public String getHbmJavaType()
	{
		return hbmJavaType;
	}

	public void setHbmJavaType(String hbmJavaType)
	{
		this.hbmJavaType = hbmJavaType;
	}

	public boolean isNullable()
	{
		return isNullable;
	}

	public void setNullable(boolean isNullable)
	{
		this.isNullable = isNullable;
	}

	public boolean isPrimaryKey() 
	{
		return isPrimaryKey;
	}

	public void setPrimaryKey(boolean isPrimaryKey)
	{
		this.isPrimaryKey = isPrimaryKey;
	}

	public boolean isForeignKey()
	{
		return isForeignKey;
	}

	public void setForeignKey(boolean isForeignKey) 
	{
		this.isForeignKey = isForeignKey;
	}

	public boolean isUniqueKey() 
	{
		return isUniqueKey;
	}

	public void setUniqueKey(boolean isUniqueKey) 
	{
		this.isUniqueKey = isUniqueKey;
	}

	public boolean isAutoIncr()
	{
		return isAutoIncr;
	}

	public void setAutoIncr(boolean isAutoIncr) 
	{
		this.isAutoIncr = isAutoIncr;
	}

	public int getLength() 
	{
		return length;
	}

	public void setLength(int length) 
	{
		this.length = length;
	}
}
