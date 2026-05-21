package com.kontenery.ksef.config

enum class KsefEnvironment(val baseUrl: String) {
    PRODUCTION("https://api.ksef.mf.gov.pl"),
    TEST("https://api-test.ksef.mf.gov.pl"),
    DEMO("https://api-demo.ksef.mf.gov.pl"),
}
