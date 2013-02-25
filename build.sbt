name := "MyWhisper"        

version := "0.1"        

scalaVersion := "2.10.0" 

seq(webSettings :_*)    

seq(lessSettings:_*)

scalacOptions ++= Seq("-unchecked", "-deprecation")

resolvers += "bone" at "http://bone.twbbs.org.tw/ivy"

libraryDependencies ++= Seq(
    "javax.servlet" % "servlet-api" % "2.5" % "provided",
    "org.eclipse.jetty" % "jetty-webapp" % "8.0.1.v20110908" % "container",
    "net.liftweb" %% "lift-webkit" % "2.5-M4" % "compile->default",
    "net.liftweb" %% "lift-squeryl-record" % "2.5-M4",
    "org.bone" %% "soplurk" % "0.1"
)

(LessKeys.filter in (Compile, LessKeys.less)) := ("custom.less")

(resourceManaged in (Compile, LessKeys.less)) <<= 
    (sourceDirectory in Compile)(_ / "webapp" / "asset" / "css")

(compile in Compile) <<= compile in Compile dependsOn (LessKeys.less in Compile)


