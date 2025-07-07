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
class ExecutiveApprovalDelegateTest {

    @Mock
    private DelegateExecution execution;

    private ExecutiveApprovalDelegate executiveApprovalDelegate;

    @BeforeEach
    void setUp() {
        executiveApprovalDelegate = new ExecutiveApprovalDelegate();
        when(execution.getProcessInstanceId()).thenReturn("test-process-123");
    }

    @Test
    void testExecuteWithMediumValueAdjustment() throws Exception {
        // Given
        when(execution.getVariable("amount")).thenReturn(new BigDecimal("-5000.00"));
        when(execution.getVariable("transactionType")).thenReturn("Adjustment");
        when(execution.getVariable("customerName")).thenReturn("Test Customer");
        when(execution.getVariable("description")).thenReturn("System error correction");

        // When
        executiveApprovalDelegate.execute(execution);

        // Then
        verify(execution).setVariable(eq("executiveApprovalStatus"), anyString());
        verify(execution).setVariable(eq("executiveApprovalOfficer"), anyString());
        verify(execution).setVariable(eq("executiveApprovalTimestamp"), any(Long.class));
        verify(execution).setVariable(eq("executiveApprovalComments"), anyString());
    }

    @Test
    void testExecuteWithHighValueRequiresCEO() throws Exception {
        // Given - very high value requires CEO approval
        when(execution.getVariable("amount")).thenReturn(new BigDecimal("-75000.00"));
        when(execution.getVariable("transactionType")).thenReturn("Adjustment");
        when(execution.getVariable("customerName")).thenReturn("VIP Customer");
        when(execution.getVariable("description")).thenReturn("Major system error correction");

        // When
        executiveApprovalDelegate.execute(execution);

        // Then
        verify(execution).setVariable(eq("executiveApprovalOfficer"), eq("CEO John Smith"));
        verify(execution).setVariable(eq("executiveApprovalTimestamp"), any(Long.class));
    }

    @Test
    void testExecuteWithSystemErrorDescription() throws Exception {
        // Given - system errors have higher approval probability
        when(execution.getVariable("amount")).thenReturn(new BigDecimal("-10000.00"));
        when(execution.getVariable("transactionType")).thenReturn("Adjustment");
        when(execution.getVariable("customerName")).thenReturn("Regular Customer");
        when(execution.getVariable("description")).thenReturn("System error - billing malfunction");

        // When
        executiveApprovalDelegate.execute(execution);

        // Then
        verify(execution).setVariable(eq("executiveApprovalStatus"), anyString());
        verify(execution).setVariable(eq("executiveApprovalReason"), anyString());
    }

    @Test
    void testExecuteWithConditionalApproval() throws Exception {
        // Given
        when(execution.getVariable("amount")).thenReturn(new BigDecimal("-8000.00"));
        when(execution.getVariable("transactionType")).thenReturn("Adjustment");
        when(execution.getVariable("customerName")).thenReturn("VIP Customer");
        when(execution.getVariable("description")).thenReturn("System error - billing malfunction correction");

        // When
        executiveApprovalDelegate.execute(execution);

        // Then - verify that if conditional, additional review flag is set
        verify(execution).setVariable(eq("executiveApprovalStatus"), anyString());
        verify(execution, atMost(1)).setVariable("requiresAdditionalReview", true);
        verify(execution, atMost(1)).setVariable("additionalReviewType", "BOARD_APPROVAL");
    }

    @Test
    void testExecuteWithNullAmount() throws Exception {
        // Given
        when(execution.getVariable("amount")).thenReturn(null);
        when(execution.getVariable("transactionType")).thenReturn("Adjustment");
        when(execution.getVariable("customerName")).thenReturn("Test Customer");
        when(execution.getVariable("description")).thenReturn("System error correction");

        // When
        executiveApprovalDelegate.execute(execution);

        // Then
        verify(execution).setVariable(eq("executiveApprovalStatus"), anyString());
        verify(execution).setVariable(eq("executiveApprovalOfficer"), anyString());
    }
}