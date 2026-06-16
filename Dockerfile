# --- Containerized Android Build Pipeline Strategy ---
# This Dockerfile outlines the containerized build automation pipeline
# for Yanga Market superapp, ensuring repeatable, isolated APK compilation
# in cloud-native deployment environments (such as Jenkins, GitHub Actions, or GitLab CI).

FROM eclipse-temurin:17-jdk-jammy AS build-env

# 1. Setup Linux & Android command-line SDK targets
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH=${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools

WORKDIR /yanga-market-build

# Install basic networking and compression packages
RUN apt-get update && apt-get install -y wget unzip git && rm -rf /var/lib/apt/lists/*

# Download and secure the official Android Command Line Tools
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline.zip && \
    unzip cmdline.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && \
    mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest && \
    rm cmdline.zip

# Auto-accept Android SDK licenses and install platform compile configurations (SDK 36)
RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0-rc1"

# 2. Package and compile the Yanga Market application
# Copy full root workspaces (excluding files highlighted in .gitignore)
COPY . .

# Convert line endings of Gradle commands and compile the APK
RUN chmod +x gradlew || true
RUN gradle assembleDebug --no-daemon

# --- Multi-stage deployment package ---
FROM alpine:latest AS artifact-repository

WORKDIR /completed-builds
COPY --from=build-env /yanga-market-build/app/build/outputs/apk/debug/app-debug.apk ./yanga-market-v1.0.0-debug.apk

CMD ["echo", "Yanga Market APK container compile completed successfully. Copied to volumes /completed-builds/yanga-market-v1.0.0-debug.apk."]
