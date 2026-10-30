
# Fascicolo Sanitario 2.0

# _it-fse-gtw-eds-client_

Il microservizio in oggetto ha la responsabilità di gestire la comunicazione verso l'EDS.

<br/>

## Repos
- [*it-fse-support*](https://github.com/ministero-salute/it-fse-support) - Pagina di supporto FSE 2.0
- [*it-fse-landing*](https://github.com/ministero-salute/it-fse-landing) - Landing page dei repository FSE 2.0

<br/>

## Isolation tests

With Java 21, run the isolated suite without MongoDB, Kafka or HTTP services:

```sh
mvn -PISOLATION clean test
```

Tests live in `src/test/java/it/finanze/sanita/fse2/ms/edsclient/isolation/`.
`@Tag("ISOLATION")` selects them only with the Maven profile; normal `mvn test`
excludes them and leaves the existing tests unchanged. `@ActiveProfiles("ISOLATION")`
activates the Spring profile for narrow `@SpringJUnitConfig` contexts; it does not
select tests. Do not extend `AbstractTest` or start the full application.

The suite exercises real publication services, Mongo query construction, MVC
controllers and exception mapping, broker/config HTTP request construction,
configuration caching, JWT/metadata utilities and logging delivery. Only external
boundaries are mocked (MongoTemplate, RestTemplate, broker/config clients and
Kafka Producer). Success and dependency-failure paths are covered.

JaCoCo reports: `target/site/jacoco/index.html` and `target/site/jacoco/jacoco.xml`.
Use `clean` to avoid stale reports.
These tests do not establish real connectivity or full application startup.

## License

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
