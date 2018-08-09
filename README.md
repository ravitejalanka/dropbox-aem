# dropbox-aem
Dropbox Integration
========

This a content package project generated using the multimodule-content-package-archetype.

Building
--------

This project uses Maven for building. Common commands:

From the root directory, run ``mvn -PautoInstallPackage clean install`` to build the bundle and content package and install to a CQ instance.

From the bundle directory, run ``mvn -PautoInstallBundle clean install`` to build *just* the bundle and install to a CQ instance.

Using with VLT
--------------

To use vlt with this project, first build and install the package to your local CQ instance as described above. Then cd to `content/src/main/content/jcr_root` and run

    vlt --credentials admin:admin checkout -f ../META-INF/vault/filter.xml --force http://localhost:4502/crx

Once the working copy is created, you can use the normal ``vlt up`` and ``vlt ci`` commands.

Specifying CRX Host/Port
------------------------

The CRX host and port can be specified on the command line with:
mvn -Dcrx.host=otherhost -Dcrx.port=5502 <goals>

Use Case

Consider a scenario where a company uploads thousands of document related to its policies or some other stuff in AEM. It would be an overhead for AEM to manage those documents resulting in the degradation of performance since AEM is already managing a lot of tasks.

Solution

One way of managing documents and avoid overhead in AEM is to use document management systems like Dropbox and Google Drive. On the AEM side, we would have some way to make those documents available for view/download purpose.

Step1:

Sign up in dropbox and create a developer app in dropbox using the following link:

https://www.dropbox.com/developers/apps/create

For our integration Dropbox with AEM, we would be requiring the Access Token ( You need Click on “Generate Token” button to get the Access token. The reason we are generating the token from the dropbox portal only so that we can skip the manual flow of OAuth2. For OAuth2 flow you can access OAuth2.0 )

Step2: Adding dependencies related to dropbox. I have used the maven build tool.  Add the following dependency to the project’s pom.xml

    <dependency>
    <groupId>com.dropbox.core</groupId>
    <artifactId>dropbox-core-sdk</artifactId>
    <version>1.7.7</version>
    </dependency>

Step3:

Create the dropbox config page using the template . You can go to “http://<domain>:<port-no>/miscadmin#/etc/cloudservices” and create the page. The page would be created under the directory in CRXDE:
