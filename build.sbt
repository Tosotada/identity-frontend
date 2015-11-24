name := "identity-frontend"

organization := "com.gu.identity"

scalaVersion := "2.11.7"

version := "1.0.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtNativePackager, UniversalPlugin, RiffRaffArtifact)

libraryDependencies ++= Seq(
  "org.scalatestplus" %% "play" % "1.4.0-M3",
  ws,
  filters,
  "jp.co.bizreach" %% "play2-handlebars" % "0.3.0",
  "com.mohiva" %% "play-html-compressor" % "0.5.0"
)

// Include handlebars views in resources
unmanagedResourceDirectories in Compile += baseDirectory.value / "app" / "views"

// Config for packaging app for deployment with riffraff
packageName in Universal := normalizedName.value

riffRaffPackageType := (packageZipTarball in Universal).value
riffRaffPackageName := name.value
riffRaffManifestProjectName := s"identity:${name.value}"
riffRaffBuildIdentifier := Option(System.getenv("CIRCLE_BUILD_NUM")).getOrElse("DEV")
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")


// Disable packaging of scaladoc
sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false


play.PlayImport.PlayKeys.playDefaultPort := 8860
routesGenerator := InjectedRoutesGenerator

addCommandAlias("devrun", "run -Dlogs.home=logs")
