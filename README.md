# Ausleihen

## Kurzbeschreibung
Buch anzeigen und ausleihen.

## Voraussetzung

### Entwicklung 

* docker installiert
* java installiert
* sbt installiert

### Docker image erzeugen

* docker installiert

## Einstiegspunkte 

* [Bootcamp](http://bootcamp.ch.innoq.io)
* [Bootcamp Tracing](http://bootcamp-tracing.ch.innoq.io)

## Module

### Ausleihen-SCS

SCS für das Ausleihen von Büchern

### dev-proxy

Nginx-Konfiguration um den auf dem Cluster verwendeten ausleihen ingess
zu simulieren.


#### Warnung

* Akutell funktioniert das Erzeugen der ausleihen DB nur als SuperUser.

### kubernetes

Deployment-Scripte für Kubernetes

#### kube-deploy

##### ConfigMap

Die [ConfigMap](https://kubernetes.io/docs/tasks/configure-pod-container/configmap/) 
enthält alle benötigten Parameter für den Ausleihen-SCS

##### Service

Ein [Service](https://kubernetes.io/docs/concepts/services-networking/service/) 
ist immer von aussen erreichbar, er leitet die Anfragen auf den akutell laufenden Ausleihen-Pod weiter.

##### Deployment

Das [Deployment](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/) beschreibt
die [ReplicaSets](https://kubernetes.io/docs/concepts/workloads/controllers/replicaset/) 
und [Pods](https://kubernetes.io/docs/concepts/workloads/pods/pod/)
und überwacht die Verfügbarkeit. 

##### Ingress

[Ingress](https://kubernetes.io/docs/concepts/services-networking/ingress/) aktiviert für den Ausleihen-SCS
die Basic-Auth und leitet die Anfragen die mit ```/ausleihen``` beginnen an den Ausleihen-SCS weiter.

#### kube-sshd-deploy

Die Infrastrukturdienste wie z.B. Postgres oder MongoDb laufen in einem
separaten kubernetes namespace auf den nicht zugegriffen werden kann.
Daher funktioniert z.B. ein kubectl port-forward auf die Postgres nicht. 

Um dennoch auf die Daten zuzugreifen, kann über den Umweg mittels eines
installierten sshd-Pod's auf die Daten zugegriffen werden.

1. Mittels ```kubectl create -f kube-sshd-deploy.yaml``` den sshd Pod installieren
2. SSH Tunnel zum SSH-Pod aufbauen mit ````kubectl port-forward [ausleihen-sshd....] [3333]:22````
3. Einen SSH Tunnel zur Datenbank durch den bestehenden SSH-Tunnel aufbauen
   1. Mit ````ssh -L [4444]:pgpool.infrastruktur:5432 -p [3333] root@localhost```` wird ein ssh-Tunnel
   zur Postgres aufgebaut.
   2. Mit ````ssh -L [4445]:mongo.infrastruktur:27017 -p [3333] root@localhost```` wird ein ssh-Tunnel
   zur MongoDb aufgebaut.
5. Mit den entsprechenden Datenbanktools kann sich nun an die Datenbank verbunden werden. In diesem
   Beispiel Port 4444 für die Postgres und Port 4445 für die MongoDB 
   
##### Hinweise

* Das Password für den Tunnel gibt es beim Bootcamp-Trainer oder selbst ein docker sshd image bauen.
* Aus Sicherheitsgründen nur temporär verwenden.
* Funktioniert möglicherweise nur solange keine 
  [Network-Policies](https://kubernetes.io/docs/concepts/services-networking/network-policies/) eingerichtet sind.

