plugins {
    // See https://jmfayard.github.io/refreshVersions
    id("de.fayard.refreshVersions") version "0.51.0"
}

refreshVersions {

}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("api-endpoint:common")
include("api-endpoint:server-jvm")
include("api-endpoint:client-html")

include("blog")

include("framework")
include("framework:assert")
include("framework:crashhandler")
include("framework:core")
include("framework:core-compose")
include("framework:halt")
include("framework:interfacelib")
include("framework:interfacelib-test")
include("framework:logging")
include("framework:metrics")
include("framework:userevents")
include("framework:preferences")
include("framework:remoteconfig")
include("framework:thread")
include("framework:test")
include("framework:utils")

include("petproject:app")
include("petproject:ui")
include("petproject:appcore")
include("petproject:azurefunction")

include("doom:lib")
include("doom:lib:sample")

include("doom:game:core")
include("doom:game:desktop")

include("kotlinlibs:common")
include("kotlinlibs:frontend")
include("kotlinlibs:server-jvm")
include("kotlinlibs:lambda-jvm")
include("kotlinlibs:spring-common")
include("kotlinlibs:cdk")

include("auraxiscontrolcenter:app")
include("auraxiscontrolcenter:appcore")
include("auraxiscontrolcenter:cdk")
include("auraxiscontrolcenter:core-models")
include("auraxiscontrolcenter:db-models")
include("auraxiscontrolcenter:deployable-models")
include("auraxiscontrolcenter:network-models")
include("auraxiscontrolcenter:streaming-client")
include("auraxiscontrolcenter:streaming-client:testgui")
include("auraxiscontrolcenter:ui")

include("stranded:lib")
include("stranded:gdx")
include("stranded:gdx:desktop")
include("stranded:gdx:core")
include("stranded:cardmanager")
include("stranded:testgui")
include("stranded:server")
include("stranded:server:demogame")
include("stranded:web")

include("framework-samples:android-app")
include("framework-samples:android-lib")
include("framework-samples:mpp-lib")
include("framework-samples:jvm-lib")
include("framework-samples:js-lib")

include("cdk-repo")
