package DMSC;

import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MessageController
{
	public void PrintMessage(String message, int serviceType)
	{
		if(serviceType == 1 || serviceType == 2)
		{
			try {
				int i = 0;
				Iterator<JSONObject> iterator = null;
				if(serviceType == 1){
					JSONParser Jpar = new JSONParser();
			        JSONArray Jdata = (JSONArray) Jpar.parse(message);
					iterator = Jdata.iterator();
				}
				else if(serviceType == 2){
					JSONParser Jpar = new JSONParser();
					JSONObject Jobj = (JSONObject) Jpar.parse(message);
					
					System.out.println("meta : " + Jobj.get("meta"));
					
			        JSONArray Jdata = (JSONArray) Jobj.get("data");
					iterator = Jdata.iterator();
				}
				
				serviceType = 0;
				
				while(iterator.hasNext()){
					JSONObject Jobj1 = iterator.next();
					
					++i;
					System.out.println();
					System.out.println("===== " + i + " =====");
					//System.out.println(i + "======" + Jobj1.toString());
					
					Iterator<String> iter = Jobj1.keySet().iterator();
					while (iter.hasNext()){
						String key = iter.next();
						Object val = Jobj1.get(key);
						
						if(val == null)
							System.out.println(key+":"+ "null");
						else
							System.out.println(key+":"+Jobj1.get(key).toString());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if(serviceType == 3)
		{
			try
			{
				serviceType = 0;
				JSONParser Jpar = new JSONParser();
				JSONObject Jobj = (JSONObject) Jpar.parse(message);
				Iterator<String> iter = Jobj.keySet().iterator();
				while (iter.hasNext()){
					String key = iter.next();
					Object val = Jobj.get(key);
					
					if(val == null)
						System.out.println(key+":"+ "null");
					else
						System.out.println(key+":"+Jobj.get(key).toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Response: " + message);
		}
	}
}
