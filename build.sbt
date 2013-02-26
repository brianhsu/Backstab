name := "Backstab"

version := "0.1"        

scalaVersion := "2.10.0" 

seq(webSettings :_*)    

seq(lessSettings:_*)

scalacOptions ++= Seq("-unchecked", "-deprecation")

resolvers += "bone" at "http://bone.twbbs.org.tw/ivy"

// Plurk Scala Binding Library
libraryDependencies += "org.bone" %% "soplurk" % "0.1"

// Java HTTP Servlet
libraryDependencies ++= Seq(
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.eclipse.jetty" % "jetty-webapp" % "8.0.1.v20110908" % "container"
)

// Lift Web Framework
libraryDependencies ++= Seq(
  "net.liftweb" %% "lift-webkit" % "2.5-RC1" % "compile->default",
  "net.liftmodules" %% "combobox" % "2.5-RC1-0.2"
)

(LessKeys.filter in (Compile, LessKeys.less)) := ("custom.less")

(resourceManaged in (Compile, LessKeys.less)) <<= 
    (sourceDirectory in Compile)(_ / "webapp" / "asset" / "css")

(compile in Compile) <<= compile in Compile dependsOn (LessKeys.less in Compile)


