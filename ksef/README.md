# Moduł `ksef` — klient KSeF API v2

Integracja z [KSeF API v2](https://api.ksef.mf.gov.pl/docs/v2/index.html): uwierzytelnianie **tokenem KSeF** (z MCU) oraz pobieranie **listy faktur** (metadane).

## Wymagania

- Token KSeF wygenerowany w Module Certyfikatów i Uprawnień (z uprawnieniem `InvoiceRead`)
- NIP firmy (kontekst)
- JVM 21+ (moduł korzysta z `java.security` do szyfrowania RSA-OAEP)

## Użycie

```kotlin
import com.kontenery.ksef.KsefClient
import com.kontenery.ksef.config.KsefConfig
import com.kontenery.ksef.config.KsefEnvironment
import com.kontenery.ksef.model.InvoiceListRequest

KsefClient(
    KsefConfig(
        nip = "5265877635",
        ksefToken = "twój-sekret-z-portalu",
        environment = KsefEnvironment.PRODUCTION, // lub TEST / DEMO
    ),
).use { client ->
    val session = client.authenticate()
    val result = client.getInvoices(
        session,
        InvoiceListRequest.lastMonths(months = 3),
    )
    result.invoices.forEach { println("${it.ksefNumber} ${it.invoiceNumber}") }
}
```

## Przepływ uwierzytelniania

1. `POST /auth/challenge`
2. Pobranie klucza publicznego `GET /security/public-key-certificates`
3. Szyfrowanie `token|timestampMs` (RSA-OAEP SHA-256) → `POST /auth/ksef-token`
4. Odpytywanie `GET /auth/{referenceNumber}` do statusu 200
5. `POST /auth/token/redeem` → `accessToken` + `refreshToken`

## Lista faktur

`POST /invoices/query/metadata` z filtrem dat i typem podmiotu (`Subject1` = sprzedawca domyślnie).

## Środowiska

| Wartość | URL |
|---------|-----|
| `PRODUCTION` | https://api.ksef.mf.gov.pl |
| `TEST` | https://api-test.ksef.mf.gov.pl |
| `DEMO` | https://api-demo.ksef.mf.gov.pl |

## Android / Compose

Moduł JVM — dodaj zależność w `androidMain` projektu `composeApp`:

```kotlin
implementation(project(":ksef"))
```

Wywołuj z wątku IO/coroutine (operacje są `suspend`).
