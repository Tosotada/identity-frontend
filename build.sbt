import scala.sys.process._
import com.typesafe.sbt.packager.MappingsHelper.{contentOf, directory}

name := "identity-frontend"

organization := "com.gu.identity"

scalaVersion := "2.12.4"

version := "1.0.0-SNAPSHOT"

lazy val `identity-frontend` = (project in file(".")).enablePlugins(PlayScala, UniversalPlugin, RiffRaffArtifact, BuildInfoPlugin)

lazy val functionalTests = Project("functional-tests", file("functional-tests"))

resolvers += "Guardian Github Releases" at "https://guardian.github.io/maven/repo-releases"

val identityLibrariesVersion = "3.140"
val akkaVersion = "2.5.11"
val playJsonVersion = "2.6.8"
val awsSdkVersion = "1.11.293"

libraryDependencies ++= Seq(
  ws,
  filters,
  "org.scalatestplus.play"          %%  "scalatestplus-play"        %   "3.1.2"   %   Test,
  "com.typesafe.play"               %%  "play-json"                 %   playJsonVersion,
  "com.typesafe.play"               %%  "play-json-joda"            %   playJsonVersion,
  "jp.co.bizreach"                  %%  "play2-handlebars"          %   "0.4.3",
  "com.mohiva"                      %%  "play-html-compressor"      %   "0.7.1",
  "com.typesafe.akka"               %%  "akka-actor"                %   akkaVersion,
  "com.typesafe.akka"               %%  "akka-slf4j"                %   akkaVersion,
  "com.gu.identity"                 %%  "identity-cookie"           %   identityLibrariesVersion,
  "com.gu.identity"                 %%  "identity-model"            %   identityLibrariesVersion,
  "com.gu"                          %%  "tip"                       %   "0.3.3",
  "com.amazonaws"                   %   "aws-java-sdk-cloudwatch"   %   awsSdkVersion,
  "com.getsentry.raven"             %   "raven-logback"             %   "8.0.3",
  "com.googlecode.libphonenumber"   %   "libphonenumber"            %   "7.2.4",

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
riffRaffArtifactResources += (file("cloudformation/identity-frontend.yaml"), "update-cloudformation/identity-frontend.yaml")

// FIXME: riffraff should automatically detect these but it seems tc-build.sh is interfering with that
riffRaffBuildIdentifier := Option(System.getenv("BUILD_NUMBER")).getOrElse("unknown")
riffRaffManifestBranch := Option(System.getenv("BRANCH_NAME")).getOrElse("unknown") // %teamcity.build.branch%

// Prout
def commitId(): String = try {
  "git rev-parse HEAD".!!.trim
} catch {
  case _: Exception => "unknown"
}

buildInfoKeys := Seq[BuildInfoKey](
  name,
  BuildInfoKey.constant("gitCommitId", Option(System.getenv("BUILD_VCS_NUMBER")).getOrElse(commitId())),
  BuildInfoKey.constant("buildNumber", Option(System.getenv("BUILD_NUMBER")).getOrElse("DEV"))
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

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:postfixOps",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xfatal-warnings",
  "-Ywarn-unused:implicits",
  "-Ywarn-unused:params",
  "-unchecked",
  "-Xlint:unsound-match",
  "-Xlint:nullary-override",
  "-Ywarn-nullary-unit",
  "-Yno-adapted-args"
)

