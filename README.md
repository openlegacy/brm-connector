# BRM Connector

Documentation about BRM:
[https://docs.oracle.com/cd/E16754_01/doc.75/e16702/prg_client_javapcm.htm#BRMDR768](https://docs.oracle.com/cd/E16754_01/doc.75/e16702/prg_client_javapcm.htm#BRMDR768)

Connector uses two jars from BRM SDK **pcmext.jar** and **pcm.jar** as Maven dependencies.
You can install them to your local maven repository using the following commands:

    mvn install:install-file -Dfile=pcmext.jar -DgroupId=com.portal -DartifactId=pcmext -Dversion=7.5 -Dpackaging=jar
    mvn install:install-file -Dfile=pcm.jar -DgroupId=com.portal -DartifactId=pcm -Dversion=7.5 -Dpackaging=jar
