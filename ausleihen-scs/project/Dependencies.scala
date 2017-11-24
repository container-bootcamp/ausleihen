import sbt._

object Dependencies {

  /*
   * Test dependencies
   */
  private object Test {
    private lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % "test"
    lazy val testDeps = Seq(scalaTest)
  }

  /*
   * Version definitions
   */
  private val akkaHttpVersion = "10.0.10"
  private val akkaVersion = "2.4.19"
  private val jacksonVersion = "2.8.6"
  private val jaegerVersion = "0.21.0"
  private val slickVersion = "3.2.0"

  /*
   * Compile dependencies
   */
  private object Compile {

    private val akkaHttp =  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
    private val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
    private val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
    private val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % akkaVersion
    private val akkaQuery = "com.typesafe.akka" %% "akka-persistence-query-experimental" % "2.4.20"
    private val akkaHttpPersistencePg = "be.wegenenverkeer" %% "akka-persistence-pg" % "0.6.0"
    private val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
    private val jacksonScalaModule =  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
    private val sseClient = "com.lightbend.akka" %% "akka-stream-alpakka-sse" % "0.14"
    private val openTracing = "io.opentracing" % "opentracing-api" % "0.30.0"
    private val openTracingImpl = "com.uber.jaeger" % "jaeger-core" % jaegerVersion
    private val slf4jLoggingImpl = "ch.qos.logback" % "logback-classic" % "1.2.3"
    private val slf4jAkka = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
    private val slick = "com.typesafe.slick" %% "slick" % slickVersion
    private val slickhikaricp = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion

    lazy val compileDeps = Seq(
      akkaHttp,
      akkaPersistence,
      akkaQuery,
      akkaHttpPersistencePg,
      jacksonDatabind,
      jacksonScalaModule,
      sseClient,
      openTracing,
      openTracingImpl,
      slf4jLoggingImpl,
      slf4jAkka,
      slick,
      slickhikaricp
    )
  }

  import Test._
  import Compile._

  lazy val dependencies = testDeps ++ compileDeps

}
