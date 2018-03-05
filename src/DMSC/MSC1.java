package DMSC;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.ac.kaist.mms_client.MMSClientHandler;
import kr.ac.kaist.mms_client.MMSConfiguration;

import net.etri.pkilib.client.ClientPKILibrary;
import net.etri.pkilib.tool.ByteConverter;

import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/* -------------------------------------------------------- */
/** 
File name : SC1.java
	Service Consumer cannot be HTTP server and should poll from MMS. 
Author : Jaehyun Park (jae519@kaist.ac.kr)
	Haeun Kim (hukim@kaist.ac.kr)
	Jaehee Ha (jaehee.ha@kaist.ac.kr)
Creation Date : 2016-12-03

Rev. history : 2017-02-01
Version : 0.3.01
Modifier : Jaehee Ha (jaehee.ha@kaist.ac.kr)

Rev. history : 2017-04-20 
Version : 0.5.0
Modifier : Jaehee Ha (jaehee.ha@kaist.ac.kr)

Rev. history : 2017-04-25
Modifier : Jaehee Ha (jaehee.ha@kaist.ac.kr)

Rev. history : 2017-07-28
Version : 0.5.9
	Changed from PollingResponseCallback.callbackMethod(Map<String,List<String>> headerField, message) 
	     to PollingResponseCallback.callbackMethod(Map<String,List<String>> headerField, List<String> messages) 
Modifier : Jaehee Ha (jaehee.ha@kaist.ac.kr)
*/
/* -------------------------------------------------------- */

public class MSC1 {
	public static void main(String args[]) throws Exception{
		//==========================================
		//============= MRN & PORT =================
		//==========================================

		//===== kilee =====
		//String myMRN = "urn:mrn:smart-navi:device:msc_kilee-20171117";
		
		//===== workstation1(default) =====
		String myMRN = "urn:mrn:smart-navi:device:msc-20171117";
		
		//==========================================
		//============= MMS Server =================
		//==========================================

		//===== Local =====
		//MMSConfiguration.MMS_URL="127.0.0.1:8088";
		
		//===== Kaist(default) =====
		MMSConfiguration.MMS_URL="mms-kaist.com:8088";
		MMSConfiguration.LOGGING = false; //MMSConfiguration.java : public static final boolean LOGGING = true; ==> public static boolean LOGGING = true;
		
		//Service Consumer cannot be HTTP server and should poll from MMS. 
		MMSClientHandler polling = new MMSClientHandler(myMRN);
		
		int pollInterval = 1000;
		String dstMRN = "urn:mrn:smart-navi:device:mms1";
		//String svcMRN = "urn:mrn:smart-navi:device:tm-server";
		String svcMRN = "urn:mrn:smart-navi:device:msp1-20170914";
		
		/*try {*/
			ClientPKILibrary clientPKILib = ClientPKILibrary.getInstance();
			ByteConverter byteConverter = ByteConverter.getInstance();

			byte[] content = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
			
			//===== old =====
			//String privateKeyPath = "PrivateKey.pem";
			//String certPath = "Certificate.pem";
			
			//byte[] signedData = clientPKILib.generateSignedData(content, privateKeyPath, certPath);
			//String hexSignedData = byteConverter.byteArrToHexString(signedData);
			
			//===== active =====
			String privateKeyPath_active = "PrivateKey_POUL_LOWENORN_active.pem";
			String certPath_active = "Certificate_POUL_LOWENORN_active.pem";
			
			byte[] signedData_active = clientPKILib.generateSignedData(content, privateKeyPath_active, certPath_active);
			String hexSignedData_active = byteConverter.byteArrToHexString(signedData_active);
			
			//===== revoked =====
			String privateKeyPath_revoked = "PrivateKey_POUL_LOWENORN_revoked.pem";
			String certPath_revoked = "Certificate_POUL_LOWENORN_revoked.pem";
			
			byte[] signedData_revoked = clientPKILib.generateSignedData(content, privateKeyPath_revoked, certPath_revoked);
			String hexSignedData_revoked = byteConverter.byteArrToHexString(signedData_revoked);
		/*}
		catch (Exception e)
		{
			e.printStackTrace();
		}*/
		
		polling.startPolling(dstMRN, svcMRN, pollInterval, new MMSClientHandler.PollingResponseCallback() {
			//Response Callback from the polling message
			//it is called when client receives a message
			@Override
			public void callbackMethod(Map<String, List<String>> headerField, List<String> messages) {
				// TODO Auto-generated method stub
				for (String s : messages) {
					System.out.print(s);
				}
			}
		});
		
		//Service Consumer which can only send message
		MMSClientHandler sender = new MMSClientHandler(myMRN);
		
		//Service Consumer is able to set he's HTTP header field : default(active)
		Map<String, List<String>> headerfield = new HashMap<String, List<String>>();
		
		List<String> valueList = new ArrayList<String>();
		valueList.add("1234567890");
		headerfield.put("AccessToken",valueList);
		
		List<String> valueList1 = new ArrayList<String>();
		valueList1.add(hexSignedData_active); //===========
		headerfield.put("HexSignedData",valueList1);
		
		sender.setMsgHeader(headerfield);
		
		sender.setSender(new MMSClientHandler.ResponseCallback (){
			//Response Callback from the request message
			@Override
			public void callbackMethod(Map<String, List<String>> headerField, String message) {
				// TODO Auto-generated method stub
				/*
				Iterator<String> hiter = headerField.keySet().iterator();
				while (hiter.hasNext()){
					String key = hiter.next();
					System.out.println(key+":"+headerField.get(key).toString());
				}
				*/		        
				int serviceType = 0;
				Iterator<String> hiter = headerField.keySet().iterator();
				while (hiter.hasNext()){
					String key = hiter.next();
					if(key == null)
						continue;
					
					if(key.equals("Servicetype"))
					{
						String TypeStr = headerField.get(key).toString();
						if(TypeStr.equals("[LookupService]") 
							|| TypeStr.equals("[GetAllInstanceById]") 
							|| TypeStr.equals("[SearchInstanceByLocation]"))
							serviceType = 1;
						else if(TypeStr.equals("[RequestService]"))
							serviceType = 2;
						else if(TypeStr.equals("[GetInstance]"))
							serviceType = 3;
						break;
					}
				}
				
				MessageController messageController = new MessageController();
				messageController.PrintMessage(message, serviceType);
			}			
		});
		
		Scanner scan = new Scanner(System.in);
		
		while(true){
			System.out.println();
			System.out.println("========== Dummy Client ==========");
			System.out.println("0. Exit");
			System.out.println("1. Lookup Service");
			System.out.println("2. Get Instance");
			System.out.println("3. Get All Instance by ID");
			System.out.println("4. Search Instance by Location");
			System.out.println("5. Submit Certificate(active)");
			System.out.println("6. Submit Certificate(revoked)");
			System.out.println("7. Select Service");
			System.out.println("8. Request Service");
			System.out.println("==================================");
			System.out.print("Input Menu#: ");
			
			int answer = scan.nextInt();
			System.out.println();
			
			switch(answer){
				case 0: //Exit
					System.exit(0);
					break;
				case 1: //Lookup Service 
					System.out.println("<<Lookup Service>>");
					//sender.sendPostMsg("urn:mrn:smart-navi:device:msr2-20170914", "LookupService"); //===== KILee
					sender.sendPostMsg("urn:mrn:smart-navi:device:dummy-msr1", "LookupService"); //===== Workstation1
					//sender.sendPostMsg("urn:mrn:smart-navi:device:msr4-20171117", "LookupService"); //===== Workstation2
					//sender.sendPostMsg("urn:mrn:smart-navi:device:msr5-20171117", "LookupService"); //===== Server
					break;
				case 2: //Get Instance
					System.out.println("<<Get Instance>>");
					System.out.print("Input Instance ID : ");
					String instanceId = scan.next();
					System.out.print("Input Instance Version : ");
					String instanceVersion = scan.next();
					System.out.println("");
					sender.sendPostMsg("urn:mrn:smart-navi:device:dummy-msr1", "GetInstance#:" + instanceId + "#:" + instanceVersion);
					break;
				case 3: //Get All Instance by ID
					System.out.println("<<Get All Instance by ID>>");
					System.out.print("Input Instance ID : ");
					String instanceId2 = scan.next();
					System.out.println("");
					sender.sendPostMsg("urn:mrn:smart-navi:device:dummy-msr1", "GetAllInstanceById#:" + instanceId2);
					break;
				case 4: //Search Instance by Location
					System.out.println("<<Get All Instance by ID>>");
					System.out.print("Input Latitude : ");
					String latitude = scan.next();
					System.out.print("Input Longitude : ");
					String Longitude = scan.next();
					System.out.println("");
					sender.sendPostMsg("urn:mrn:smart-navi:device:dummy-msr1", "SearchInstanceByLocation#:" + latitude + "#:" + Longitude);
					break;
				case 5: //Submit Certificate(active)
					headerfield.clear();
					
					valueList.clear();
					valueList.add("1234567890");
					headerfield.put("AccessToken",valueList);
					
					valueList1.clear();
					valueList1.add(hexSignedData_active); //=======
					headerfield.put("HexSignedData",valueList1);
					
					sender.setMsgHeader(headerfield);
					
					System.out.println("<<Submit Certificate(active)>>");
					//sender.sendPostMsg("urn:mrn:smart-navi:device:msp2-20171117", "/forwarding", "VerifySignedData"); //===== KILee
					//sender.sendPostMsg("urn:mrn:smart-navi:device:msp3-20171117", "/forwarding", "VerifySignedData"); //===== Workstation1
					sender.sendPostMsg("urn:mrn:smart-navi:device:msp4-20171117", "/forwarding", "VerifySignedData"); //===== Workstation2
					//sender.sendPostMsg("urn:mrn:smart-navi:device:msp5-20171117", "/forwarding", "VerifySignedData"); //===== Server
					break;
				case 6: //Submit Certificate(revoked)
					headerfield.clear();
					
					valueList.clear();
					valueList.add("1234567890");
					headerfield.put("AccessToken",valueList);
					
					valueList1.clear();
					valueList1.add(hexSignedData_revoked); //========
					headerfield.put("HexSignedData",valueList1);
					
					sender.setMsgHeader(headerfield);
					
					System.out.println("<<Submit Certificate(revoked)>>");
					//sender.sendPostMsg("urn:mrn:smart-navi:device:msp2-20171117", "/forwarding", "VerifySignedData"); //===== KILee
					//sender.sendPostMsg("urn:mrn:smart-navi:device:msp3-20171117", "/forwarding", "VerifySignedData"); //===== Workstation1
					sender.sendPostMsg("urn:mrn:smart-navi:device:msp4-20171117", "/forwarding", "VerifySignedData"); //===== Workstation2
					//sender.sendPostMsg("urn:mrn:smart-navi:device:msp5-20171117", "/forwarding", "VerifySignedData"); //===== Server
					break;
				case 7: //Select Service 
					System.out.println("<<Select Service>>");
					//sender.sendPostMsg("urn:mrn:smart-navi:device:msp3-20171117", "/forwarding", "SelectService");
					break;
				case 8: //Request Service
					System.out.println("<<Request Service>>");
					//sender.sendPostMsg("urn:mrn:smart-navi:device:msp2-20171117", "/forwarding", "RequestService"); //===== KILee
					//sender.sendPostMsg("urn:mrn:smart-navi:device:msp3-20171117", "/forwarding", "RequestService"); //===== Workstation1
					sender.sendPostMsg("urn:mrn:smart-navi:device:dummy-msp1", "/forwarding", "RequestService"); //===== Workstation2
					//sender.sendPostMsg("urn:mrn:smart-navi:device:msp5-20171117", "/forwarding", "RequestService"); //===== Server
					break;
			}
		}
		
		//===== verifySignedData =====
		//sender.sendPostMsg("urn:mrn:smart-navi:device:msp1-20170914", "/forwarding", "VerifySignedData");
		
		/*
		for (int i = 0; i < 2;i++){
			sender.sendPostMsg("urn:mrn:smart-navi:device:msp1-20170914", "/forwarding", "안녕 hi \"hello\" " + i);
			//Thread.sleep(100);
		}
		*/
		
		/*
		try {
			ClientPKILibrary clientPKILib = ClientPKILibrary.getInstance();
			ServerPKILibrary serverPKILib = ServerPKILibrary.getInstance();
			
			ByteConverter byteConverter = ByteConverter.getInstance();
			
			byte[] content = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
			
			String privateKeyPath = "PrivateKey.pem";
			String certPath = "Certificate.pem";
			
			byte[] signedData = clientPKILib.generateSignedData(content, privateKeyPath, certPath);
			String hexSignedData = byteConverter.byteArrToHexString(signedData);
			
			sender.sendPostMsg("urn:mrn:smart-navi:device:msp1-20170914", "/forwarding", hexSignedData);
			
			System.out.println();
	        System.out.println( "============== Client-side ==============" );
	        System.out.println("Content: " + byteConverter.byteArrToHexString( content ) );
	        System.out.println("SignedData: " + hexSignedData);
	        
	        System.out.println();
	        System.out.println( "============== Server-side ==============" );
	        System.out.println("SubjectMRN: " + serverPKILib.getSubjectMRN(signedData));
	        System.out.println("Verifying: " + serverPKILib.verifySignedData(signedData));
	        System.out.println("(Extract) Content: " + byteConverter.byteArrToHexString(serverPKILib.extractContent(signedData)));
	
	        System.out.println();
	        System.out.println( "============== Additional Test ==============" );
	        signedData[signedData.length - 10] = 0x02; // 임의로 바꿀 때는 넉넉히 앞에를 바꿔줘야된다. 마지막에 붙는 것은 Padding이라, 맨 마지막만 바꾸는거는 부족함.
	        System.out.println("Verifying after Modify(Should be False): " + serverPKILib.verifySignedData(signedData));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		*/
        
	}
}
