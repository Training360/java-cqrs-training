# CQRS Training - Course Enrollments

* Write repository (command side) → domain réteg
* Read repository (query side) → application / infrastructure réteg (nem domain!)

Transactional outbox

Fogadó oldal idempotens, tehát nem a különbséget kell elküldeni, hanem az összes jelentkezők számát

Transactional outbox esetén elveszik a Correlation ID

* implementálni kell, el kell tárolni
* publish során a headerbe kell tenni
* query oldalon pedig ki kell olvasni a headerből, és el kell tárolni az MDC-ben, hogy a logokban is megjelenjen

Enrollment és cancellation legyen idempotens