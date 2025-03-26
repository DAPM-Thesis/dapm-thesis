# dapm-thesis
The shared GIT repo is for enabling collaboration between the three dapm groups:
1. Reshma Zaman, Tama Sarker
2. Christian Becke, Zou Yong Nan Klaassen
3. Hussein Dirani, Raihanullah Mehran
## Projects
Every folder is a separate project with its own pom.xml file!
1. **dapm-pipeline**
    - This project contains all the template code for creating processing elements: source, operator, sink. Pipeline and channel communication.
2. **dapm-pipeline-execution**
    - A project which uses the *dapm-pipeline* project as a dependency. It uses the templates to create the processing elements and connect them using the pipelinebuilder.

## Jitpack
The [dapm-pipeline](#projects) project is a shared project between the groups. This project is available on [jitpack.io](https://jitpack.io/) and can be used as a dependency in other projects.

The `jitpack.yml` in the root of the project ensures only the **dapm-pipeline** project folder will be included in the build.
```
jdk:
  - openjdk21
install:
  cd dapm-pipeline && mvn install -DskipTests
  ```

### Get started with Jitpack (build)
If you want to be able to push new builds to jitpack, follow the following steps:
1. To push a new build to jitpack, you have to use a tag. Run the following:
```
git tag v1.0.0
git push origin v1.0.0
```
Make sure to update the tag version with a newer version than the previous one. If there is no new tag set, there will be no new build pushed to jitpack.

The new version is now available on jitpack <a href="https://jitpack.io/#DAPM-Thesis/dapm-thesis" target="_blank">View here</a>


### Get started with Jitpack (dependency)
If you want to use the jitpack build in another project, follow the following steps:
1. In your project's `pom.xml` maven file, add:
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.DAPM-Thesis</groupId>
        <artifactId>dapm-thesis</artifactId>
        <version>v1.0.0</version>
    </dependency>
</dependencies>
```
Remember to update the version with the latest.
Do a `mvn clean install` after.