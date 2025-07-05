package com.example.transactionprocessor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionProcessorApplicationTest {

    @Test
    void applicationShouldHaveMainMethod() {
        // Simple test to verify the main application class exists
        assertDoesNotThrow(() -> {
            TransactionProcessorApplication.class.getDeclaredMethod("main", String[].class);
        });
    }

    @Test
    void applicationClassShouldExist() {
        assertNotNull(TransactionProcessorApplication.class);
    }
}