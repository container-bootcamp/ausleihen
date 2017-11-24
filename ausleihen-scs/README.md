# Ausleihen-SCS

SCS für das Ausleihen und Zurückgeben von Büchern.

## Funktionsweise

Der Ausleihen-SCS hört auf Server-Send-Events vom Einbuchen-SCS. Neue Bücher werden in einer eigenen 
Persistenz (Postgres) gespeichert. Schreibende und lesende Zugriffe auf die Persistenz sind durch 
[akka-persistence](https://doc.akka.io/docs/akka/current/scala/persistence.html) und 
[akka-persistence-query](https://doc.akka.io/docs/akka/current/scala/persistence-query.html) getrennt.


