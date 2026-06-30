# CLAUDE.md — Enterprise Order Management System (OMS)

Rozproszona platforma do asynchronicznego zarządzania cyklem życia zamówień.
Mikroserwisy + Event-Driven Architecture na Apache Kafka, spójność ostateczna
gwarantowana wzorcem **Transactional Outbox**. Serwisy: **Order**, **Payment**,
**Notification**.

> Ten plik to **mapa i zasady nadrzędne**, ładowane zawsze. Szczegóły są w plikach
> źródłowych (sekcja *Mapa wiedzy*). Gdy ten plik mówi co innego niż plik szczegółowy,
> **CLAUDE.md wygrywa** — to celowe rozstrzygnięcia kilku rozbieżności między plikami.

---

## Stack (skrót)

Java **21** · Spring Boot **3.3.2** · Spring Cloud 2023.0.3 · PostgreSQL (schema **wyłącznie** przez Flyway) · Apache Kafka (`spring-kafka`) · Keycloak (OAuth2 Resource Server, JWT) · MapStruct · Lombok (tylko strukturalne adnotacje) · Resilience4j · Testcontainers.

Pełny wykaz wersji i wytycznych implementacyjnych: **`02-architecture-rules.md`**.

---

## Architektura (zasada nadrzędna)

Dwie osie, które się **nie wykluczają**:

- **Hexagonal (Ports & Adapters)** rządzi *kierunkiem zależności*: `adapter → application → domain`, nigdy odwrotnie. `domain/` ma **zero** zależności do Springa, JPA, Flyway, Kafki.
- **Vertical Slice** rządzi *grupowaniem* warstwy aplikacji: use case'y w osobnych katalogach (`application/createorder/`, `application/cancelorder/`…).

Reguły pakietowania (rozstrzygają konflikt 02 ↔ 03 §1.1):
- **Heksagon — zawsze.**
- **Slice w `application/` — gdy use case'ów w serwisie jest ≥ ~4.** Mniej → zostań przy zwykłym podziale warstwowym.
- **Domena nigdy nie jest slice'owana.** Agregat (`Order`, `Payment`) jest wspólną granicą spójności i pilnuje niezmienników — duplikowanie go per slice jest zakazane. Porty wyjściowe (`application/port/out/`) i adaptery `out/` też są wspólne.

---

## Żelazne zasady kodu (obowiązują w każdym zadaniu)

- **Rich Domain Model.** Stan mutowany wyłącznie intencyjnymi metodami biznesowymi (`order.confirm()`), nigdy setterami (`setStatus(...)`). Zakaz modeli anemicznych. → *Tell, Don't Ask*.
- **Brak infrastruktury w domenie.** Żadnych adnotacji JPA/Spring w `domain/`. `Order` (domena) i `OrderJpaEntity` to osobne byty, mapowane MapStructem.
- **Pieniądze tylko jako `Money` (Value Object).** Zakaz `BigDecimal`/`Double`/`int` dla kwot (Primitive Obsession).
- **Constructor injection.** `private final` + `@RequiredArgsConstructor`. Wstrzykiwanie przez pola dozwolone wyłącznie w testach.
- **Optimistic locking.** Każda encja JPA ma `@Version`; `OptimisticLockingFailureException` → HTTP 409.
- **Outbox.** Zakaz publikacji do Kafki z warstwy aplikacyjnej — wyłącznie zapis do `outbox_events` w tej samej transakcji + `OutboxMessageRelay`.
- **Java 21 obowiązkowo:** `record` (DTO/Event/Command/VO), `sealed interface` + pattern matching dla zamkniętych hierarchii, text blocks. → szczegóły i lista „zabronione/przestarzałe": `AI_CODING_GUIDELINES.md`.
- **Siedem obowiązkowych wzorców** (status `OBOWIĄZKOWY` w `03-...md`): Value Object, Static Factory Methods, First-Class Collections, Wrap Semantyczny, CQS, Guard Clauses, Test Data Builder. Stosuj je z automatu, nie po wskazaniu.

---

## Rozstrzygnięcia konfliktów (to jest wersja wiążąca)

- **Wyjątki vs Result.** Domena (VO, agregaty) **rzuca** wyjątki domenowe przy naruszeniu niezmienników (`OrderDomainException` → 422, blokady → 409). Warstwa aplikacji (use case'y) **zwraca `Result<T>`** dla przewidywalnych wyników biznesowych. Wyjątków nie używamy do sterowania przepływem w use case'ach. (godzi 01/02 ↔ 03 §3.5, zgodnie z `04-testing.md`).
- **Duplikacja.** Tolerowana **między slice'ami warstwy aplikacji** (nie sklejaj na siłę podobnych use case'ów). Zakazana w **plumbingu infrastruktury**: jeden `@KafkaListener` + `switch` pattern matching + wspólna `handleIdempotently` (godzi 05-coding-guidelines §6 ↔ 03 §1.1).
- **Nazewnictwo metod.** Wiążące są reguły z `05-coding-guidelines.md §7` (np. `complete()` zamiast `confirmPayment()`). Przykład `order.confirmPayment(...)` w `02` ilustruje **wyłącznie** zasadę intencyjnej metody kontra setter — nie konkretną nazwę.
- **Słownik domeny jest wiążący.** Synonimy i słowa zakazane z `01-domain-dictionary.md` obowiązują w kodzie, nazwach i komentarzach (np. `Order`, nie `Cart`; `Money`, nie surowy `amount`; `customerId`, nie `User`).

---

## Mapa wiedzy — co przeczytać do danego zadania

| Robisz… | Przeczytaj |
| :--- | :--- |
| Modelujesz domenę, nazywasz pojęcia, sprawdzasz regułę biznesową/niezmiennik (OR-INV, PY-LIMIT, idempotencja webhooków) | `01-domain-dictionary.md` |
| Decyzje o warstwach, stacku, pakietach, obsłudze błędów (RFC 7807), konwencjach `*Request/*Command/*JpaEntity` | `02-architecture-rules.md` |
| Piszesz/refaktoryzujesz kod domenowy lub aplikacyjny i dobierasz wzorce | `03-design-patterns-and-heuristic.md` (sekcje `OBOWIĄZKOWY`) |
| Piszesz testy — dobór stylu per warstwa, buildery, custom assertions | `04-testing.md` |
| Listenery Kafki, idempotencja, cechy Javy 21, nazewnictwo semantyczne | `05-coding-guidelines.md` |
| Projektujesz/zmieniasz HTTP API: kontrakt OpenAPI, idempotencja API (`Idempotency-Key`), rate-limit, model błędów (katalog typów RFC 7807), paginacja, **zakaz kwot w żądaniu** | `06-api-contracts.md` (+ `docs/contracts/openapi/`, `data-contracts/`) |
| Projektujesz/zmieniasz zdarzenia Kafka: schemat, topologia/routing (**jeden schemat = jeden topic**), wersjonowanie, koperta CloudEvents | `07-event-contracts.md` (+ `docs/contracts/events/`) |

---

## Zanim uznasz zadanie za zakończone

- [ ] Kierunek zależności heksagonu zachowany; `domain/` bez frameworków.
- [ ] Pojęcia zgodne ze słownikiem (01); brak słów zakazanych.
- [ ] Kwoty przez `Money`; niezmienniki w agregacie/VO, nie w serwisie.
- [ ] Domena rzuca wyjątki, use case zwraca `Result`; brak duplikacji w infrastrukturze.
- [ ] Wzorce `OBOWIĄZKOWY` zastosowane tam, gdzie pasują (03).
- [ ] Testy w stylu właściwym dla warstwy (04); deterministyczne, na Testcontainers gdzie integracyjne.
- [ ] Zmiany w HTTP API / zdarzeniach zgodne z kontraktem (06/07): bez kwot w żądaniu, błędy wg katalogu RFC 7807, jeden schemat = jeden topic.
- [ ] Kod się kompiluje i testy przechodzą (uruchomione, nie założone).
