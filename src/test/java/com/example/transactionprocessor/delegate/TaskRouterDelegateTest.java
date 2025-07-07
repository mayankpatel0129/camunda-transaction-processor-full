package com.example.transactionprocessor.delegate;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskRouterDelegateTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private DelegateExecution execution;

    private MeterRegistry meterRegistry;
    private TaskRouterDelegate taskRouterDelegate;

    @BeforeEach
    void setUp() {
        // Use SimpleMeterRegistry instead of mocking MeterRegistry
        meterRegistry = new SimpleMeterRegistry();
        
        // Create instance and inject dependencies
        taskRouterDelegate = new TaskRouterDelegate();
        ReflectionTestUtils.setField(taskRouterDelegate, "applicationContext", applicationContext);
        ReflectionTestUtils.setField(taskRouterDelegate, "meterRegistry", meterRegistry);

        // Set up execution mock
        Map<String, Object> variables = new HashMap<>();
        lenient().when(execution.getVariables()).thenReturn(variables);
        lenient().when(execution.getProcessInstanceId()).thenReturn("test-process-123");
        lenient().when(execution.getBusinessKey()).thenReturn("test-business-key");

        // Manually set required fields to avoid @PostConstruct issues in tests
        ReflectionTestUtils.setField(taskRouterDelegate, "corePoolSize", 2);
        ReflectionTestUtils.setField(taskRouterDelegate, "maxPoolSize", 4);
        ReflectionTestUtils.setField(taskRouterDelegate, "queueCapacity", 10);
        ReflectionTestUtils.setField(taskRouterDelegate, "keepAliveSeconds", 60);
        ReflectionTestUtils.setField(taskRouterDelegate, "batchSize", 10);
        ReflectionTestUtils.setField(taskRouterDelegate, "batchTimeoutSeconds", 30);

        // Initialize the delegate (simulating @PostConstruct)
        taskRouterDelegate.init();

        // Mock delegate beans for all the new delegates
        setupDelegateMocks();
    }

    private void setupDelegateMocks() {
        // Purchase delegates
        lenient().when(applicationContext.getBean(eq("purchaseAuthorizationDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        lenient().when(applicationContext.getBean(eq("purchaseSettlementDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        lenient().when(applicationContext.getBean(eq("fraudCheckDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        lenient().when(applicationContext.getBean(eq("riskAssessmentDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        lenient().when(applicationContext.getBean(eq("notificationDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        lenient().when(applicationContext.getBean(eq("complianceDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));

        // Payment delegates
        lenient().when(applicationContext.getBean(eq("paymentValidationDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        lenient().when(applicationContext.getBean(eq("paymentPostingDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));

        // Adjustment delegates
        lenient().when(applicationContext.getBean(eq("adjustmentReviewDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        lenient().when(applicationContext.getBean(eq("adjustmentApplyDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        lenient().when(applicationContext.getBean(eq("autoReviewDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        lenient().when(applicationContext.getBean(eq("supervisorApprovalDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        lenient().when(applicationContext.getBean(eq("managerApprovalDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        lenient().when(applicationContext.getBean(eq("executiveApprovalDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        lenient().when(applicationContext.getBean(eq("auditTrailDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));

        // Refund delegates
        lenient().when(applicationContext.getBean(eq("refundValidationDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        lenient().when(applicationContext.getBean(eq("refundProcessDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));

        // Chargeback delegates
        lenient().when(applicationContext.getBean(eq("chargebackInvestigateDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        lenient().when(applicationContext.getBean(eq("chargebackDisputeDelegate"), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenReturn(mock(org.camunda.bpm.engine.delegate.JavaDelegate.class));
    }

    @Test
    void testExecuteSmallPurchaseSequentialTasks() throws Exception {
        // Given - small purchase uses simple sequential processing
        when(execution.getVariable("dmnResult")).thenReturn("authorize,settle");
        when(execution.getVariable("transactionType")).thenReturn("Purchase");

        // When
        taskRouterDelegate.execute(execution);

        // Then
        verify(applicationContext, times(2)).getBean(anyString(), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        
        // Verify metrics were updated
        Counter successCounter = meterRegistry.find("transaction.processing.success").counter();
        assert successCounter != null;
        assert successCounter.count() == 1.0;
    }

    @Test
    void testExecuteHighValuePurchaseParallelTasks() throws Exception {
        // Given - high value purchase uses fraud check and parallel processing
        when(execution.getVariable("dmnResult")).thenReturn("fraudCheck,authorize,parallel:settle,parallel:notification");
        when(execution.getVariable("transactionType")).thenReturn("Purchase");

        // When
        taskRouterDelegate.execute(execution);

        // Then
        verify(applicationContext, times(4)).getBean(anyString(), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        
        // Verify parallel task counter was incremented
        Counter parallelCounter = meterRegistry.find("transaction.parallel.tasks").counter();
        assert parallelCounter != null;
        assert parallelCounter.count() == 1.0;
    }

    @Test
    void testExecuteAdjustmentWithApprovalWorkflow() throws Exception {
        // Given - adjustment transaction with supervisor approval
        when(execution.getVariable("dmnResult")).thenReturn("review,supervisorApproval,apply,notification");
        when(execution.getVariable("transactionType")).thenReturn("Adjustment");

        // When
        taskRouterDelegate.execute(execution);

        // Then
        verify(applicationContext, times(4)).getBean(anyString(), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        
        // Verify sequential task counter was incremented
        Counter sequentialCounter = meterRegistry.find("transaction.sequential.tasks").counter();
        assert sequentialCounter != null;
        assert sequentialCounter.count() == 1.0;
    }

    @Test
    void testExecuteExecutiveApprovalAdjustment() throws Exception {
        // Given - high-value adjustment requiring executive approval and audit trail
        when(execution.getVariable("dmnResult")).thenReturn("review,executiveApproval,auditTrail,parallel:apply,parallel:notification,parallel:compliance");
        when(execution.getVariable("transactionType")).thenReturn("Adjustment");

        // When
        taskRouterDelegate.execute(execution);

        // Then
        verify(applicationContext, times(6)).getBean(anyString(), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        
        // Verify both parallel and success counters were incremented
        Counter parallelCounter = meterRegistry.find("transaction.parallel.tasks").counter();
        Counter successCounter = meterRegistry.find("transaction.processing.success").counter();
        assert parallelCounter != null && parallelCounter.count() == 1.0;
        assert successCounter != null && successCounter.count() == 1.0;
    }

    @Test
    void testExecuteWithNullTaskFlow() {
        // Given
        when(execution.getVariable("dmnResult")).thenReturn(null);
        when(execution.getVariable("transactionType")).thenReturn("Purchase");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> taskRouterDelegate.execute(execution));
    }

    @Test
    void testExecuteWithEmptyTaskFlow() {
        // Given
        when(execution.getVariable("dmnResult")).thenReturn("");
        when(execution.getVariable("transactionType")).thenReturn("Purchase");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> taskRouterDelegate.execute(execution));
    }

    @Test
    void testExecuteWithMissingDelegate() throws Exception {
        // Given
        when(execution.getVariable("dmnResult")).thenReturn("unknownTask");
        when(execution.getVariable("transactionType")).thenReturn("Purchase");
        when(applicationContext.getBean(anyString(), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class)))
                .thenThrow(new RuntimeException("Bean not found"));

        // When
        taskRouterDelegate.execute(execution);

        // Then - should not throw exception, should handle gracefully
        verify(applicationContext).getBean(anyString(), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        
        // Should still increment success counter for graceful handling
        Counter successCounter = meterRegistry.find("transaction.processing.success").counter();
        assert successCounter != null;
        assert successCounter.count() == 1.0;
    }

    @Test
    void testExecuteUltraHighValuePurchaseWithAllSecurityFeatures() throws Exception {
        // Given - ultra high value purchase with maximum security
        when(execution.getVariable("dmnResult")).thenReturn("fraudCheck,riskAssessment,authorize,parallel:settle,parallel:notification,parallel:compliance");
        when(execution.getVariable("transactionType")).thenReturn("Purchase");

        // When
        taskRouterDelegate.execute(execution);

        // Then
        verify(applicationContext, times(6)).getBean(anyString(), eq(org.camunda.bpm.engine.delegate.JavaDelegate.class));
        
        // Verify all relevant metrics
        Counter parallelCounter = meterRegistry.find("transaction.parallel.tasks").counter();
        Counter successCounter = meterRegistry.find("transaction.processing.success").counter();
        Timer processingTimer = meterRegistry.find("transaction.processing.time").timer();
        
        assert parallelCounter != null && parallelCounter.count() == 1.0;
        assert successCounter != null && successCounter.count() == 1.0;
        assert processingTimer != null && processingTimer.count() == 1.0;
    }
}