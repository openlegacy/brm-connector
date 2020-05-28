
# BRM Connector  
  
Documentation about BRM:  
[https://docs.oracle.com/cd/E16754_01/doc.75/e16702/prg_client_javapcm.htm#BRMDR768](https://docs.oracle.com/cd/E16754_01/doc.75/e16702/prg_client_javapcm.htm#BRMDR768)  

---  
Connector uses two jars from BRM SDK **pcmext.jar** and **pcm.jar** as Maven dependencies.  
  
You can install them to your local maven repository using the following commands:  
  
    mvn install:install-file -Dfile=pcmext.jar -DgroupId=com.portal -DartifactId=pcmext -Dversion=7.5 -Dpackaging=jar 
    mvn install:install-file -Dfile=pcm.jar -DgroupId=com.portal -DartifactId=pcm -Dversion=7.5 -Dpackaging=jar

---  
BRM connector is based on the OpenLegacy version _4.7.0-SNAPSHOT_.  
  
In order to re-compile BRM connector, you need to place its sources in the _openlegacy-provider_ module inside _openlegacy-core_ repository, _develop_ branch.  
  
For example:  
   
    /openlegacy-core/openlegacy-core-parent/openlegacy-providers/brm-connector  
Without doing it, Maven cannot compile Kotlin sources and run _ktlint_ checks.  
  
---  
BRM connector uses _LIVE_ tests which connects to the BRM system test environment.  
  
At the time of writing, the test environment doesn't have public access and can be accessed only via ssh connection.  
  
In order to run LIVE test (which connects to _localhost:11960_), you need firstly to establish ssh connection to BRM test environment with **local port forwarding**.  
  
Example:  
  
    ssh -i PrivateKey.pem -L 11960:<brm-inner-ip>:11960 login@hostname.com
    
After that, BRM system will be accessible to access by unit tests via pcp://localhost:11960

---
