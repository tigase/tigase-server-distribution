/*
 * Tigase XMPP Server Distribution - bootstrap configuration for all Tigase projects
 * Copyright (C) 2004 Tigase, Inc. (office@tigase.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipFile

String root = System.getProperty("user.dir");

def jarDir = "./target/dist/jars/"
def tigaseLibraries = Paths.get(root, jarDir)?.toFile()?.listFiles(new FileFilter() {

	@Override
	boolean accept(File pathname) {
		def filename = pathname.toPath().getFileName().toString()
		return (filename.endsWith(".jar") && (filename.startsWith("tigase-") || filename.startsWith("jaxmpp-"))) ||
				filename.equals("licence-lib.jar")
	}
})

AtomicInteger build = new AtomicInteger(0);

log.info("Found ${tigaseLibraries?.length} libraries")

tigaseLibraries.sort().each { file ->
	def zipFile = new ZipFile(file)
	def entry = zipFile.getEntry("META-INF/MANIFEST.MF")
	def reader = new InputStreamReader(zipFile.getInputStream(entry))
	def lines = reader.readLines()
	def implementationBuild = lines.find { line -> line.startsWith("Implementation-Build") }
	if (implementationBuild != null) {
		def buildNumber = implementationBuild.replaceAll("Implementation-Build: (\\d+)/.*", "\$1")
		try {
			if (Integer.valueOf(buildNumber) == 1) {
				log.warn("Incorrect dependency build number for source: ${file.getName()}: ${buildNumber} / implementation: ${implementationBuild}")
			} else {
				log.info("Adding version: ${buildNumber}  \timplementation: ${implementationBuild}, source ${file.getName()}")
			}
			build.addAndGet(Integer.valueOf(buildNumber))
		} catch (Exception e) {
			log.warn("Wrong format for: ${file.toPath().getFileName().toString()}: ${buildNumber}")
		}
	} else {
		log.warn("Missing implementation build for: ${file.toPath().getFileName().toString()}: ${implementationBuild}")
	}
}

buildVersionFile = Paths.get(root, "target", "build-version").toFile()
if (!buildVersionFile.exists()) {
	buildVersionFile.createNewFile()
}
buildVersionFile.write("b${build}")

log.info("setting revision to: " + build)
project.properties.setProperty('gitVersion', build.toString())

