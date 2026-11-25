# Payit — UPI & Digital Wallet Android Application

> A modern, Google Pay–inspired UPI and digital wallet Android application built with Kotlin and Jetpack Compose (Material 3), featuring phone-OTP authentication, QR scanning, conversational peer-to-peer payments, bill recharges, scratch-card rewards, and local state persistence.

## Overview
Payit is an end-to-end Android fintech application crafted in Kotlin and Jetpack Compose using a custom Material Design 3 design system with adaptive Light and Dark themes. Modeled after Google Pay (GPay), it delivers a complete digital payment workflow starting from mobile number Sign In / Sign Up with 6-digit OTP verification to an interactive home dashboard. Users can scan QR codes, chat and send money to contacts or merchants, authorize transfers via an NPCI-style 4-digit UPI PIN pad or PIN-free UPI Lite, pay utility bills and mobile recharges, scratch cashback reward cards, check bank balances and CIBIL scores, and persist all transactions and account states locally on the device.

## Key Highlights
* **Tech Stack**: Kotlin, Jetpack Compose, Material Design 3, Custom Canvas Graphics & Vector Iconography (`ImageVector`), `SharedPreferences` + JSON local persistence.
* **Core Flows**:
  * Phone + 6-Digit OTP Authentication
  * QR Scanner & Custom Scannable QR Generator (BharatQR, PhonePe, Paytm, GPay compatible)
  * P2P & Merchant Chat Threads
  * 3-Stage Send Money + NPCI UPI PIN Pad + Digital Receipt
  * PIN-Free UPI Lite (₹1,850 wallet with instant top-ups) & Multi-Bank Management
  * Utility Bills (BESCOM, Airtel Postpaid, Tata Play DTH, FASTag), Mobile Recharge, CIBIL Score & Interactive Scratch Cards

## UI Design Frameworks & Architecture
* **Material 3 Token Architecture**: Semantic color role tokens (`surface`, `surfaceContainer`, `primary`, `outlineVariant`) with adaptive Light/Dark tonal elevations.
* **Thumb-Zone Spatial Ergonomics**: Primary payment triggers anchored in the bottom 40% thumb reach arc with strict ≥48dp touch bounds following Fitts's Law.
* **Progressive Disclosure**: 3-stage transfer flow (Payee verification → Account selector → NPCI 4-digit PIN authentication → Digital receipt) minimizing cognitive load.
* **Unidirectional Data Flow (UDF)**: Composable state hoisting with immutable data models and deterministic offline persistence via `SharedPreferences` + JSON.

## Tech Stack
* **Language**: Kotlin
* **UI Toolkit**: Jetpack Compose & Material 3
* **Graphics**: Custom Canvas & Vector Iconography (`ImageVector`)
* **Local Persistence**: `SharedPreferences` + JSON
* **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)
