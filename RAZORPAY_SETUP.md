# Razorpay Setup Documentation

## Introduction
Razorpay is a leading payment gateway that allows businesses to accept, process, and disburse payments with its product suite. This documentation outlines the steps necessary to integrate Razorpay into your Spring Boot application.

## Prerequisites
- Java Development Kit (JDK) 8 or higher
- Maven 3.5 or higher
- Spring Boot 2.0 or higher
- Razorpay account (sign up at https://razorpay.com)

## Step 1: Add Razorpay Dependency
Add the following dependency in your `pom.xml`:
```xml
<dependency>
    <groupId>com.razorpay</groupId>
    <artifactId>razorpay-java-client</artifactId>
    <version>1.3.1</version>
</dependency>
```

## Step 2: Configuration
Create an application properties file under `src/main/resources/application.properties` and add your Razorpay API key and secret:
```properties
razorpay.keyId=YOUR_KEY_ID
razorpay.keySecret=YOUR_KEY_SECRET
```

## Step 3: Create Razorpay Service
Create a service class to handle payment operations:
```java
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {
    @Value("${razorpay.keyId}")
    private String keyId;

    @Value("${razorpay.keySecret}")
    private String keySecret;

    private RazorpayClient client;

    public RazorpayService() throws Exception {
        client = new RazorpayClient(keyId, keySecret);
    }

    public Order createOrder(int amount) throws Exception {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "receipt#1");
        return client.orders.create(orderRequest);
    }
}
```

## Step 4: Create Controller
Create a controller class to handle HTTP requests:
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private RazorpayService razorpayService;

    @PostMapping("/createOrder")
    public ResponseEntity<Order> createOrder(@RequestParam int amount) throws Exception {
        Order order = razorpayService.createOrder(amount);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }
}
```

## Step 5: Frontend Integration
Integrate Razorpay's checkout option in your frontend:
```html
<html>
<head>
    <script src="https://checkout.razorpay.com/v1/checkout.js"></script>
</head>
<body>
    <button id="rzp-button1">Pay with Razorpay</button>
    <script>
        var options = {
            "key": "YOUR_KEY_ID", // Enter the Key ID generated from the Razorpay Dashboard
            "amount": "50000", // Amount is in currency subunits. Default currency is INR. Hence 50000 refers to 50000 paise or ₹500
            "currency": "INR",
            "name": "Acme Corp",
            "description": "Test Transaction",
            "image": "https://example.com/your_logo",
            "order_id": "order_9A3q2a7fZ7Zc1k", // Receive the order_id created in the previous step
            "handler": function (response){
                alert(response.razorpay_payment_id);
                alert(response.razorpay_order_id);
                alert(response.razorpay_signature);
            },
            "prefill": {
                "name": "Gaurav Kumar",
                "email": "gaurav.kumar@example.com",
                "contact": "9999999999"
            },
            "notes": {
                "address": "Razorpay Corporate Office"
            },
            "theme": {
                "color": "#F37254"
            }
        };
        var rzp1 = new Razorpay(options);
        document.getElementById('rzp-button1').onclick = function(e) {
            rzp1.open();
            e.preventDefault();
        }
    </script>
</body>
</html>
```

## Conclusion
You have successfully integrated Razorpay into your Spring Boot application. For further details, refer to the [Razorpay Documentation](https://razorpay.com/docs/api/).