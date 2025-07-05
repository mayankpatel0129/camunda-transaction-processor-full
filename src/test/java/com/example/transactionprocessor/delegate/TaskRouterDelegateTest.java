package com.example.transactionprocessor.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TaskRouterDelegateTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private TaskRouterDelegate taskRouterDelegate;

    @BeforeEach
    void setUp() {
        // Use lenient stubbing to avoid unnecessary stubbing exceptions
        lenient().when(applicationContext.getBean(eq("purchaseAuthorizationDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        lenient().when(applicationContext.getBean(eq("purchaseSettlementDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
    }

    @Test
    void testExecuteSequentialTasks() throws Exception {
        // Given
        when(execution.getVariable("taskFlow")).thenReturn("authorize,settle");
        when(execution.getVariable("transactionType")).thenReturn("Purchase");

        // When
        taskRouterDelegate.execute(execution);

        // Then
        verify(applicationContext, times(2)).getBean(anyString(), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class));
    }

    @Test
    void testExecuteParallelTasks() throws Exception {
        // Given
        when(execution.getVariable("taskFlow")).thenReturn("validate,parallel:post");
        when(execution.getVariable("transactionType")).thenReturn("Payment");

        // When
        taskRouterDelegate.execute(execution);

        // Then
        verify(applicationContext, atLeastOnce()).getBean(anyString(), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class));
    }

    @Test
    void testExecuteWithNullTaskFlow() {
        // Given
        when(execution.getVariable("taskFlow")).thenReturn(null);
        when(execution.getVariable("transactionType")).thenReturn("Purchase");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> taskRouterDelegate.execute(execution));
    }

    @Test
    void testExecuteWithEmptyTaskFlow() {
        // Given
        when(execution.getVariable("taskFlow")).thenReturn("");
        when(execution.getVariable("transactionType")).thenReturn("Purchase");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> taskRouterDelegate.execute(execution));
    }

    @Test
    void testExecuteWithMissingDelegate() throws Exception {
        // Given
        when(execution.getVariable("taskFlow")).thenReturn("authorize");
        when(execution.getVariable("transactionType")).thenReturn("Purchase");
        when(applicationContext.getBean(anyString(), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenThrow(new RuntimeException("Bean not found"));

        // When
        taskRouterDelegate.execute(execution);

        // Then - should not throw exception, should handle gracefully
        verify(applicationContext).getBean(anyString(), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class));
    }
}