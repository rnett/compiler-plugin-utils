allprojects {
    version = "1.1.1-SNAPSHOT"
    group = "com.github.rnett.compiler-plugin-utils"
    extra["isReleaseVersion"] = !version.toString().endsWith("SNAPSHOT")

    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots") {
            mavenContent { snapshotsOnly() }
        }
        jcenter()
    }
}
