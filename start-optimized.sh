#!/bin/bash

# High-Performance JVM Settings for 1000s TPS Transaction Processing
# Optimized for multi-threaded, high-throughput applications

echo "Starting Transaction Processor with High-Performance JVM Settings..."

# JVM Memory Settings (Adjust based on available system memory)
HEAP_SIZE="8g"
NEW_GEN_SIZE="2g"
METASPACE_SIZE="512m"

# GC Settings - G1GC optimized for low latency and high throughput
GC_SETTINGS="-XX:+UseG1GC \
-XX:G1HeapRegionSize=32m \
-XX:MaxGCPauseMillis=50 \
-XX:G1NewSizePercent=20 \
-XX:G1MaxNewSizePercent=40 \
-XX:G1MixedGCCountTarget=8 \
-XX:G1MixedGCLiveThresholdPercent=85 \
-XX:G1OldCSetRegionThresholdPercent=10 \
-XX:+G1UseAdaptiveIHOP \
-XX:G1AdaptiveIHOPNumInitialSamples=3"

# Thread and Performance Settings
THREAD_SETTINGS="-XX:+UseBiasedLocking \
-XX:BiasedLockingStartupDelay=0 \
-XX:+OptimizeStringConcat \
-XX:+UseFastAccessorMethods \
-XX:+UseCompressedOops \
-XX:+UseCompressedClassPointers"

# JIT Compiler Optimizations
JIT_SETTINGS="-XX:+TieredCompilation \
-XX:TieredStopAtLevel=4 \
-XX:+UseCodeCacheFlushing \
-XX:ReservedCodeCacheSize=512m \
-XX:InitialCodeCacheSize=128m"

# NUMA and CPU Optimizations
CPU_SETTINGS="-XX:+UseNUMA \
-XX:+UseThreadPriorities \
-XX:ThreadPriorityPolicy=1"

# Large Pages for better memory performance
MEMORY_SETTINGS="-XX:+UseLargePages \
-XX:LargePageSizeInBytes=2m"

# Monitoring and Debugging (remove in production for max performance)
MONITORING_SETTINGS="-XX:+PrintGC \
-XX:+PrintGCDetails \
-XX:+PrintGCTimeStamps \
-XX:+PrintGCApplicationStoppedTime \
-Xloggc:gc.log \
-XX:+UseGCLogFileRotation \
-XX:NumberOfGCLogFiles=5 \
-XX:GCLogFileSize=100m"

# Network and IO Optimizations
NETWORK_SETTINGS="-Djava.net.preferIPv4Stack=true \
-Djava.awt.headless=true \
-Dsun.net.useExclusiveBind=false"

# Application-specific settings
APP_SETTINGS="-Dspring.profiles.active=production \
-Dmanagement.endpoints.web.exposure.include=health,info,metrics,prometheus \
-Dlogging.level.com.example.transactionprocessor=INFO"

# Security settings for production
SECURITY_SETTINGS="-Djava.security.egd=file:/dev/./urandom"

# Combine all JVM arguments
JVM_ARGS="-Xms${HEAP_SIZE} \
-Xmx${HEAP_SIZE} \
-Xmn${NEW_GEN_SIZE} \
-XX:MetaspaceSize=${METASPACE_SIZE} \
-XX:MaxMetaspaceSize=${METASPACE_SIZE} \
${GC_SETTINGS} \
${THREAD_SETTINGS} \
${JIT_SETTINGS} \
${CPU_SETTINGS} \
${MEMORY_SETTINGS} \
${MONITORING_SETTINGS} \
${NETWORK_SETTINGS} \
${APP_SETTINGS} \
${SECURITY_SETTINGS}"

echo "JVM Arguments: ${JVM_ARGS}"
echo "========================================"

# Start the application
java ${JVM_ARGS} -jar target/transaction-processor-1.0.0.jar

echo "Application stopped."