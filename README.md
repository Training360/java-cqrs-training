# CQRS Training - Course Enrollments

A CQRS esetén könnyű kijelenteni, hogy csak szétválasztjuk az írás és olvasási műveleteket.
Azonban a konkrét implementáció során több problémába is ütközhetük, különösen akkor, ha
külön folyamatként fut a két oldal, és közöttük aszinkron üzenetküldés van. Ekkor megjelenhetnek az 
elosztottságból adódó bizonytalanságok, mit lát a UI, mit lát egy másik párhuzamos lekérdezés, és mit 
lát egy másik párhuzamos módosítás (utóbbi kettő akár UI, akár másik microservice felől)?

Kell futó Kafka, mely a következőképp indítható:

```shell
cd kafka
docker compose up -d
```

Az adatbázis H2, ahhoz a konzol: http://localhost:8080/h2-console

## Kiindulási projekt

* Feladat
* DDD
* Request -> Command
* Entity
* Repository interfész a domain rétegben
* Infrastructure, JPA
* Olvasáskor DTO-zás


## Projection query

* application rétegben a query interfész

## Event

* CQRS metódus szinten: Egy objektum metódusa vagy command, vagy query (CQS – Bertrand Meyer)
* Greg Young szoftverarchitekt
    * DDD gondolatkörben
    * CQRS és Event Sourcing
* Miért nem cache?
* Külön read és write model
* Eventual consistent - nincs tranzakció - állapotgép
* Projection lag
* Domain events
* Gyűjti az entity
* A saved entitásba nem kerül bele
* Spring Cloud Stream
* `CourseEnrollmentCountJpaEntity` entitás és repository
    * Write repository (command side) → domain réteg
    * Read repository (query side) → application / infrastructure réteg (nem domain!)
    * Denormalizálás
    * JPA? -> NoSQL
* RepositoryAdapter ebből olvas
* TraceID - observability

## Transactional outbox

* Repo
* Tábla
* Transactional inbox
* Eltűnik a TraceID
    * implementálni kell, el kell tárolni
    * publish során a headerbe kell tenni
    * query oldalon pedig ki kell olvasni a headerből, és el kell tárolni az MDC-ben, hogy a logokban is megjelenjen

## Idempotens fogadás

Fogadó oldal idempotens, tehát nem a különbséget kell elküldeni, hanem az összes jelentkezők számát

## Felhasználói felület felől retry

* Retry: felhasználó, felhasználói felület, API gateway/load balancer/reverse proxy
* Enrollment, cancallation is legyen idempotens - aggregate szintű idempotencia
* Hibajelenség: nyom egy módosítást, nem jön még vissza, hogy sikeres, és nyom még egyet
* Idempotency key


## Felhasználói felület

* Ghost state -> UI megelőlegezi a változást, de nem megy körbe a folyamat
* Kör
* Tracking token vs. verziózás - read barrier
* Version az entitásban, és a query oldalon is
* save helyett saveAndFlush
* ráadásul külön kell átadni, mert az eventben még nincs benne
* Polling
    * ETag, hálózati forgalom optimalizáció
    * Polling timeout: hibaüzenet, alerting


## Mi a helyzet a párhuzamos felületi kérésekkel

* Régebbit láthat
* Konzisztenst, ha nem kérdez be a write modelbe
* Módosítás ugyanaz, mint a normál esetben, optimistic/pessimistic lock

## Külön microservice

* Külön skálázható
* Komplex logika megkövetelheti
* API gateway

## Mi a helyzet más microservice-ekkel?

* Nem olvashat! Event! Saga!

## Event sourcing

* Command jön, eventet tárolunk (ez utóbbi a tény, az előbbi csak szándék)
* Betöltjük az eventeket
* Snapshot - optimalizálás
* Aggregate szinten egy szál, szigorú sorrend
* Több aggregate párhuzamos
* Olvasás csak a read modellből

