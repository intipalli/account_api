package com.example.account_api.controller;

import java.net.URI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.account_api.model.Customer;
import com.example.account_api.model.TokenController;
import com.example.account_api.utils.JSONHelper;
import com.example.account_api.model.Token;
import com.example.account_api.utils.JSONHelper;
import com.example.account_api.controller.TokenController;

@RestController
@RequestMapping("/register")
public class RegisterController {

	@GetMapping
	public String healthCheck() {
		return "<h3>The Authentication service is up and running!</h3>";
	}

	@PostMapping
	public ResponseEntity<?> addCustomer(@RequestBody Customer newCustomer, UriComponentsBuilder uriBuilder) {
		if (newCustomer.getId() != 0) {
			return ResponseEntity.badRequest().body("Customer ID should not be provided when creating a new customer.");
		}
		if (newCustomer.getName() == null || newCustomer.getName().trim().isEmpty()) {
			return ResponseEntity.badRequest().body("Customer name cannot be null or empty.");
		}
		if (newCustomer.getEmail() == null || newCustomer.getEmail().trim().isEmpty()) {
			return ResponseEntity.badRequest().body("Customer email cannot be null or empty.");
		}

		Customer existingCustomer = TokenController.getCustomerByName(newCustomer.getEmail());
		if (existingCustomer != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("A customer with the same email already exists.");
		}

		postNewCustomer(JSONHelper.javaToJson(newCustomer));

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(newCustomer.getId())
				.toUri();

		return ResponseEntity.created(location).build();
	}

    private void postNewCustomer(String json_string) {
		try {

			URL url = new URL("http://localhost:8080/api/customers");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			Token token = TokenController.getUserToken();
			conn.setRequestProperty("authorization", "Bearer " + token.getToken());
			// conn.setRequestProperty("tokencheck", "false");

			OutputStream os = conn.getOutputStream();
			os.write(json_string.getBytes());
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

	}
}