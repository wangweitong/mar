package com.mar.broadcast.alarm;

import com.google.gson.Gson;
import com.mar.store.UserManager;
import com.mar.store.mysql.MysqlLogManager;
import com.mar.store.mysql.persistence.ZeusUser;
import com.mar.util.Environment;
import com.mar.util.JsonUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SMSAlarm extends AbstractZeusAlarm{
	private static Logger log=LogManager.getLogger(SMSAlarm.class);
	@Autowired
	private UserManager userManager;
	@Autowired
	private MysqlLogManager zeusLogManager;
    private static String notifyUrl = Environment.getNotifyUrl();//Noc服务器
	private static String accessToken = Environment.getAccessToken();//Noc access_token

	@Override
	public void alarm(String jobId, List<String> uids, String title, String content)
			throws Exception {
		String srcId = "BI-Zeus调度系统";
		String devId = InetAddress.getLocalHost().getHostName();
		String itemId = "宙斯报警";
		String level = "high";
		String message = title;
		List<ZeusUser> userList = userManager.findListByUidByOrder(uids);
		if(userList != null && userList.size()>0){
			for(ZeusUser user : userList){
				message += "<br>负责人："+user.getName()+" 电话："+user.getPhone()+" 邮箱："+user.getEmail();
			}
		}
		message += "<br>" + content;
		sendNOCAlarm(jobId, notifyUrl, accessToken, srcId, devId, itemId, level, message);
		/*try{
			LogDescriptor logDescriptor = new LogDescriptor();
			logDescriptor.setLogType("noc");
			logDescriptor.setIp(InetAddress.getLocalHost().getHostAddress());
			logDescriptor.setUserName("zeus");
			logDescriptor.setUrl(jobId);
			logDescriptor.setRpc(srcId);
			logDescriptor.setDelegate(devId);
			logDescriptor.setMethod(level);
//			logDescriptor.setDescription((message.length()>4000 ? message.substring(4000) : message));
			logDescriptor.setDescription("");
			zeusLogManager.addLog(logDescriptor);
		}catch(Exception ex){
			log.error(ex.toString());
		}*/
	}

	@SuppressWarnings("deprecation")
	public void sendNOCAlarm(String jobId, String sendUrl, String accessToken, String srcId, String devId, String itemId, String level, String message) {
		log.info("jobId: " + jobId + " begin to send the noc, the srcId is " + srcId + ", the devId is " + devId + ", the itemId is " + itemId + ". the message is " + message + ". the sendUrl is " + sendUrl);
        HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(sendUrl);
		Gson gson = new Gson();
		method.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		try {
			Map<String, String> bodyMap = new HashMap<String, String>();
			bodyMap.put("access_token", accessToken);
			String requestBody = getRequestBody(srcId, devId, itemId, level, message);
			bodyMap.put("request_body", "njson=" + requestBody);
			method.setRequestBody(JsonUtil.map2json(bodyMap).toString());
			int code = client.executeMethod(method);
			log.info("jobId: " + jobId + " the return code is " + HttpStatus.SC_OK);
			String responseBodyAsString = method.getResponseBodyAsString(2000);
			log.info("jobId: " + jobId + " the response body is " + responseBodyAsString);
			ResponseJson rJ = null;
			if (responseBodyAsString != null) {
				rJ = gson.fromJson(responseBodyAsString, ResponseJson.class);
			}
			if (code !=  HttpStatus.SC_OK || rJ == null || !rJ.isSuccess()) {
				log.error("jobId: " + jobId + " send noc failed, code: " + code);
				return;
			}
			log.info("jobId: " + jobId + " send noc successfully!");
		} catch(HttpException  e) {
			log.error("jobId: " + jobId +" send noc fail,", e);
		} catch (IOException e) {
			log.error("jobId: " + jobId + " send noc fail,", e);
		} catch (Exception e) {
			log.error("jobId: " + jobId + " send noc fail,", e);
		}
   }
   
	private String getRequestBody(String srcId, String devId, String itemId, String level, String message) throws UnsupportedEncodingException {
		Map<String, String> param = new HashMap<String, String>();
		param.put("src_id",  srcId);
		param.put("dev_id",  devId);
		param.put("item_id", itemId);
		param.put("level",   level);
		param.put("message", message);
		param.put("fields",  "");
//		System.out.println(JsonUtil.map2json(param).toString());
		return URLEncoder.encode(JsonUtil.map2json(param).toString(), "utf-8");
	}
	
	class ResponseJson{

		private String message;
		private String data;
		private boolean success;
		private int error;
		
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public String getData() {
			return data;
		}
		public void setData(String data) {
			this.data = data;
		}
		public boolean isSuccess() {
			return success;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public int getError() {
			return error;
		}
		public void setError(int error) {
			this.error = error;
		}

		@Override
		public String toString() {
			return "ReturnJson [message=" + message + ", data=" + data
					+ ", success=" + success + ", error=" + error + "]";
		}
	}
	
	public static void main(String[] args) {
		String returnString = "{\"message\": \"\", \"data\": \"enqueue\", \"success\": true, \"error\": 0}";
//		String returnString = null;
		Gson gson = new Gson();
		ResponseJson rJson = gson.fromJson(returnString, ResponseJson.class);
		System.out.println("the message is " + rJson.getMessage());
		System.out.println("the data is " + rJson.getData());
		System.out.println("the error is " + rJson.getError());
		System.out.println("the success is " + rJson.isSuccess());

	}
	
	

}