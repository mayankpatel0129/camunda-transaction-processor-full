package com.example.transactionprocessor.controller;

import com.example.transactionprocessor.model.TransactionRequest;
import com.example.transactionprocessor.model.PaymentTransactionRequest;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private RuntimeService runtimeService;

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processTransaction(@RequestBody TransactionRequest transactionRequest) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("transactionType", transactionRequest.getTransactionType());
            variables.put("amount", transactionRequest.getAmount());
            variables.put("currency", transactionRequest.getCurrency());
            variables.put("transactionDateTime", transactionRequest.getTransactionDateTime());
            variables.put("creditCardNumber", transactionRequest.getCreditCardInfo().getMaskedCardNumber());
            variables.put("cardHolderName", transactionRequest.getCreditCardInfo().getHolderName());
            variables.put("cardType", transactionRequest.getCreditCardInfo().getCardType());
            variables.put("billingAddress", transactionRequest.getBillingAddress().toString());
            variables.put("vendorName", transactionRequest.getVendorInfo().getName());
            variables.put("vendorLocation", transactionRequest.getVendorInfo().getLocation());
            variables.put("description", transactionRequest.getDescription());
            variables.put("referenceNumber", transactionRequest.getReferenceNumber());
            
            String processInstanceId = UUID.randomUUID().toString();
            variables.put("processInstanceId", processInstanceId);

            System.out.println("Processing transaction: " + transactionRequest);
            
            runtimeService.startProcessInstanceByKey("transactionProcessing", processInstanceId, variables);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Transaction process started successfully");
            response.put("processInstanceId", processInstanceId);
            response.put("transactionType", transactionRequest.getTransactionType());
            response.put("amount", transactionRequest.getAmount());
            response.put("referenceNumber", transactionRequest.getReferenceNumber());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error processing transaction: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error processing transaction: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/payment")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody PaymentTransactionRequest paymentRequest) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("transactionType", paymentRequest.getTransactionType());
            variables.put("paymentAmount", paymentRequest.getPaymentAmount());
            variables.put("currency", paymentRequest.getCurrency());
            variables.put("paymentDate", paymentRequest.getPaymentDate());
            variables.put("scheduledDateTime", paymentRequest.getScheduledDateTime());
            
            // Customer Account Information
            variables.put("customerAccountNumber", paymentRequest.getCustomerAccount().getAccountNumber());
            variables.put("customerName", paymentRequest.getCustomerAccount().getCustomerName());
            variables.put("customerEmail", paymentRequest.getCustomerAccount().getCustomerEmail());
            variables.put("customerPhone", paymentRequest.getCustomerAccount().getCustomerPhone());
            variables.put("accountType", paymentRequest.getCustomerAccount().getAccountType());
            variables.put("currentBalance", paymentRequest.getCustomerAccount().getCurrentBalance());
            variables.put("creditLimit", paymentRequest.getCustomerAccount().getCreditLimit());
            variables.put("minimumPaymentDue", paymentRequest.getCustomerAccount().getMinimumPaymentDue());
            variables.put("paymentDueDate", paymentRequest.getCustomerAccount().getPaymentDueDate());
            variables.put("accountStatus", paymentRequest.getCustomerAccount().getAccountStatus());
            
            // Payment Method Information
            variables.put("paymentMethodType", paymentRequest.getPaymentMethod().getPaymentType());
            variables.put("paymentMethodId", paymentRequest.getPaymentMethod().getPaymentMethodId());
            variables.put("paymentMethodNickname", paymentRequest.getPaymentMethod().getNickname());
            variables.put("isDefaultPaymentMethod", paymentRequest.getPaymentMethod().isDefault());
            
            // Bank Details
            variables.put("bankName", paymentRequest.getPaymentMethod().getBankDetails().getBankName());
            variables.put("routingNumber", paymentRequest.getPaymentMethod().getBankDetails().getRoutingNumber());
            variables.put("bankAccountNumber", paymentRequest.getPaymentMethod().getBankDetails().getMaskedAccountNumber());
            variables.put("bankAccountHolderName", paymentRequest.getPaymentMethod().getBankDetails().getAccountHolderName());
            variables.put("bankAccountType", paymentRequest.getPaymentMethod().getBankDetails().getAccountType());
            
            // Payment Details
            variables.put("paymentType", paymentRequest.getPaymentType());
            variables.put("isRecurring", paymentRequest.isRecurring());
            variables.put("recurringFrequency", paymentRequest.getRecurringFrequency());
            variables.put("paymentReference", paymentRequest.getPaymentReference());
            variables.put("memo", paymentRequest.getMemo());
            variables.put("confirmationEmail", paymentRequest.isConfirmationEmail());
            
            String processInstanceId = UUID.randomUUID().toString();
            variables.put("processInstanceId", processInstanceId);

            System.out.println("Processing payment: " + paymentRequest);
            
            runtimeService.startProcessInstanceByKey("transactionProcessing", processInstanceId, variables);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Payment process started successfully");
            response.put("processInstanceId", processInstanceId);
            response.put("transactionType", paymentRequest.getTransactionType());
            response.put("paymentAmount", paymentRequest.getPaymentAmount());
            response.put("paymentReference", paymentRequest.getPaymentReference());
            response.put("customerAccount", paymentRequest.getCustomerAccount().getMaskedAccountNumber());
            response.put("paymentMethod", paymentRequest.getPaymentMethod().getNickname());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error processing payment: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/start")
    public String startTransaction(@RequestParam String transactionType) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("transactionType", transactionType);

            System.out.println("Starting process for transaction type: " + transactionType);
            
            runtimeService.startProcessInstanceByKey("transactionProcessing", variables);

            return "Transaction process started for type: " + transactionType;
        } catch (Exception e) {
            System.err.println("Error starting process: " + e.getMessage());
            e.printStackTrace();
            return "Error starting transaction: " + e.getMessage();
        }
    }
}
