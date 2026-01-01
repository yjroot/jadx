package jadx.mcp.tools;

import java.io.File;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import jadx.mcp.JadxMcpContext;
import jadx.mcp.JadxMcpServer;

public class LoadFileTool {
	private static final String TOOL_NAME = "jadx_load_file";
	private static final String DESCRIPTION = "Load an APK, DEX, JAR, AAB, or .jadx project file for decompilation";
	private static final String SCHEMA = "{"
			+ "\"type\": \"object\","
			+ "\"properties\": {"
			+ "\"path\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Absolute path to the input file\""
			+ "}"
			+ "},"
			+ "\"required\": [\"path\"]"
			+ "}";

	public static McpServerFeatures.SyncToolSpecification create(JadxMcpContext context) {
		return new McpServerFeatures.SyncToolSpecification(
				new McpSchema.Tool(TOOL_NAME, DESCRIPTION, SCHEMA),
				(exchange, args) -> {
					String path = JadxMcpServer.getString(args, "path");
					if (path == null || path.isEmpty()) {
						return JadxMcpServer.errorResult("path is required");
					}

					File file = new File(path);
					if (!file.exists()) {
						return JadxMcpServer.errorResult("File not found: " + path);
					}

					try {
						context.loadFile(path);
						String result = String.format(
								"File loaded successfully: %s\nClasses: %d\nResources: %d",
								path, context.getClassCount(), context.getResourceCount());
						return JadxMcpServer.successResult(result);
					} catch (Exception e) {
						return JadxMcpServer.errorResult("Failed to load file: " + e.getMessage());
					}
				});
	}
}
