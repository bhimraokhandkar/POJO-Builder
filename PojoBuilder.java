import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PojoBuilder
{
	static Properties properties = new Properties();
	private Connection getConnection()
	{
		Connection connection=null;
		String database_name =properties.get("database_name").toString();
		String database_url =properties.get("database_url").toString();
		String database_username =properties.get("database_username").toString();
		String database_password =properties.get("database_password").toString();
		String url=database_url+database_name+"?autoReconnect=true";
		
		try
		{
			connection=DriverManager.getConnection(url,database_username,database_password);
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return connection ;
	}
	
	private void createPojo(String tableName,Map<String,List<PojoColumnBean>> reverseAssociationMap )
	{
		System.out.println("=================================="+tableName+"==========START=====================");
		
		String generate_hbm =  properties.get("generate_hbm").toString();
		String generate_reverse_association =  properties.get("generate_reverse_association").toString();
		String generate_association =  properties.get("generate_association").toString();
		Map<String,String> tableMap = null;
		List<PojoColumnBean> reverseAssociationList = null;
		
		PojoDefinitionBean pojoDefinitionBean = new PojoDefinitionBean();
		
		if("true".equalsIgnoreCase(generate_association))
		{
			tableMap = getTablesForeignKey(tableName);
		}
			
		if("true".equalsIgnoreCase(generate_reverse_association))
		{
			if(reverseAssociationMap!=null && reverseAssociationMap.size()>0)
			{
				reverseAssociationList = reverseAssociationMap.get(tableName);
				pojoDefinitionBean.setIsReverseAssociation(true);
			}
		}
		
		String className = tableName.replaceAll("_","");
		String classFirstLetter = className.substring(0, 1).toUpperCase();
		className =  classFirstLetter+className.substring(1);
		
		pojoDefinitionBean.setClassName(className);
		pojoDefinitionBean.setTableName(tableName);
		
		List<PojoColumnBean> defBeans = getTableInfo(tableName,tableMap,className);
		
		if(defBeans!=null && defBeans.size()>0 && reverseAssociationList!=null && reverseAssociationList.size()>0)
		{
			defBeans.addAll(reverseAssociationList);
		}
		
		pojoDefinitionBean.setPojoColumnBean(defBeans);
		 
		generateJavaFile(pojoDefinitionBean);
		
		if("true".equalsIgnoreCase(generate_hbm))
			generateHibernateFile(pojoDefinitionBean);
		  
		System.out.println("=================================="+tableName+"==========END=====================");
	}

	private List<PojoColumnBean> getTableInfo(String tableName, Map<String, String> tableMap, String className)
	{
		String packagename =  properties.get("packagename").toString();
		String generate_association =  properties.get("generate_association").toString();
		String sql ="SHOW COLUMNS FROM "+tableName;
		Connection connection=null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<PojoColumnBean> defBeans = new ArrayList<PojoColumnBean>(); 
		
		try 
		{
			  connection = getConnection();
			  preparedStatement= connection.prepareStatement(sql);
			  resultSet = preparedStatement.executeQuery();
			  if(resultSet!=null )
			  {
				  while (resultSet.next())
				  {
				       String fieldName = resultSet.getString(1);
				       String fieldType = resultSet.getString(2);
				       boolean isNullable = "YES".equals(resultSet.getString(3)) ? false : true;
				       boolean isPrimaryKey = "PRI".equals(resultSet.getString(4))  ? true : false;
				       boolean isForeignKey = "MUL".equals(resultSet.getString(4))  ? true : false;
				       boolean isUniqueKey = "UNI".equals(resultSet.getString(4))  ? true : false;
				       boolean isAutoIncr = resultSet.getString(6)!=null && !"".equals(resultSet.getString(6))  ? true : false;
				       
				       
				       String sqlType = fieldType.contains("(") ? fieldType.substring(0,fieldType.indexOf("(")) : fieldType;
				       int length = 0;
				       String javaType = "";
				       String hbmJavaType = "";
				       String referenceTable = null;
				       
				       if("bigint".equals(sqlType) || "long".equals(sqlType))
				       {
				    	   javaType = "Long";
				    	   hbmJavaType = "java.lang.Long";
				    	   length = fieldType.contains("(") ? Integer.parseInt(fieldType.substring(fieldType.indexOf("(")+1,fieldType.indexOf(")"))) : 0;
				       }
				    	  
				       if("varchar".equals(sqlType) || "char".equals(sqlType) || "text".equals(sqlType))
				       {
				    	   javaType = "String";
				    	   hbmJavaType = "java.lang.String";
				    	   length = fieldType.contains("(") ? Integer.parseInt(fieldType.substring(fieldType.indexOf("(")+1,fieldType.indexOf(")"))) : 0;
				       }  
				    	   
				       if("datetime".equals(sqlType) || "time".equals(sqlType) || "date".equals(sqlType) || "timestamp".equals(sqlType))
				       {
				    	   javaType = "java.util.Date";
				    	   hbmJavaType = "java.util.Date";
				       }
				    	   
				       if("int".equals(sqlType) || "tinyint".equals(sqlType) || "smallint".equals(sqlType) || "unsigned".equals(sqlType))
				       {
				    	   javaType = "Integer";
				    	   hbmJavaType = "java.lang.Integer";
				    	   length = fieldType.contains("(") ? Integer.parseInt(fieldType.substring(fieldType.indexOf("(")+1,fieldType.indexOf(")"))) : 0;
				       }
				    	   
				       if("decimal".equals(sqlType))
				       {
				    	   javaType = "java.math.BigDecimal";
				    	   hbmJavaType = "java.math.BigDecimal";
				       }
				    	
				       if("double".equals(sqlType))
				       {
				    	   javaType = "Double";
				    	   hbmJavaType = "java.lang.Double";
				       }
				       
				       PojoColumnBean pojoColumnBean = new PojoColumnBean();
				       
				       /**
				        * Code added to make relationship
				        * */
				       if("true".equalsIgnoreCase(generate_association) && tableMap!=null && tableMap.size()>0)
				       {
				    	   if(tableMap.containsKey(fieldName))
					       {
					    	   referenceTable = tableMap.get(fieldName);
					    	   String refClassName = referenceTable.replaceAll("_","");
							   String firstLetter = refClassName.substring(0, 1).toUpperCase();
							   refClassName =  firstLetter+refClassName.substring(1);
					    	   javaType = packagename+"."+refClassName;
					    	   
					    	   pojoColumnBean.setReferenceClassName(packagename+"."+refClassName);
					    	   pojoColumnBean.setForeignKey(isForeignKey);
					       }
				       }
				       
				       pojoColumnBean.setColumnName(fieldName);
				       pojoColumnBean.setFieldName(fieldName);
				       pojoColumnBean.setReferenceTable(referenceTable);
				       pojoColumnBean.setJavaType(javaType);
				       pojoColumnBean.setHbmJavaType(hbmJavaType);
				       pojoColumnBean.setSqlType(sqlType);
				       
				       pojoColumnBean.setNullable(isNullable);
				       pojoColumnBean.setLength(length);
				       pojoColumnBean.setPrimaryKey(isPrimaryKey);
				       pojoColumnBean.setAutoIncr(isAutoIncr);
				       pojoColumnBean.setUniqueKey(isUniqueKey);
				       defBeans.add(pojoColumnBean);
				  }
			  }
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		finally
		{
				try
				{
					if(connection!=null)
						connection.close();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
		}
		return defBeans;
	}

	private void generateHibernateFile(PojoDefinitionBean pojoDefinitionBean) 
	{
		String packagename =  properties.get("packagename").toString();
		String foldername =  properties.get("foldername").toString();
		
		List<PojoColumnBean> defBeans = pojoDefinitionBean.getPojoColumnBean();
		if(defBeans!=null && defBeans.size()>0)
		{
			  StringBuffer stringBuffer = new StringBuffer();
			  stringBuffer.append("<?xml version=\"1.0\"?>\n");
			  stringBuffer.append("<!DOCTYPE hibernate-mapping PUBLIC \n\t\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\" \n\t\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\" > \n\n");
			  stringBuffer.append("<hibernate-mapping>\n");
			  stringBuffer.append("\t<class name=\""+packagename+"."+pojoDefinitionBean.getClassName()+"\" table=\""+pojoDefinitionBean.getTableName()+"\" >\n");
			  		  
			  for (Iterator iterator = defBeans.iterator(); iterator .hasNext();)
			  {
				  PojoColumnBean pojoColumnBean = (PojoColumnBean) iterator.next();
				  String fieldName = pojoColumnBean.getFieldName();
				  
				  if(pojoColumnBean.isPrimaryKey())
				  {
					  stringBuffer.append("\t\t <id name=\""+fieldName+"\" type=\""+pojoColumnBean.getHbmJavaType()+"\" column =\""+pojoColumnBean.getColumnName()+"\" >\n");
					  stringBuffer.append(" \t\t \t\t <generator class=\"native\" /> \n");
					  stringBuffer.append("\t\t </id> \n ");
				  }
				  else if(pojoColumnBean.isForeignKey())
				  {
					  stringBuffer.append("\t\t <many-to-one name=\""+fieldName+"\"  class=\""+pojoColumnBean.getReferenceClassName()+"\" ");
					  if(pojoColumnBean.isNullable())
						  stringBuffer.append("not-null=\""+pojoColumnBean.isNullable()+"\" ");
					  stringBuffer.append("> \n ");
					  
					  stringBuffer.append(" \t\t \t\t <column name=\""+pojoColumnBean.getColumnName()+"\" /> \n");
					  stringBuffer.append("\t\t </many-to-one> \n");
				  } 
				  else if(pojoColumnBean.getReversedClassName()!=null)
				  {
						String className =  pojoColumnBean.getReversedClassName();
						String columnName =  pojoColumnBean.getReversedColumnName();
						String smallLetter = className.substring(0, 1).toLowerCase();
						String columnFirstLetter = columnName.substring(0, 1).toUpperCase();
						String associationFieldName =smallLetter+className.substring(1)+columnFirstLetter+columnName.substring(1)+"s";
						
						stringBuffer.append("\n\t\t <set name=\""+associationFieldName+"\" lazy=\"true\" inverse=\"true\" cascade=\"none\" > \n");
						
						stringBuffer.append("\t\t\t\t <key> \n");
						stringBuffer.append("\t\t\t\t\t\t <column name=\""+columnName+"\" /> \n");
						stringBuffer.append("\t\t\t\t </key> \n");
						stringBuffer.append("\t\t\t\t <one-to-many class=\""+packagename+"."+className+"\" />\n");
						
						stringBuffer.append("\t\t </set> \n");
				  }
				  else
				  {
					  stringBuffer.append("\t\t <property name=\""+fieldName+"\"  type=\""+pojoColumnBean.getHbmJavaType()+"\"  ");
					  stringBuffer.append(" column=\""+pojoColumnBean.getColumnName()+"\" ");
					  if(pojoColumnBean.isNullable())
						  stringBuffer.append("not-null=\""+pojoColumnBean.isNullable()+"\" ");
					  if(pojoColumnBean.isUniqueKey())
						  stringBuffer.append("unique=\""+pojoColumnBean.isUniqueKey()+"\" length=\""+pojoColumnBean.getLength()+"\" ");
					  if(pojoColumnBean.getLength()>0)
						  stringBuffer.append("length=\""+pojoColumnBean.getLength()+"\" ");
					  stringBuffer.append("/> \n  ");
				  }
			  }
			  
			  stringBuffer.append("\t</class>\n");
			  stringBuffer.append("</hibernate-mapping>");
			  String hbmFileName = pojoDefinitionBean.getClassName()+".hbm.xml";
			  createFile(  hbmFileName,foldername,stringBuffer.toString());
		 }
		
	}

	private void generateJavaFile(PojoDefinitionBean pojoDefinitionBean)
	{
		String packagename =  properties.get("packagename").toString();
		String foldername =  properties.get("foldername").toString();
		
		List<PojoColumnBean> defBeans = pojoDefinitionBean.getPojoColumnBean();
		if(defBeans!=null && defBeans.size()>0)
		{
			  StringBuffer stringBuffer = new StringBuffer();
			  stringBuffer.append("package "+packagename+";\n");
			  
			  if(pojoDefinitionBean.isReverseAssociation())
				  stringBuffer.append("import java.util.Set;\n");
			  
			  stringBuffer.append("public class "+pojoDefinitionBean.getClassName()+"\n{\n");
			  for (Iterator iterator = defBeans.iterator(); iterator.hasNext();)
			  {
				  PojoColumnBean pojoColumnBean = (PojoColumnBean) iterator.next();
				  if(pojoColumnBean.getJavaType()!=null)
				  {
					  stringBuffer.append("\t private  " + pojoColumnBean.getJavaType() +"  "+pojoColumnBean.getFieldName()+";\n");
				  }
				  if(pojoColumnBean.getReversedClassName()!=null)
				  {
						String className =  pojoColumnBean.getReversedClassName();
						String columnName =  pojoColumnBean.getReversedColumnName();
						String classFirstLetter = className.substring(0, 1).toLowerCase();
						String columnFirstLetter = columnName.substring(0, 1).toUpperCase();
						String fieldName =classFirstLetter+className.substring(1)+columnFirstLetter+columnName.substring(1)+"s";
						stringBuffer.append("\t private  Set<" + packagename+"."+className +">  "+fieldName+";\n"); 
				  }
			  }
			  
			  for (Iterator iterator = defBeans.iterator(); iterator.hasNext();)
			  {
				  PojoColumnBean pojoColumnBean = (PojoColumnBean) iterator.next();
				  if(pojoColumnBean.getFieldName()!=null)
				  {
					  String fieldName = pojoColumnBean.getFieldName();
					  String firstLetter = fieldName.substring(0, 1).toUpperCase();
						
					  stringBuffer.append(" \n\tpublic  void set"+firstLetter+fieldName.substring(1)+" ("+ pojoColumnBean.getJavaType()  +" "+ fieldName+") \n");
					  stringBuffer.append(" \t{\n       \t\t this."+fieldName+"="+fieldName+"; \n");
					  stringBuffer.append(" \t}\n\n ");
					
					  stringBuffer.append("\tpublic  "+pojoColumnBean.getJavaType() +" get"+firstLetter+fieldName.substring(1)+" () \n");
					  stringBuffer.append(" \t{\n      \t\t return "+fieldName+"; \n");
					  stringBuffer.append(" \t} \n");
				  }
				  
				  if(pojoColumnBean.getReversedClassName()!=null)
				  {
						String className =  pojoColumnBean.getReversedClassName();
						String columnName =  pojoColumnBean.getReversedColumnName();
						String smallLetter = className.substring(0, 1).toLowerCase();
						String columnFirstLetter = columnName.substring(0, 1).toUpperCase();
						String fieldName =smallLetter+className.substring(1)+columnFirstLetter+columnName.substring(1)+"s";
						
						String methodName = fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
						
						stringBuffer.append(" \n\tpublic  void set"+methodName+"s (Set<"+packagename+"."+className+"> "+fieldName+") \n");
						stringBuffer.append(" \t{\n       \t\t this."+fieldName+"="+fieldName+"; \n");
						stringBuffer.append(" \t}\n\n ");
						
						stringBuffer.append("\tpublic  Set<"+packagename+"."+className+"> get"+methodName+" () \n");
						stringBuffer.append(" \t{\n      \t\t return "+fieldName+"; \n");
						stringBuffer.append(" \t} \n");

				  }
			  }
			  
			  stringBuffer.append("\n}");
			  String javaFileName = pojoDefinitionBean.getClassName()+".java";
			  createFile(  javaFileName,foldername,stringBuffer.toString());
		 }
	}

	private void createFile(String filename,String filepath  , String fileData)
	{
		new File(filepath).mkdirs();
		File file = new File(filepath+filename);
		PrintWriter printWriter = null ;
		try
		{
			printWriter = new PrintWriter(new FileWriter(file));
		    printWriter.println(fileData);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(printWriter != null )
				printWriter.close();
		}
	}
	
	private List getTotalTables()
	{
		String sql ="SHOW TABLES";
		Connection connection=null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<String> tableList = new ArrayList<String>();
		try 
		{
			  connection = getConnection();
			  preparedStatement= connection.prepareStatement(sql);
			  resultSet = preparedStatement.executeQuery();
			  if(resultSet!=null )
			  {
				  while (resultSet.next())
				  {
					 // System.out.println(" "+resultSet.getString(1)+" ");
					  tableList.add(resultSet.getString(1));
				  }
			}
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		finally
		{
				try
				{
					if(connection!=null)
						connection.close();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
		}
		return tableList;
	}
	
	private Map getTablesForeignKey(String tableName)
	{
		long start_time =System.currentTimeMillis ();
		String sql ="SELECT COLUMN_NAME, REFERENCED_TABLE_NAME FROM  INFORMATION_SCHEMA.KEY_COLUMN_USAGE "
				+ " WHERE TABLE_NAME= '"+tableName+"' AND REFERENCED_TABLE_NAME != '' ";
		Connection connection=null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Map<String,String> tableMap = new HashMap<String,String>();
		try 
		{
			  connection = getConnection();
			  preparedStatement= connection.prepareStatement(sql);
			  resultSet = preparedStatement.executeQuery();
			  if(resultSet!=null )
			  {
				  while (resultSet.next())
				  {
					  tableMap.put(resultSet.getString(1), resultSet.getString(2));
				  }
			  }
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		finally
		{
				try
				{
					if(connection!=null)
						connection.close();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
		}
		System.out.println("TIME TAKEN TO GET FOREIGNKEY FOR : "+tableName+" =======>> "+(System.currentTimeMillis ()-start_time)+" ms");
		return tableMap;
	}
	
	private Map<String,List<PojoColumnBean>> getAllTableAssociates()
	{
		long start_time =System.currentTimeMillis ();
		String database_name = properties.get("database_name").toString();
		String sql ="SELECT TABLE_NAME,COLUMN_NAME,REFERENCED_TABLE_NAME FROM information_schema.KEY_COLUMN_USAGE "
				+ " WHERE TABLE_SCHEMA= '"+database_name+"' AND REFERENCED_TABLE_NAME!='' ";
		Connection connection=null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Map<String,List<PojoColumnBean>> reverseAssociationMap = new HashMap<String, List<PojoColumnBean>>();
		try 
		{
			  connection = getConnection();
			  preparedStatement= connection.prepareStatement(sql);
			  resultSet = preparedStatement.executeQuery();
			  if(resultSet!=null )
			  {
				  while (resultSet.next())
				  {
					  String table_name = resultSet.getString(1);
					  String column_name = resultSet.getString(2);
					  String referenced_table_name = resultSet.getString(3);
					  
					  String className = table_name.replaceAll("_","");
					  String classFirstLetter = className.substring(0, 1).toUpperCase();
					  className =  classFirstLetter+className.substring(1);
					  
					  List<PojoColumnBean> reverseAssociationList = null;
					  if(reverseAssociationMap.containsKey(referenced_table_name))
					  {
						  reverseAssociationList = reverseAssociationMap.get(referenced_table_name);
					  }
					  else
					  {
						  reverseAssociationList = new ArrayList<PojoColumnBean>();
					  }
					  PojoColumnBean pojoColumnBean = new PojoColumnBean();
					  pojoColumnBean.setReversedColumnName(column_name);
					  pojoColumnBean.setReversedClassName(className);
					  reverseAssociationList.add(pojoColumnBean);
					  reverseAssociationMap.put(referenced_table_name, reverseAssociationList);
				  }
			  }
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		finally
		{
				try
				{
					if(connection!=null)
						connection.close();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
		}
		System.out.println("TIME TAKEN TO GET ALL ASSOCIATES FOR : "+database_name+" =======>> "+(System.currentTimeMillis ()-start_time)+" ms");
		return reverseAssociationMap;
	}
	
	private void generateAllPojo()
	{
		long startTime = System.currentTimeMillis ();
		
		Map<String,List<PojoColumnBean>> reverseAssociationMap = getAllTableAssociates();
		List<String> tableList =  getTotalTables();
		for (Iterator iterator = tableList.iterator(); iterator.hasNext();)
		{
			String string = (String) iterator.next();
			createPojo(string,reverseAssociationMap);
		}
		
		long totalMilliSeconds = System.currentTimeMillis () - startTime;
	    long totalSecs = totalMilliSeconds/1000;
	    System.out.println("#########  Time Taken  ==== "+totalSecs + " seconds "+" to generate "+tableList.size()+" tables");
	}
	
	public static void main(String[] args)
	{
		try
		{
			properties.load(PojoBuilder.class.getResourceAsStream("pojo.properties"));
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		PojoBuilder pojoBuilder = new PojoBuilder();
		pojoBuilder.generateAllPojo();
	}

}
