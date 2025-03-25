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

The `jitpack.yml` in the root of the project ensures only the **dapm-pipeline** project folder will be build and available.
```
jdk:
  - openjdk21
install:
  cd dapm-pipeline && mvn install -DskipTests
  ```

### Get started with Jitpack (build)
If you want to be able to push new builds to jitpack, follow the following steps:
1. Go to [jitpack.io](https://jitpack.io/) and click **sign in**.
2. Go to your profile and click **Authorize Private Repos**
3. Grant jitpack access to dapm-thesis organization.
4. Go to [github.com](https://github.com/) -> **Settings** -> **Developer settings** -> **Personal access tokens** -> **Tokens (classic)**
5. Generate new token (classic) and give it these permissions:
    - repo
    - read:packages
6. Generate the token and make sure to **COPY** it.
7. Now go to your folder *`C:\Users\user\.m2`* or *`$HOME\.m2`*.
8. Check if there is a file called `settings.xml` if it is not there, create it.
9. Add this to your `settings.xml` file:
```
<servers>
    <server>
        <id>jitpack.io</id>
        <username>YOUR_GIT_USERNAME</username>
        <password>YOUR_AUTH_TOKEN</password>
    </server>
</servers>
```
Make sure to replace with your username and token.
10. To push a new build to jitpack, you have to use a tag. Run the following:
```
git tag v1.0.0
git push origin v1.0.0
```
Make sure to update the tag version with a newer version than the previous one. If there is no new tag set, there will be no new build pushed to jitpack.

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

<dependency>
    <groupId>com.github.dapm-thesis</groupId>
    <artifactId>dapm-pipeline</artifactId>
    <version>LATEST</version>
</dependency>
```
Do a `mvn clean install` after.