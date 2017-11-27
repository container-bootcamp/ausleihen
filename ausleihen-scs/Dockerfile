FROM hseeberger/scala-sbt as build
WORKDIR /root
COPY . /root
RUN sbt universal:packageBin
RUN unzip target/universal/ausleihen-0.0.1.zip 

FROM anapsix/alpine-java
WORKDIR /ausleihen
COPY --from=build /root/ausleihen-0.0.1 /ausleihen/
ENTRYPOINT ["bin/ausleihen"]