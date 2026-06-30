# 07-event-contracts.md — Kontrakty zdarzeń asynchronicznych (Kafka)

Ten dokument jest **wiążącym kontraktem** dla wszystkich zdarzeń integracyjnych publikowanych na
Apache Kafka oraz **inwentarzem topologii** (zdarzenie → topic). Podejście **spec-first**: źródłem
prawdy są pliki **AsyncAPI 3.0** w `events/`, a ten plik definiuje reguły, których one przestrzegają.

> **Granice dokumentu.** Synchroniczne API HTTP → [`06-api-contracts.md`](06-api-contracts.md).
> Niezawodność dostarczania (DLQ, inbox, retry, dedup, kolejność) → [`08-messaging-reliability.md`](../messaging/08-messaging-reliability.md).
> Nazewnictwo zdarzeń (czas przeszły) i wzorzec listenerów → [`05-coding-guidelines.md`](../coding-guidelines/05-coding-guidelines.md).
> Tabela zdarzeń integracyjnych i reguły biznesowe → [`01-domain-dictionary.md`](../domain-language/01-domain-dictionary.md) §3–4.
> Transactional Outbox → [`02-architecture-rules.md`](../architecture/02-architecture-rules.md).
> Przy konflikcie **`CLAUDE.md` wygrywa**. Reguły **OBOWIĄZKOWY** są nienegocjowalne.
>
> **Granica `07` ↔ `08`:** `07` mówi *czym jest komunikat* (schemat, topic, wersja, routing);
> `08` mówi *jak go niezawodnie dostarczyć* (DLQ, inbox, retry, dedup, kolejność). Zero nakładania.

---

## 1. Cel i zakres

- Definiuje: kopertę zdarzenia, schematy payloadów, topologię i routing, wersjonowanie oraz
  obowiązki producenta i konsumenta dla zdarzeń Kafka.
- Egzekwowalny maszynowo: reguły `EVT-*` mają odpowiednik w regule Spectrala (AsyncAPI, sekcja 11).
- Każde zdarzenie ma jawny **status**: `AKTUALNY`, `NAPRAWA` (istnieje, ale łamie kontrakt — do
  korekty), `PLANOWANY`, `RESERVED`.

---

## 2. Model warstw i konwencje `$ref`

```
07-event-contracts.md   (ten plik — rulebook zdarzeń + topologia)
        │ rządzi
        ▼
events/<domena>.vN.yaml  (AsyncAPI 3.0) ──$ref──► data-contracts/ (Money, OrderLine, Address)
```

- **DC-REF (wspólne z `06`):** payload zdarzenia montuje pola specyficzne dla zdarzenia, ale
  kształty domenowe bierze przez `$ref` z `data-contracts/` (np. `OrderLine`, `Money`) — **nigdy
  inline**. To likwiduje duplikację (audit EVT-2: 3× zduplikowany `OrderCreatedEvent`).
- Schemat payloadu danego zdarzenia żyje w pliku `events/` (jako `components.messages`/`schemas`),
  nie w `data-contracts/` (tam są tylko kanoniczne kształty domeny, nie zdarzenia).

---

## 3. Koperta zdarzenia — `EVT-ENV-1`

**Standard: CloudEvents 1.0, tryb binarny nad Kafką** — metadane w nagłówkach, `data` = payload JSON.
Rozszerza obecne podejście nagłówkowe (`outbox-event-id`, `outbox-event-type`).

| Atrybut | Nośnik | Znaczenie | Migracja z dziś |
| :--- | :--- | :--- | :--- |
| `ce_id` | nagłówek | Unikalny id zdarzenia (klucz dedup) | z `outbox-event-id` |
| `ce_type` | nagłówek | Typ zdarzenia, np. `OrderCreatedEvent` (routing) | z `outbox-event-type` |
| `ce_source` | nagłówek | Serwis-producent (np. `order-service`) | nowe |
| `ce_time` | nagłówek | Czas wystąpienia (ISO-8601 UTC) | nowe |
| `ce_dataschema` | nagłówek | URI schematu+wersji payloadu | nowe |
| `traceparent` | nagłówek | W3C Trace Context (propagacja śladu) | nowe (audit OBS-2) |
| *(klucz wiadomości)* | Kafka key | Klucz partycjonowania = id agregatu | bez zmian |
| `data` | wartość | Payload JSON wg schematu z `events/` | bez zmian |

---

## 4. Topologia i routing — `EVT-TOPIC-1`

**Status: OBOWIĄZKOWY.** Jeden schemat zdarzenia = jeden topic. Producent **routuje po typie
zdarzenia** (`ce_type`) do dedykowanego topicu. Publikacja wielu schematów na jeden topic jest zakazana.

**Kiedy stosować (trigger):** dodajesz/publikujesz nowe zdarzenie albo piszesz relay outboxa.

**Nazewnictwo topiców:** `<domena>.<agregat>-events.<zdarzenie>.v<major>`
(np. `order.order-events.created.v1`). **Klucz partycji** = id agregatu producenta
(`orderId` dla order-events, `paymentId` dla payment-events) → kolejność per agregat.

**Dobrze (nasz projekt — już istnieje):**
```java
// payment-service OutboxMessageRelay — routing po typie
private String topicFor(String eventType) {
    return switch (eventType) {
        case "PaymentCompletedEvent" -> topics.getPaymentCompletedEvents();
        case "PaymentFailedEvent"    -> topics.getPaymentFailedEvents();
        ...
    };
}
```

**Źle (czego nie generować — to jest audit B1):**
```java
// order-service OutboxMessageRelay — wszystko na jeden topic „...created.v1"
publishOutboxMessage(topics.getOrderEvents(), event);   // OrderCancelledEvent ląduje na topicu created
```

**Dlaczego u nas:** `OrderCancelledEvent` trafiał na `order.order-events.created.v1` i był
deserializowany przez konsumentów jako `OrderCreatedEvent` (poison/fałszywe powiadomienia).
Naprawa: `OrderCancelledEvent` dostaje własny topic i order-outbox routuje po typie (jak payment).

---

## 5. Nazewnictwo zdarzeń

- Typy zdarzeń w **czasie przeszłym** z sufiksem `Event`: `OrderCreatedEvent`,
  `PaymentCompletedEvent` (zgodnie z `05` §7).
- Nazwa zdarzenia w topicu = rdzeń bez sufiksu, kebab/lower (`created`, `cancelled`, `completed`, `failed`).

---

## 6. Wersjonowanie i kompatybilność — `EVT-VER-1`

- Wersja **major** w nazwie topicu (`.vN`) i w `ce_dataschema`.
- W obrębie wersji **wyłącznie zmiany addytywne** (nowe pola **opcjonalne**). Usunięcie/zmiana
  typu pola = breaking → **nowy topic** `.vN+1` + okno współbieżnej publikacji (dual-publish),
  potem wygaszenie starego.
- **Konsument = tolerant reader:** ignoruje nieznane pola, nie zakłada kolejności.
- **Bramka CI:** kontrola kompatybilności schematu (sekcja 11).

---

## 7. Kompletność i semantyka payloadu — `EVT-PAYLOAD-1`

Payload niesie to, czego potrzebują **konsumenci** (sterowane regułami `01` §4).

- **`OrderCreatedEvent` MUSI nieść `items[]`** (`$ref OrderLine`) — `01` §NT-TRIG-1 wymaga e-maila
  z wykazem pozycji; dziś event ma tylko `totalAmount` (audit EVT-4).
- **`PaymentFailedEvent.reason` = obiekt `{ code, detail }`**, nie wolny string. `code` to enum:
  `LIMIT_EXCEEDED | GATEWAY_REJECTED | GATEWAY_TIMEOUT | GATEWAY_CANCELLED | UNKNOWN`. Dziś `reason`
  jest zaszyte na stałe (`"Payment rejected by gateway"`) — łamie NT-TRIG-1 (audit EVT-3).
- Kwoty wyłącznie jako `Money` (`$ref`), identyfikatory jako `UUID`.

---

## 8. Obowiązki producenta — `EVT-PROD-1`

**Status: OBOWIĄZKOWY.** Publikacja **wyłącznie przez Transactional Outbox** — zakaz bezpośredniego
`KafkaTemplate.send` z warstwy aplikacji (`02` §Transactional Outbox).

- Zapis zdarzenia do `outbox_events` w **tej samej transakcji** co zmiana agregatu.
- Relay ustawia kopertę (`ce_id`, `ce_type`, …), **routuje po typie** do właściwego topicu
  (`EVT-TOPIC-1`), klucz = id agregatu.
- Jeden schemat na topic; brak „dziury" routingu (każdy typ ma `topicFor`).

---

## 9. Obowiązki konsumenta — `EVT-CONS-1`

- Deserializuj wg **jednego** schematu danego topicu. **Zakaz** „czytania każdego rekordu jako
  `OrderCreatedEvent`" (audit B1) — to konsekwencja złamania `EVT-TOPIC-1` po stronie producenta.
- **Tolerant reader** (sekcja 6). Idempotencja, retry, DLQ i obsługa konfliktów → [`08`](../messaging/08-messaging-reliability.md).
- Dla rodzin zdarzeń (`sealed interface`) na wspólnym topicu — rozgałęzienie przez pattern
  matching `switch` (`05` §1). Domyślnie jednak: **jeden typ = jeden topic**.

---

## 10. Master-inwentarz zdarzeń (topologia docelowa)

| Zdarzenie | Topic | Producent | Konsumenci | Payload (kluczowe pola) | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `OrderCreatedEvent` | `order.order-events.created.v1` | order | payment, notification | `orderId, customerId, items[] ($ref OrderLine), totalAmount ($ref Money)` | AKTUALNY (dodać `items`) |
| `OrderCancelledEvent` | `order.order-events.cancelled.v1` | order | notification *(rozszerzenie NT-TRIG, PLANOWANY)* | `orderId, customerId, reason, previousStatus` | **NAPRAWA** (dziś mis-routed → B1) |
| `PaymentInitiatedEvent` | `payment.payment-events.initiated.v1` | payment | payment-gateway-worker | `paymentId, orderId, amount ($ref Money)` | AKTUALNY |
| `PaymentCompletedEvent` | `payment.payment-events.completed.v1` | payment | order, notification | `orderId, paymentId, customerId` | AKTUALNY |
| `PaymentFailedEvent` | `payment.payment-events.failed.v1` | payment | order, notification | `orderId, paymentId, customerId, reason{code,detail}` | AKTUALNY (`reason` → enum) |
| `ShipmentDispatchedEvent` | `shipping.shipment-events.dispatched.v1` | shipping | notification, order | `shipmentId, orderId, address ($ref Address)` | PLANOWANY |

> `OrderCancelledEvent`: kontraktowo dostaje **własny** topic. Decyzja produktowa, czy notification
> go konsumuje (rozszerzenie NT-TRIG-1) — ale **bezwzględnie nie wolno** mu trafiać na topic `created`.

---

## 11. AsyncAPI jako artefakt i governance — `EVT-SPEC-1`

- **Źródło prawdy = `events/*.vN.yaml` (AsyncAPI 3.0).** `operations` (send/receive), `channels`
  (= topici), `messages` z `payload` `$ref` do `data-contracts/` i nagłówkami koperty (sekcja 3);
  `bindings.kafka` (topic, key).
- **Spectral (ruleset AsyncAPI) egzekwuje `EVT-*`:**
  - kanał ma dokładnie **jeden** `message` (EVT-TOPIC-1, jeden schemat/topic),
  - payload `$ref`-uje `data-contracts/` dla kształtów domenowych (zakaz inline `Money`),
  - komunikat deklaruje atrybuty koperty (`ce_id`, `ce_type`),
  - nazwa kanału pasuje do wzorca `<domena>.<agregat>-events.<zdarzenie>.v<major>`.
- **Kompatybilność:** kontrola schematu w CI przy zmianie (EVT-VER-1).
- **Pipeline CI:** `spectral lint (asyncapi)` → `validate` → `schema-compat`.

---

## 12. Powiązania i Definition of Done

**Powiązania:** [`01`](../domain-language/01-domain-dictionary.md) (zdarzenia integracyjne, NT-TRIG) ·
[`02`](../architecture/02-architecture-rules.md) (Outbox) · [`05`](../coding-guidelines/05-coding-guidelines.md)
(listenery, nazewnictwo) · [`06`](06-api-contracts.md) · [`08`](../messaging/08-messaging-reliability.md) ·
[raport audytu](../audit/2026-06-30-production-readiness-report.md).

**Definition of Done (dla każdego zdarzenia w `events/`):**
- [ ] Ma **własny** topic; producent routuje po `ce_type` (EVT-TOPIC-1).
- [ ] Payload `$ref`-uje `data-contracts/` (brak inline `Money`/`OrderLine`); kompletny wg potrzeb konsumentów.
- [ ] `PaymentFailedEvent.reason` to enum+detail; `OrderCreatedEvent` ma `items[]`.
- [ ] Koperta CloudEvents (`ce_*` + `traceparent`); klucz partycji = id agregatu.
- [ ] Publikacja przez Outbox; konsument = tolerant reader, deserializacja wg jednego schematu topicu.
- [ ] Walidatory AsyncAPI + Spectral przechodzą; rozjazdy kod↔kontrakt wypisane jako osobne zadania
      (m.in. order-outbox routing, `reason` enum, `items` w OrderCreated).
