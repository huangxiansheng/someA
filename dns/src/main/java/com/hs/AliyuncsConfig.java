package com.hs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

@Configuration
public class AliyuncsConfig {
	
    @Autowired
    private Environment environment;
    
	@Bean
	public IAcsClient getClient() {
		String regionId = "cn-hangzhou"; // 必填固定值，必须为“cn-hanghou”
		String accessKeyId = environment.getProperty("accessKeyId"); // your accessKey
		String accessKeySecret = environment.getProperty("accessKeySecret");// your accessSecret
		IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
		// 若报Can not find endpoint to access异常，请添加以下此行代码
		// DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", "Alidns",
		// "alidns.aliyuncs.com");
		IAcsClient client = new DefaultAcsClient(profile);
		return client;
	}
	
	@Bean
	public String domainName() {
		return environment.getProperty("domainName");
	}
	@Bean
	public String typeKey() {
		return environment.getProperty("typeKey");
	}
	@Bean
	public String rrKey() {
		return environment.getProperty("rrKey");
	}
	@Bean
	public Boolean checkFlag() {
		return Boolean.valueOf(environment.getProperty("checkFlag")); //是否强制远程验证    true 验证，false如本地相同 则不远程验证
	}

}
