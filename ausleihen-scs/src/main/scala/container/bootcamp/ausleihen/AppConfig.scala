package container.bootcamp.ausleihen

import com.typesafe.config.ConfigFactory


/**
  * Holds all the configuration stuff
  */
object AppConfig {

  private val config = ConfigFactory.load()

  object ContainerBootcampAusleihenConfig {
    val cbaConfig = config.getConfig("container.bootcamp.ausleihen")
    val cbaPort = cbaConfig.getInt("port")
  }

  object ContainerBootcampEinbuchenConfig {
    val cbeConfig = config.getConfig("container.bootcamp.einbuchen")
    val cbeUrl = cbeConfig.getString("url")
  }

  object ContainerBootcampReservierenConfig {
    val cbrConfig = config.getConfig("container.bootcamp.reservieren")
    val cbrUrl = cbrConfig.getString("url")
  }

}
