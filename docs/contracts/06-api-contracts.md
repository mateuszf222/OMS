# 06-api-contracts.md — Kontrakty synchronicznych API (HTTP)

Ten dokument jest **wiążącym kontraktem** dla wszystkich synchronicznych API HTTP w OMS oraz
**master-inwentarzem** całego API. Podejście **spec-first**: źródłem prawdy są pliki OpenAPI
w `openapi/`, a ten plik definiuje reguły, których one przestrzegają.

> **Granice dokumentu.** Zdarzenia asynchroniczne (Kafka/AsyncAPI) → [`07-event-contracts.md`](07-event-contracts.md).
> Niezawodność dostarczania (DLQ, inbox, retry, dedup) → [`08-messaging-reliability.md`](../messaging/08-messaging-reliability.md).
> Nazewnictwo warstw (`*Request/*Response`) i bazowe mapowanie wyjątek→HTTP → [`02-architecture-rules.md`](../architecture/02-architecture-rules.md)
> (referujemy, **nie** kopiujemy). Słownik pojęć → [`01-domain-dictionary.md`](../domain-language/01-domain-dictionary.md).
> Przy konflikcie z plikiem szczegółowym **`CLAUDE.md` wygrywa**. Reguły oznaczone **OBOWIĄZKOWY** są nienegocjowalne.

---

## 1. Cel i zakres

- Definiuje kontrakt REST/HTTP dla serwisów OMS: konwencje, wersjonowanie, idempotencję, model
  błędów, bezpieczeństwo wejścia, paginację i nagłówki.
- Egzekwowalny maszynowo: reguły `API-*` mają odpowiednik w regule Spectrala (sekcja 13).
- Każdy endpoint ma jawny **status**: `AKTUALNY` (kod istnieje), `PLANOWANY` (zaprojektowany,
  brak kodu), `RESERVED` (zarezerwowana przestrzeń pod przyszły kontekst). Generator **nie może**
  traktować `PLANOWANY`/`RESERVED` jak istniejącego kodu.

---

## 2. Model warstw kontraktów i konwencje `$ref`

```
06-api-contracts.md   (ten plik — rulebook HTTP + master-inwentarz)
        │ rządzi
        ▼
openapi/<serwis>.vN.yaml ──$ref──┐
   │ $ref                         │
   ▼                              ▼
_shared/ (problem, pagination,   data-contracts/ (Order, OrderLine, Money, Address)
 idempotency, security/)          = kanoniczne kształty domeny (SSoT, transport-agnostic)
```

**Reguły kompozycji (OBOWIĄZKOWE):**
- **DC-REF:** żaden plik serwisu **nie definiuje kształtu domenowego inline** — wyłącznie `$ref`
  do `data-contracts/` (np. `Money`, `OrderLine`). To likwiduje duplikację schematów między
  serwisami (audit: 3× zduplikowany `OrderCreatedEvent`).
- **SH-REF:** mechanika przekrojowa (`problem`, `pagination`, `idempotency`, `security`) —
  wyłącznie `$ref` do `openapi/_shared/`.
- **Wersjonowanie plików:** każdy kontrakt i kontrakt danych ma jawny sufiks `.vN`
  (`order-service.v1.yaml`, `data-contracts/Money.v1.yaml`). Plik bez wersji jest zakazany.
- **Money tylko raz:** kanoniczny `Money` żyje w `data-contracts/Money.v1.yaml`. **Nie** tworzymy
  drugiej definicji w `_shared/` (decyzja D2).

---

## 3. Master-inwentarz API

| Serwis | Endpointy HTTP | Zdarzenia (→ `07`) | Status | Spec |
| :--- | :--- | :--- | :--- | :--- |
| order-service | `POST /orders`, `POST /orders/{id}/cancel` (+ `GET` planowane, admin-cancel) | publikuje `order-events` | AKTUALNY (+ rozszerzenia) | `openapi/order-service.v1.yaml` |
| payment-service | `POST /payments/webhook` (zewnętrzny PayU) | publikuje `payment-events` | AKTUALNY | `openapi/payment-webhook.v1.yaml` |
| inventory-service | rezerwacje stanu | `inventory-events` | PLANOWANY | `openapi/inventory-service.v1.yaml` |
| shipping-service | wysyłki | `shipment-events` | PLANOWANY | `openapi/shipping-service.v1.yaml` |
| pricing-service | wyceny / `quoteId` | — | PLANOWANY | `openapi/pricing-service.v1.yaml` |
| customer-service | profil klienta | — | OPCJONALNY (Keycloak pokrywa większość) | `openapi/customer-service.v1.yaml` |

Szczegóły endpointów: sekcja 12. Kontrakty zdarzeń: `07`.

---

## 4. Konwencje REST i nazewnictwo

- **Base path:** `/api/v{major}` (obecnie `/api/v1`). Zasoby w liczbie mnogiej: `/orders`, `/payments`.
- **Media type:** `application/json`, UTF-8. Błędy: `application/problem+json` (RFC 7807).
- **Identyfikatory:** `UUID`. **Czas:** ISO-8601 w UTC. **Kwoty:** wyłącznie obiekt `Money`
  (`$ref data-contracts/Money.v1.yaml`), nigdy surowy `amount` (patrz API-SEC-1).
- **DTO:** wejście `*Request`, wyjście `*Response` (definicja w `02`). Komendy aplikacyjne `*Command`
  są wewnętrzne — **nie** wyciekają do API.
- **CQS na granicy HTTP:** `GET` jest czystym odczytem (bez efektów ubocznych); mutacje to
  `POST/PUT/PATCH/DELETE`.

---

## 5. Wersjonowanie i kompatybilność — `API-VER-1`

- Wersjonowanie w URI: `/api/v1`. W obrębie wersji dozwolone **wyłącznie zmiany addytywne**
  (nowe pola opcjonalne, nowe endpointy).
- Zmiana łamiąca → nowa wersja `/api/v2` + nowy plik `*.v2.yaml`. Stara wersja przez okres
  przejściowy z nagłówkami `Deprecation` i `Sunset`.
- **Bramka CI:** `oasdiff` wykrywa zmianę łamiącą bez podniesienia wersji i blokuje merge.

---

## 6. Idempotencja — `API-IDEM-1`

**Status: OBOWIĄZKOWY.** Każdy `POST` tworzący zasób lub wywołujący nieidempotentny skutek
wymaga nagłówka `Idempotency-Key`.

**Kiedy stosować (trigger):** projektujesz endpoint, który tworzy zasób (`POST /orders`) lub
wykonuje skutek, którego nie wolno powtórzyć (anulowanie, refund).

**Kontrakt:**
- Klient wysyła `Idempotency-Key: <UUID>`.
- Serwer zapisuje `(klucz → wynik)` w Redis (TTL **24h**) w ramach przetwarzania.
- Powtórka z tym samym kluczem → **ta sama** odpowiedź (status + body), bez ponownego skutku.
- Ten sam klucz z **innym** body → `409 Conflict` (`type: .../idempotency-key-reuse`).

**Dobrze (nasz projekt):**
```http
POST /api/v1/orders
Idempotency-Key: 7c9e6679-7425-40de-944b-e07fc1f90ae7
Content-Type: application/json

{ "items": [ { "productId": "1111...", "quantity": 2 } ] }
```

**Źle (czego nie generować):**
```http
POST /api/v1/orders          # brak Idempotency-Key — retry sieciowy tworzy duplikat zamówienia
{ "items": [ ... ] }
```

**Dlaczego u nas:** retry klienta po timeoucie nie może tworzyć dwóch zamówień (audit B5).
Mechanizm współdzieli istniejący Redis (`spring.data.redis`), ten sam, którego używa dedup
konsumentów Kafki.

---

## 7. Rate limiting — `API-RATE-1`

- Limitowanie na **api-gateway** (Spring Cloud Gateway `RequestRateLimiter` + Redis — już dostępny).
- Klucz limitu: `sub` z JWT (per użytkownik), z fallbackiem na IP dla ścieżek publicznych.
- Przekroczenie → `429 Too Many Requests` + nagłówek `Retry-After` + body RFC 7807
  (`type: .../rate-limit-exceeded`). Limity konfigurowalne per route.

---

## 8. Model błędów — `API-ERR-1`

Każdy błąd HTTP zwracany jako **RFC 7807 `ProblemDetail`** (`application/problem+json`).
Pole `type` to **realny URI** z przestrzeni `https://errors.enterprise-oms.com/{kod}` —
**nigdy `about:blank`** (audit API-4). Schemat: `openapi/_shared/problem.yaml`.

**Katalog typów (źródło prawdy dla `type`/HTTP):**

| `type` (skrót) | HTTP | Kiedy | Obecne mapowanie w kodzie |
| :--- | :--- | :--- | :--- |
| `validation-failed` | 400 | błąd walidacji `*Request` | `MethodArgumentNotValidException` |
| `malformed-request` | 400 | niepoprawny JSON / typ | `HttpMessageNotReadable`, `TypeMismatch` |
| `unauthorized` | 401 | brak/nieważny token | Spring Security |
| `forbidden` | 403 | brak roli / nie właściciel | rola / API-SEC-2 |
| `order-not-found` | 404 | zasób nie istnieje (lub cudzy) | `OrderNotFoundException` |
| `domain-rule-violation` | 422 | naruszenie niezmiennika domeny | `OrderDomainException` |
| `product-not-available` | 422 | produkt spoza katalogu | `ProductNotAvailableException` |
| `order-state-conflict` | 409 | niedozwolone przejście stanu | `InvalidOrderStateTransitionException` |
| `concurrent-modification` | 409 | blokada optymistyczna | `OptimisticLockingFailureException` |
| `idempotency-key-reuse` | 409 | ten sam klucz, inne body | API-IDEM-1 |
| `rate-limit-exceeded` | 429 | przekroczony limit | API-RATE-1 |
| `internal-error` | 500 | błąd nieoczekiwany | `Exception` |

**Kształt odpowiedzi:**
```json
{
  "type": "https://errors.enterprise-oms.com/validation-failed",
  "title": "Validation Failed",
  "status": 400,
  "detail": "items: Zamówienie musi zawierać co najmniej jeden produkt",
  "instance": "/api/v1/orders",
  "timestamp": "2026-06-30T18:00:00Z",
  "errors": [ { "field": "items", "message": "..." } ]
}
```

> Bazowe mapowanie „niezmiennik → 422, blokada/konflikt → 409" jest zdefiniowane w `02`; tutaj
> żyje **konkretny katalog `type`**. Nie duplikuj mapowania — referuj `02`.

---

## 9. Bezpieczeństwo wejścia i autoryzacja — `API-SEC`

### `API-SEC-1` — zakaz wartości monetarnych w żądaniu

**Status: OBOWIĄZKOWY.** Żadna kwota/cena (`amount`, `price`, `total`, `Money`) **nie może**
pojawić się w ciele żądania ani w parametrach. Serwer wyznacza kwoty z **zaufanego źródła**
(Catalog/Pricing).

**Dobrze (nasz projekt):**
```java
public record CreateOrderRequest(List<OrderItemRequest> items) {
    public record OrderItemRequest(UUID productId, int quantity) {}   // tylko co i ile
}
// cena: serwer pobiera z ProductPriceCatalog (trustedUnitPrice)
```

**Źle (czego nie generować):**
```java
public record OrderItemRequest(UUID productId, int quantity, BigDecimal unitPrice) {}
// klient podaje cenę → ROLE_USER zamawia za 0.01 (regresja bugu z audytu)
```

**Dlaczego u nas:** utrwala naprawę luki cenowej — cena jest brana z [`InMemoryProductPriceCatalog`](../../order-service/src/main/java/org/example/orderservice/infrastructure/adapter/out/pricing/InMemoryProductPriceCatalog.java)
(docelowo Catalog/Pricing), nigdy z requestu.

### Pozostałe reguły bezpieczeństwa wejścia

- **`API-SEC-2` (ownership / BOLA):** wołający operuje wyłącznie na **swoich** zasobach
  (`customerId` z `sub` == właściciel zasobu). Dla cudzego zasobu zwróć `404`, nie `403`
  (nie ujawniaj istnienia). Wzorzec już w [`OrderCancellationService`](../../order-service/src/main/java/org/example/orderservice/application/service/OrderCancellationService.java).
- **`API-SEC-3`:** `customerId`/`adminId` zawsze z tokenu (`sub` / rola realm), **nigdy z body**.
- **`API-SEC-4`:** walidacja wejścia (jakarta) z **ograniczeniami wartości** — np. `quantity`
  w zakresie `1..MAX` (brak górnego limitu → przekroczenie PY-LIMIT-1, audit B2).

### Model autoryzacji

- OAuth2 Resource Server, JWT z Keycloak. Role z `realm_access.roles` → `ROLE_*`
  ([`KeycloakJwtAuthenticationConverter`](../../order-service/src/main/java/org/example/orderservice/infrastructure/config/KeycloakJwtAuthenticationConverter.java)).
- Endpointy klienta → `ROLE_USER`; administracyjne → `ROLE_ADMIN`; webhook → bez JWT, **podpis** (sekcja 12.2).

---

## 10. Paginacja, filtrowanie, sortowanie — `API-PAGE-1`

- Kolekcje przyjmują: `page` (0-based), `size` (domyślnie 20, **max 100**), `sort=pole,kierunek`.
- Odpowiedź w kopercie:
```json
{ "content": [ ... ],
  "page": { "number": 0, "size": 20, "totalElements": 137, "totalPages": 7 } }
```
- Schemat: `openapi/_shared/pagination.yaml`. Filtrowanie tylko po jawnie dozwolonych polach.

---

## 11. Nagłówki standardowe — `API-HDR-1`

- `201 Created` → nagłówek `Location` z URI nowego zasobu.
- `Idempotency-Key` — przyjmowany na mutacjach (sekcja 6).
- **Propagacja kontekstu śledzenia:** `traceparent` (W3C Trace Context) przekazywany dalej do
  serwisów i do nagłówków Kafki (szczegóły propagacji przez broker → `08`).

---

## 12. Inwentarz endpointów per serwis

### 12.1. order-service (`openapi/order-service.v1.yaml`)

| Metoda + ścieżka | Rola | Idempotencja | Wejście | Sukces | Błędy (`type`) | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `POST /api/v1/orders` | USER | `Idempotency-Key` | `CreateOrderRequest` (`items[productId,quantity]`) | `201` + `Location` + `{orderId}` | validation, product-not-available, domain-rule-violation, rate-limit | AKTUALNY (do utwardzenia o IDEM/SEC-4) |
| `GET /api/v1/orders/{id}` | USER (owner) | — | — | `200` `OrderResponse` | unauthorized, forbidden/not-found | PLANOWANY |
| `GET /api/v1/orders` | USER | — | `page,size,sort` | `200` lista (koperta) | unauthorized | PLANOWANY |
| `POST /api/v1/orders/{id}/cancel` | USER (owner) | `Idempotency-Key` | `CancelOrderRequest{reason}` | `200` `CancelOrderResponse{status:enum}` | order-not-found, order-state-conflict | AKTUALNY (status jako enum, nie luźny String) |
| `POST /api/v1/admin/orders/{id}/cancel` | ADMIN | `Idempotency-Key` | `CancelOrderRequest{reason}` | `200` | forbidden, order-not-found, order-state-conflict | PLANOWANY (podpięcie `CancelOrderByAdminUseCase`) |

### 12.2. payment-service — webhook (`openapi/payment-webhook.v1.yaml`)

| Metoda + ścieżka | Auth | Idempotencja | Wejście | Odpowiedź | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST /api/v1/payments/webhook` | **podpis PayU** (`OpenPayu-Signature`), nie JWT | idempotentny wg PY-IDEM-1 (stan `COMPLETED/FAILED` → ignoruj) | `PayUNotification` (ignoreUnknown) | **zawsze `200`** przy poprawnym podpisie (też gdy ignorujemy); `400` malformed; `403` zły podpis | AKTUALNY |

> Kontrakt **zewnętrzny** — kształt narzuca PayU; nie stosujemy do niego reguł `*Request`/`Money`
> z tego dokumentu. Podpis: docelowo HMAC-SHA256 stałoczasowo (audit BezP-1).

### 12.3. Serwisy PLANOWANE / RESERVED

`inventory-service`, `shipping-service`, `pricing-service`, `customer-service` — kontrakty
powstaną wraz z wdrożeniem kontekstu (mapa kontekstów: [raport audytu §2](../audit/2026-06-30-production-readiness-report.md)).
Do tego czasu istnieją jako szkielety oznaczone `PLANOWANY`/`RESERVED`.

---

## 13. OpenAPI jako artefakt i governance — `API-SPEC-1`

- **Źródło prawdy = pliki w `openapi/`** (spec-first). springdoc generuje spec z anotacji, a CI
  **porównuje** wynik z plikiem YAML i **failuje przy rozjeździe**. Swagger UI serwuje statyczny YAML.
  To zamienia obecny [`OpenApiConfig`](../../order-service/src/main/java/org/example/orderservice/infrastructure/config/OpenApiConfig.java)
  z generatora w weryfikator.
- **Reużycie:** `_shared/` i `data-contracts/` przez `$ref`. Bundling wieloplikowy: `redocly`/`swagger-cli`.
- **Spectral ruleset — maszynowe egzekwowanie reguł `API-*`** (przykłady reguł do wdrożenia):
  - `POST` tworzący zasób **musi** deklarować parametr `Idempotency-Key` (API-IDEM-1).
  - Schemat `*Request` **nie może** zawierać pól `amount|price|total` ani `$ref` „surowej" liczby
    dla kwoty — tylko `$ref` `Money` (API-SEC-1).
  - Każda odpowiedź błędu **musi** `$ref`-ować `problem.yaml` (API-ERR-1).
  - Brak `type: about:blank`.
- **Breaking changes:** `oasdiff` jako bramka (API-VER-1).
- **Pipeline CI:** `spectral lint` → `bundle/validate` → `springdoc-diff` → `oasdiff`.

---

## 14. Playbook: dodanie nowego mikroserwisu

1. Utwórz `openapi/<svc>.v1.yaml` z szablonu; referuj `_shared/` (mechanika) i `data-contracts/` (kształty).
2. Brakujące **kanoniczne kształty** dodaj do `data-contracts/` (`*.vN.yaml`) — **nigdy inline** w serwisie.
3. Jeśli serwis publikuje/konsumuje zdarzenia → dodaj kanał w `events/` i opisz go w [`07`](07-event-contracts.md)
   (payloady `$ref` do `data-contracts/`).
4. Zarejestruj serwis w **master-inwentarzu** (sekcja 3) ze statusem.
5. Politykę topiców/dostarczania uzupełnij w [`07`](07-event-contracts.md) / [`08`](../messaging/08-messaging-reliability.md).
6. Przejdź bramki CI (sekcja 13).
7. **Dopiero potem** generuj kod z zatwierdzonego kontraktu (kontrakt → kod, nie odwrotnie).

---

## 15. Powiązania i Definition of Done

**Powiązania:** [`01-domain-dictionary.md`](../domain-language/01-domain-dictionary.md) ·
[`02-architecture-rules.md`](../architecture/02-architecture-rules.md) ·
[`07-event-contracts.md`](07-event-contracts.md) ·
[`08-messaging-reliability.md`](../messaging/08-messaging-reliability.md) ·
[raport audytu](../audit/2026-06-30-production-readiness-report.md).

**Definition of Done (dla każdego serwisu w `openapi/`):**
- [ ] Walidatory OpenAPI + Spectral przechodzą; `$ref` rozwiązują się (bundling OK).
- [ ] Zero kwot/cen w schematach `*Request`; kwoty wyłącznie przez `Money` (`$ref`).
- [ ] Zero `type: about:blank`; wszystkie błędy `$ref`-ują `problem.yaml`.
- [ ] `POST` tworzące zasób mają `Idempotency-Key`.
- [ ] Każdy endpoint `AKTUALNY` zgodny z kodem; rozjazdy kod↔kontrakt wypisane jako osobne zadania.
- [ ] Endpointy `PLANOWANY`/`RESERVED` jawnie oznaczone (nie udają istniejącego kodu).

**Do zrobienia poza tym plikiem (kolejne kroki):** dopisać `06` i `07` do „Mapy wiedzy" w
[`CLAUDE.md`](../../CLAUDE.md) oraz odwołanie krzyżowe w `02`.
