plugins {
	id("jadx-java")
	id("application")
}

dependencies {
	implementation(project(":jadx-core"))
	implementation(project(":jadx-plugins-tools"))
	implementation(project(":jadx-commons:jadx-app-commons"))

	runtimeOnly(project(":jadx-plugins:jadx-dex-input"))
	runtimeOnly(project(":jadx-plugins:jadx-java-input"))
	runtimeOnly(project(":jadx-plugins:jadx-java-convert"))
	runtimeOnly(project(":jadx-plugins:jadx-smali-input"))
	runtimeOnly(project(":jadx-plugins:jadx-rename-mappings"))
	runtimeOnly(project(":jadx-plugins:jadx-kotlin-metadata"))
	runtimeOnly(project(":jadx-plugins:jadx-xapk-input"))
	runtimeOnly(project(":jadx-plugins:jadx-aab-input"))
	runtimeOnly(project(":jadx-plugins:jadx-apkm-input"))
	runtimeOnly(project(":jadx-plugins:jadx-apks-input"))

	// MCP SDK
	implementation("io.modelcontextprotocol.sdk:mcp:0.10.0")

	implementation("ch.qos.logback:logback-classic:1.5.22")
}

application {
	applicationName = "jadx-mcp"
	mainClass.set("jadx.mcp.JadxMcpServer")
	applicationDefaultJvmArgs =
		listOf(
			"-Xms128M",
			"-XX:MaxRAMPercentage=70.0",
			"-Djdk.util.zip.disableZip64ExtraFieldValidation=true",
			"--enable-native-access=ALL-UNNAMED",
		)
}
