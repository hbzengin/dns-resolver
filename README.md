# DNS Resolver in Java

This repo contains a custom DNS resolve in Java that allows for both recursive and iterative DNS resolution. The resolver handles DNS message parsing, network messages, and name resolution following [RFC 1035](https://datatracker.ietf.org/doc/html/rfc1035) spec. It includes support for A records, NS records, CNAME records. It also implements DNS message compression for efficient packet handling, since most servers respond this way.

Recursive queries delegate resolution to recursive DNS servers like Google's 8.8.8.8 and iterative queries perform step-by-step resolution starting from root nameservers. When doing iterative resolution, if any CNAME record is seen, the resolution starts over from root nameservers with the canonical name. For nameserver lookups, if no 'A' record is present in the additionals section for a 'NS' record, the resolver falls back to recursive resolution.

## Features

- Supports both recursive and iterative DNS resolution
- Supports A, NS, and CNAME record types (can be extended)
- Follows [RFC 1035](https://datatracker.ietf.org/doc/html/rfc1035) in structure
- Handles DNS name compression using pointer offsets
- Automatically follows CNAME records during iterative resolution

## Usage

The resolver "knows" the query mode based on the recursion desired (RD) flag in the DNS header. Recursive queries (`rd=true`) are fairly straight-forward and DNS server handles the resolution. Iterative queries (`rd=false`) are handled by nameserver traversal. The `Main.java` class has a demo by resolving the same domain using both resolution methods. You can run the resolver using Maven or directly with Java.

```bash
mvn compile exec:java -Dexec.mainClass="com.hbzengin.dnsresolver.Main"
```

### Code Examples

Create a DNS query and resolve them either recursively or iteratively

```java
// Create a DNS message for recursive resolution
DnsHeader header = new DnsHeader();
// Pick some ID
header.setId((int) (Math.random() * 65_536));
// Enable recursion
header.setRd(true);
DnsQuestion question = new DnsQuestion("www.example.com", RecordType.A, RecordClass.IN);
DnsMessage query = new DnsMessage(header, List.of(question));

// Resolve using recursive mode (Google's Recursive DNS)
DnsResolver recursiveResolver = new DnsResolver("8.8.8.8");
DnsMessage response = recursiveResolver.resolve(query);

// Resolve using iterative mode (starting from a root nameserver)
// Disable recursion
header.setRd(false);
DnsResolver iterativeResolver = new DnsResolver("198.41.0.4");
DnsMessage iterativeResponse = iterativeResolver.resolve(query);
```
