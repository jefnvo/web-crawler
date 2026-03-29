# Web Crawler

A web crawler for a single-domain built in Java. Given a starting URL, it crawls every reachable page within the same subdomain, printing each visited URL and the links discovered on that page.

## Requirements

- Java 21+
- Maven 3.8+

## Architecture

The project follows hexagonal architecture with a domain layer isolated from infrastructure:

- **Domain** — crawl logic, frontier abstraction, scope enforcement, URI normalisation
- **Infra** — HTTP client (`java.net.http`), HTML parser (Jsoup), console reporter

Two crawl strategies are available, both using breadth-first traversal:

- **Sequential** - Single-thread BFS written just for validation and debugging. Useful for when peripheral aspects of the code needs to be changed and you have to debug it.
- **Concurrent** - Use java virtual threads to speed-up crawling. Since this app is I/O heavy you can pass the number of threads you want default is 1000, but be careful to not be throtled.

The traversal order (breadth, depth, hybrid, etc.) is controlled by the injected `Frontier`. Swap `BfsFrontier` for a `DfsFrontier` or `PriorityFrontier` without changing any strategy code.

## Compile

```bash
mvn package -DskipTests
```

This produces `target/web-crawler-1.0-SNAPSHOT.jar`.

## Run tests

```bash
mvn test
```

## Run

```bash
java -jar target/web-crawler-1.0-SNAPSHOT.jar <url> [faster [<threads>]]
```

### Parameters

- **url (required)**: The starting URL. Crawling is restricted to its subdomain.
- **faster (optional)**: Enables the concurrent strategy using virtual threads. If not specified, sequential will be used.
- **threads (optional)**: Maximum number of concurrent in-flight requests. Only valid with `faster`. Default: `1000`.

### Usage examples

```bash
# Sequential crawl (default)
java -jar target/web-crawler-1.0-SNAPSHOT.jar https://crawlme.monzo.com/

# Concurrent crawl with default thread limit (1000)
java -jar target/web-crawler-1.0-SNAPSHOT.jar https://crawlme.monzo.com/ faster

# Concurrent crawl with 50 concurrent requests
java -jar target/web-crawler-1.0-SNAPSHOT.jar https://crawlme.monzo.com/ faster 50
```
