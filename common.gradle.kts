allprojects {
    version = "1.0.1"
    extra["kotlinVersion"] = "1.5.20"
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
