name := "Backstab"

version := "0.1"        

scalaVersion := "2.11.12" 

seq(webSettings :_*)    

seq(lessSettings:_*)

scalacOptions ++= Seq("-unchecked", "-deprecation")

resolvers += "bone" at "http://brianhsu.moe/ivy"

// Plurk Scala Binding Library
libraryDependencies += "org.bone" %% "soplurk" % "0.3.6"

// Java HTTP Servlet
libraryDependencies ++= Seq(
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.eclipse.jetty" % "jetty-webapp" % "8.0.1.v20110908" % "container"
)

// Lift Web Framework
libraryDependencies ++= Seq(
  "net.liftweb" %% "lift-webkit" % "2.6.3" % "compile->default",
  "net.liftmodules" %% "combobox" % "2.6-RC2-0.6"
)

(LessKeys.filter in (Compile, LessKeys.less)) := ("custom.less")

(resourceManaged in (Compile, LessKeys.less)) <<= 
    (sourceDirectory in Compile)(_ / "webapp" / "assets" / "css")

(compile in Compile) <<= compile in Compile dependsOn (LessKeys.less in Compile)


