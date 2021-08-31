package net.sy599.common.security;

import java.util.Properties;

import org.springframework.beans.factory.FactoryBean;

public class PropertiesEncryptFactoryBean implements FactoryBean {
	
	private Properties properties;
	

	public Object getObject() throws Exception {
		return getProperties();
	}

	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return Properties.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public Properties getProperties() {
		return properties;
	}
	
	public void setProperties(Properties inProperties) {
		this.properties = inProperties;
		String originalUsername = properties.getProperty("user");
		String originalPassword = properties.getProperty("password");
		
		SecuritConstant securitConstant;
		try {
			securitConstant = new SecuritConstantImpl();
			properties.put("user", originalUsername);
			if (originalPassword != null){
				String newPassword = securitConstant.decrypt(originalPassword);
				properties.put("password", newPassword);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
