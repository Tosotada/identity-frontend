import com.typesafe.sbt.packager.MappingsHelper.{contentOf, directory}

name := "identity-frontend"

organization := "com.gu.identity"

scalaVersion := "2.11.8"

version := "1.0.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, UniversalPlugin, RiffRaffArtifact, BuildInfoPlugin)

lazy val functionalTests = Project("functional-tests", file("functional-tests"))

resolvers += "Guardian Github Releases" at "https://guardian.github.io/maven/repo-releases"

val identityLibrariesVersion = "3.140"

libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % "test",
  "org.mockito" % "mockito-core" % "1.9.5" % "test",
  ws,
  filters,
  "jp.co.bizreach" %% "play2-handlebars" % "0.3.1",
  "com.mohiva" %% "play-html-compressor" % "0.6.3",
  "com.gu.identity" %% "identity-cookie" % identityLibrariesVersion,
  "com.gu.identity" %% "identity-model" % identityLibrariesVersion,
  "com.typesafe.akka" %% "akka-actor" % "2.4.1",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.0",
  "com.amazonaws" % "aws-java-sdk-cloudwatch" % "1.10.54",
  "com.getsentry.raven" % "raven-logback" % "7.2.1",
  "com.googlecode.libphonenumber" % "libphonenumber" % "7.2.4",
  "com.gu" %% "tip" % "0.3.2"
)

// Set logs options and default local resource for running locally (run and test)
javaOptions ++= Seq("-Dlogs.home=logs", "-Dconfig.resource=DEV.conf")

testOptions in Test += Tests.Argument("-oDF")

// RiffRaff
packageName in Universal := name.value
mappings in Universal ++= directory("deploy")
riffRaffPackageType := (packageBin in Universal).value
riffRaffPackageName := name.value
riffRaffManifestProjectName := s"identity:${name.value}"
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")

// FIXME: riffraff should automatically detect these but it seems tc-build.sh is interfering with that
riffRaffBuildIdentifier := Option(System.getenv("BUILD_NUMBER")).getOrElse("unknown")
riffRaffManifestBranch := Option(System.getenv("BRANCH_NAME")).getOrElse("unknown") // %teamcity.build.branch%

// Prout
buildInfoKeys := Seq[BuildInfoKey](
  name,
  BuildInfoKey.constant("gitCommitId", Option(System.getenv("BUILD_VCS_NUMBER")) getOrElse (try {
    "git rev-parse HEAD".!!.trim
  } catch { case e: Exception => "unknown" })),
  BuildInfoKey.constant("buildNumber", Option(System.getenv("BUILD_NUMBER")) getOrElse "DEV")
)

buildInfoOptions += BuildInfoOption.ToMap

// Disable packaging of scaladoc
sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false


PlayKeys.devSettings := Seq("play.server.http.port" -> "8860")
routesGenerator := InjectedRoutesGenerator

addCommandAlias("devrun", "run")

// Include handlebars views in resources for lookup on classpath
unmanagedResourceDirectories in Compile += (resourceDirectory in Assets).value

mappings in Assets ++= contentOf(baseDirectory.value / "target/web/build-npm")

// enable asset fingerprinting
pipelineStages := Seq(digest)
mappings in Assets ++= contentOf(baseDirectory.value / "target/web/digest")

