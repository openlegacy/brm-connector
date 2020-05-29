# BRM Connector  
  
Documentation about BRM:  
[https://docs.oracle.com/cd/E16754_01/doc.75/e16702/prg_client_javapcm.htm#BRMDR768](https://docs.oracle.com/cd/E16754_01/doc.75/e16702/prg_client_javapcm.htm#BRMDR768)  

## Required dependencies
Connector uses two jars from BRM SDK **pcmext.jar** and **pcm.jar** as Maven dependencies.  

You can install them to your local maven repository using the following commands:  
  
    mvn install:install-file -Dfile=pcmext.jar -DgroupId=com.portal -DartifactId=pcmext -Dversion=7.5 -Dpackaging=jar 
    mvn install:install-file -Dfile=pcm.jar -DgroupId=com.portal -DartifactId=pcm -Dversion=7.5 -Dpackaging=jar

## OpenLegacy version
BRM connector is based on the OpenLegacy version _4.7.0-SNAPSHOT_.  
  
In order to re-compile BRM connector, you need to place its sources in the _openlegacy-provider_ module inside _openlegacy-core_ repository, _develop_ branch.  
  
For example:  
   
    /openlegacy-core/openlegacy-core-parent/openlegacy-providers/brm-connector  
Without doing it, Maven cannot compile Kotlin sources and run _ktlint_ checks.  
  
## Connection properties
All connection properties are declared in the class `io.ol.provider.brm.properties.OLBrmProperties.ProjectBrmProperties`

Below is an example of  the `application.yml` with all available connection properties and comment for each property:

    ol:
      brm:
        project:
          <OpenLegacy SDK project Name>:
            host: localhost
            port: 11960
            username: "root.0.0.0.1"
            password: "password"
            service: "/service/admin_client"
            # the time in milliseconds to wait before the BRM client will drop the request.
            timeout: 5000
            # Login type: 0 or 1. The default is 1. 
            # A type 1 login requires the application to provide a username and password. 
            # A type 0 login is a trusted login that comes through a CM Proxy, for example, and does not require a username and password in the properties.
            login_type: 1
            # A type 0 login requires a full POID (Portal Object ID) of the service 
            # A type 1 login uses a default value 1 
            service_poid: 1
            # Required when Login type is 0. 
            # The number assigned to BRM database when the BRM Data Manager was installed. 
            database_no: ""

The user could use already predefined ***Infranet.properties*** file which is commonly used for specifying connection details in the BRM system, instead of all connection properties specified above. 
To do this, the user needs to specify  the full path to the external *Infranet.properties* file in the application.yml file.
In this case, all required BRM properties will be loaded from the specified file, ignoring BRM connection properties specified in the OpenLegacy properties. 
It could be useful in case when needed more fine-tuning for the BRM connection when the standard BRM connection properties specified in OpenLegacy properties are not enough. 
Or in a case, when pre-configured Infranet.properties file re-used across several applications. 

    ol:
      brm:
        project:
          <OpenLegacy SDK project Name>:
            infranet_properties_file_path: "/usr/local/brm/Infranet.properties"

## Specifying Opcode and Opcode flag

Opcode and opcode flag could be specified as the RpcEntity/RpcOperation path.
The opcode is mandatory and could be represented in the following formats:
- For default opcodes as opcode constant name, e.g. *PCM_OP_CUST_FIND* 
- For custom and default opcodes as opcode integer value e.g. *51*

The opcode flag is optional, represented by integer number and could be added after the opcode separated by ":"  in the format `<Opcode>:<OpcodeFlag>`, e.g. *PCM_OP_CUST_FIND:32*

## Connection to Test Environment
BRM connector uses _LIVE_ tests which connects to the BRM system test environment.  
  
At the time of writing, the test environment doesn't have public access and can be accessed only via ssh connection.  
  
In order to run LIVE test (which connects to _localhost:11960_), you need firstly to establish ssh connection to BRM test environment with **local port forwarding**.  
  
Example:  
  
    ssh -i PrivateKey.pem -L 11960:<brm-inner-ip>:11960 login@hostname.com
    
After that, the BRM system will be accessible by unit tests via *pcp://localhost:11960*
