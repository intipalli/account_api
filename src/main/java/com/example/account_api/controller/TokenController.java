package com.example.account_api.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account_api.model.Customer;
import com.example.account_api.model.Token;
import com.example.account_api.utils.JSONHelper;
import com.example.account_api.utils.JWTFactory;

@RestController
@RequestMapping("/token")
public class TokenController {

    public static Token userToken;

    // @GetMapping
    // public String getFakeToken() {
    //     return "jwt-fake-token-asdfasdfasfa".toString();
    // }

	@PostMapping
	public ResponseEntity<?> createTokenForCustomer(@RequestBody Customer customer) {
		String email = customer.getEmail();
		String password = customer.getPassword();
	
		// Validate email and password
		if (email == null || email.trim().isEmpty()) {
			return ResponseEntity.badRequest().body("Email cannot be null or empty.");
		}
		if (password == null || password.trim().isEmpty()) {
			return ResponseEntity.badRequest().body("Password cannot be null or empty.");
		}
	
		// Check credentials and create token if valid
		if (checkCredentials(email, password)) {
			Token token = createToken(email);
			return ResponseEntity.ok().body(token);
		}
	
		// Return unauthorized status if credentials are invalid
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
	}
	
    private boolean checkCredentials(String username, String password) {
        if (username.equals("ApiClientApp") && password.equals("secret")) {
            return true;
        }
        Customer cust = getCustomerByName(username);
        if (cust != null && cust.getEmail().equals(username) && cust.getPassword().equals(password)) {
            return true;
        }
        return false;
    }

    public static Token getUserToken() {
        if (userToken == null || userToken.getToken() == null || userToken.getToken().length() == 0) {
            userToken = createToken("ApiClientApp");
        }
        return userToken;
    }

    private static Token createToken(String username) {
        return new Token(JWTFactory.createToken(username));
    }

    public static Customer getCustomerByName(String username) {
        try {
            URL url = new URL("http://localhost:8080/api/customers/byname/" + username);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("authorization", "Bearer " + getUserToken().getToken());

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                String output = "";
                String line;
                while ((line = br.readLine()) != null) {
                    output = output + line;
                }
                conn.disconnect();
                return JSONHelper.jsonToJava(output);
            } else {
                conn.disconnect();
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
