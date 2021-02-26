import scala.collection.immutable.ListMap

val extraResolvers =
  ListMap(
    "confluent" -> "https://packages.confluent.io/maven/",
  )

resolvers in ThisBuild ++=
  extraResolvers.map { case (name, location) => name.at(location) }.toSeq
