package com.example.account_api.Utils;

import org.json.JSONObject;
import com.example.account_api.model.Customer;

public class JSONHelper {

    public static Customer jsonToJava(String json){
        
        JSONObject obj = new JSONObject(json);

        Customer cust = new Customer();
        cust.setName((String) obj.get("name"));
        cust.setId((int) obj.get("id"));
        cust.setEmail((String) obj.get("email"));
        cust.setPassword((String) obj.get("password"));
        return cust;
    }
    
    public static String javaToJson(Customer customer){
        JSONObject jo = new JSONObject();

        jo.put("id", customer.getId());
        jo.put("name", customer.getName());
        jo.put("email", customer.getEmail());
        jo.put("password", customer.getPassword());

        return jo.toString();
    }
}
