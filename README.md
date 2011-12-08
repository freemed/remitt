# REMITT Electronic Medical Information Translation and Transmission

* Website: http://remitt.org
* Development: https://github.com/freemed/remitt
* Wiki: https://github.com/freemed/remitt/wiki

## What is REMITT?

(It is a recursive acronym.)

It is a system for translating documents from one format to another,
then transmitting them. It was designed to be used for medical billing
and claim submission using XML files. It uses XSLT and XML, and is
written in Java as a J2EE application.

## How do I install it?

This application should be able to be dropped into a Servlet container
like Apache Tomcat and run with little to no configuration. The
system property "remitt.properties" can point at a configuration file to
override default config properties which are defined inside the war
file.

(In Debian or Ubuntu packaged tomcat instances, you would add
`-Dremitt.properties=/path/to/my/remitt.properties` to the options passed
to tomcat in `/etc/defaults/tomcat` or `/etc/defaults/tomcat55`, depending on
the version you're using.)

You can also run it from source using Maven with
`mvn -Dorg.mortbay.jetty.Request.maxFormContentSize=6000000 jetty:run`, or
simply `mvn jetty:run` if you're not going to be testing large documents
in the test harness.

The only prerequisite for installing this software is importing the
database definitions from sql/*.sql into your working database server.

At the moment, MySQL is the supported / preferred database server. The
remitt user should probably have all privileges on the remitt database,
but also SELECT on mysql.proc (otherwise you'll need
"noAccessToProcedureBodies=true" in the JDBC URL).

## Remote Services Information

REST services are exposed at /remitt/services/rest/service/(functionname)
and SOAP services are available through the WSDL at
/remitt/services/interface?wsdl

## Who is responsible for this thing?

The primary author is Jeff Buchbinder, and the ownership of the code
resides with the [FreeMED Software Foundation](http://freemedsoftware.org/).


