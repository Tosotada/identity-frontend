logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.18")

addSbtPlugin("com.gu" % "sbt-riffraff-artifact" % "0.9.9")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")
