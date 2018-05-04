package com.hs.scheduled;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.AddDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsResponse;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsResponse.Record;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordRequest;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.hs.util.IpUtil;

@Component
public class DNSTasks {

    private static final Logger log = LoggerFactory.getLogger(DNSTasks.class);
    @Autowired
    private String domainName ;
    @Autowired
    private String typeKey ;
    @Autowired
    private String rrKey;
    @Autowired
    private boolean checkFlag ;
    
    private  String selfIp = "";
    
    @Autowired
    private IAcsClient client;
    
    @Scheduled(fixedRate = 10000)
    public void reportCurrentTime() {
    	
    	String ip = IpUtil.getV4IP();//获取本地ip
    	log.info("获取本地外网ip:"+ip);
    	log.info("系统缓存保存ip:"+selfIp);
    	
    	if(selfIp.equals(ip)&&!checkFlag) {//如果ip和本地ip相等，则不用去验证
    		log.info("本地ip和修改过的ip是同一个，不再远程验证");
    		return;
    	}
    	
    	List<Record> list = new ArrayList<Record>();
    	boolean updateFlag = checkIpInDns(ip,list);
    	
    	log.info("是否需要修改DNS解析updateFlag："+updateFlag);
    	if(updateFlag) {
    		if(null == list|| list.isEmpty()) { // 如果list为空，则为新增DNS解析记录,否则为修改dns记录
    			addDNS(ip);
    			log.info("新增DNS解析IP："+ip);
    		}else {
    			updateDNS(list,ip);
    			log.info("修改DNS解析IP:"+ip);
    		}
    	}
    	selfIp = ip;
    }

    private void updateDNS(List<Record> list, String ip) {
		for (Record record : list) {
			UpdateDomainRecordRequest request = new UpdateDomainRecordRequest();
			
	    	request.setProtocol(ProtocolType.HTTPS); // 指定访问协议
			request.setAcceptFormat(FormatType.JSON); // 指定api返回格式
			request.setMethod(MethodType.POST); // 指定请求方法
			request.setRegionId("cn-hangzhou");// 指定要访问的Region,仅对当前请求生效，不改变client的默认设置。
			
	    	request.setActionName("UpdateDomainRecord");
	    	
	    	request.setRecordId(record.getRecordId());
	    	request.setRR(rrKey);
	    	request.setType(typeKey);
	    	request.setValue(ip);
	    	
	    	log.info("修改DNS解析配置");
	    	log.info("原IP:"+record.getValue());
	    	log.info("新IP:"+ip);
	    	
	    	try {
				client.getAcsResponse(request);
			} catch (ServerException e) {
				log.error("修改dns解析错误", e);
			} catch (ClientException e) {
				log.error("修改dns解析错误", e);
			}
	    	
		}
	}

    public DescribeDomainRecordsRequest getRequest(){
    	DescribeDomainRecordsRequest request = new DescribeDomainRecordsRequest();
    	request.setProtocol(ProtocolType.HTTPS); // 指定访问协议
		request.setAcceptFormat(FormatType.JSON); // 指定api返回格式
		request.setMethod(MethodType.POST); // 指定请求方法
		request.setRegionId("cn-hangzhou");// 指定要访问的Region,仅对当前请求生效，不改变client的默认设置。
    	return request;
    }
    
    /**
     * 添加DNS解析
     * */
	private void addDNS(String ip) {
		AddDomainRecordRequest request = new AddDomainRecordRequest();
		
    	request.setProtocol(ProtocolType.HTTPS); // 指定访问协议
		request.setAcceptFormat(FormatType.JSON); // 指定api返回格式
		request.setMethod(MethodType.POST); // 指定请求方法
		request.setRegionId("cn-hangzhou");// 指定要访问的Region,仅对当前请求生效，不改变client的默认设置。
		
		request.setActionName("AddDomainRecord");
		
		request.setDomainName(domainName);
		request.setRR(rrKey);
		request.setType(typeKey);
		request.setValue(ip);
		
		try {
			 client.getAcsResponse(request);
		}catch (ClientException e) {
			log.error("添加DNS解析出错", e);
		}
	}

	/**
     * 根据本地ip判断是否需要修改dns解析记录
     * @return ture要改，false不改
     * */
	private boolean checkIpInDns(String ip,List<Record> records ) {
		DescribeDomainRecordsRequest request = getRequest();
		DescribeDomainRecordsResponse response;
		
		request.setActionName("DescribeDomainRecords");
		
		request.setDomainName(domainName);
		request.setTypeKeyWord(typeKey);
		request.setRRKeyWord(rrKey);
		boolean flag = false;//是否修改ip
		
		try {
			response = client.getAcsResponse(request);
			List<Record> list = response.getDomainRecords();
			if(list ==null || list.isEmpty()) {
				flag = true;
				return flag;
			}
			for (Record record : list) {
				if(!ip.equals(record.getValue())) {//如果不等于，则要进行修改
					log.info("需要修改的ip："+record.getValue());
					flag =true;
					records.add(record);
				}
			}
		} catch (ClientException e) {
			log.error("查询DNS解析出错", e);
		}
		return flag;
	}
}