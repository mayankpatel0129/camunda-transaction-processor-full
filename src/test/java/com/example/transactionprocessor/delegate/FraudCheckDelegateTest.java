package com.example.transactionprocessor.delegate;

import com.example.transactionprocessor.exception.BusinessRuleException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudCheckDelegateTest {

    @Mock
    private DelegateExecution execution;

    private FraudCheckDelegate fraudCheckDelegate;

    @BeforeEach
    void setUp() {
        fraudCheckDelegate = new FraudCheckDelegate();
        when(execution.getProcessInstanceId()).thenReturn("test-process-123");
    }

    @Test
    void testExecuteWithNormalTransaction() throws Exception {
        // Given
        when(execution.getVariable("amount")).thenReturn(new BigDecimal("1000.00"));
        when(execution.getVariable("cardNumber")).thenReturn("4111111111111111");
        when(execution.getVariable("vendorLocation")).thenReturn("New York, NY");

        // When
        fraudCheckDelegate.execute(execution);

        // Then
        verify(execution).setVariable("fraudCheckStatus", "PASSED");
        verify(execution).setVariable(eq("riskScore"), any(Integer.class));
        verify(execution).setVariable(eq("fraudCheckTimestamp"), any(Long.class));
    }

    @Test
    void testExecuteWithFraudAmount() {
        // Given - exactly $15,000 triggers fraud detection
        when(execution.getVariable("amount")).thenReturn(new BigDecimal("15000.00"));
        when(execution.getVariable("cardNumber")).thenReturn("4111111111111111");
        when(execution.getVariable("vendorLocation")).thenReturn("Online");

        // When & Then
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, 
            () -> fraudCheckDelegate.execute(execution));
        
        assertEquals("FRAUD_DETECTED", exception.getBusinessCode());
        assertTrue(exception.getBusinessReason().contains("fraud detection"));
    }

    @Test
    void testExecuteWithHighRiskLocation() throws Exception {
        // Given
        when(execution.getVariable("amount")).thenReturn(new BigDecimal("500.00"));
        when(execution.getVariable("cardNumber")).thenReturn("4111111111111111");
        when(execution.getVariable("vendorLocation")).thenReturn("High Risk Location");

        // When
        fraudCheckDelegate.execute(execution);

        // Then - may or may not trigger fraud depending on random factor, but should complete
        verify(execution).setVariable(eq("fraudCheckTimestamp"), any(Long.class));
    }

    @Test
    void testExecuteWithNullAmount() throws Exception {
        // Given
        when(execution.getVariable("amount")).thenReturn(null);
        when(execution.getVariable("cardNumber")).thenReturn("4111111111111111");
        when(execution.getVariable("vendorLocation")).thenReturn("Online");

        // When
        fraudCheckDelegate.execute(execution);

        // Then
        verify(execution).setVariable("fraudCheckStatus", "PASSED");
        verify(execution).setVariable(eq("riskScore"), any(Integer.class));
    }
}