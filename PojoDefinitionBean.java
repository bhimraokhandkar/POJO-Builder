import java.util.List;

public class PojoDefinitionBean
{
	private String tableName ;
	
	private String className ;
	
	private boolean isReverseAssociation;
	
	private List<PojoColumnBean> pojoColumnBean;

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName(String tableName) 
	{
		this.tableName = tableName;
	}

	public String getClassName() 
	{
		return className;
	}

	public void setClassName(String className)
	{
		this.className = className;
	}

	public boolean isReverseAssociation()
	{
		return isReverseAssociation;
	}

	public void setIsReverseAssociation(boolean isReverseAssociation)
	{
		this.isReverseAssociation = isReverseAssociation;
	}

	public List<PojoColumnBean> getPojoColumnBean()
	{
		return pojoColumnBean;
	}

	public void setPojoColumnBean(List<PojoColumnBean> pojoColumnBean) {
		this.pojoColumnBean = pojoColumnBean;
	}
	
}
