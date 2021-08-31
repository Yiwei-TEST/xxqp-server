package com.sy.sanguo.common.util;

import com.sy.mainland.util.PropertiesEncryptFactoryBean;

public class SyPropertiesEncryptFactoryBean extends PropertiesEncryptFactoryBean {
    @Override
    public String decrypt(String str) {
        try {
            return new net.sy599.common.security.SecuritConstantImpl().decrypt(str);
        }catch (Exception e){
            return str;
        }
    }
}
