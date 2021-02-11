allprojects {
    version = "0.1.0"

    group = "com.github.rnett.compiler-plugin-utils"
    extra["isReleaseVersion"] = !version.toString().endsWith("SNAPSHOT")

    repositories {
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        mavenCentral()
        maven("https://oss.jfrog.org/artifactory/oss-snapshot-local/")
        google()
        jcenter()
        mavenLocal()
        maven("https://oss.sonatype.org/content/repositories/snapshots") {
            mavenContent { snapshotsOnly() }
        }
    }

    setProperty("GROUP", group)
    setProperty("POM_ARTIFACT_ID", name)
    setProperty("VERSION_NAME", version)
}
