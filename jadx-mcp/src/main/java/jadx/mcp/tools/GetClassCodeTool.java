package jadx.mcp.tools;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import jadx.api.JavaClass;
import jadx.mcp.JadxMcpContext;
import jadx.mcp.JadxMcpServer;

public class GetClassCodeTool {
	private static final String TOOL_NAME = "jadx_get_class_code";
	private static final String DESCRIPTION = "Get the decompiled Java source code for a specific class";
	private static final String SCHEMA = "{"
			+ "\"type\": \"object\","
			+ "\"properties\": {"
			+ "\"class_name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Full class name (e.g., com.example.MainActivity)\""
			+ "}"
			+ "},"
			+ "\"required\": [\"class_name\"]"
			+ "}";

	public static McpServerFeatures.SyncToolSpecification create(JadxMcpContext context) {
		return new McpServerFeatures.SyncToolSpecification(
				new McpSchema.Tool(TOOL_NAME, DESCRIPTION, SCHEMA),
				(exchange, args) -> {
					if (!context.isLoaded()) {
						return JadxMcpServer.errorResult("No file loaded. Use jadx_load_file first.");
					}

					String className = JadxMcpServer.getString(args, "class_name");
					if (className == null || className.isEmpty()) {
						return JadxMcpServer.errorResult("class_name is required");
					}

					JavaClass javaClass = context.findClass(className);
					if (javaClass == null) {
						return JadxMcpServer.errorResult("Class not found: " + className);
					}

					try {
						String code = javaClass.getCode();
						return JadxMcpServer.successResult(code);
					} catch (Exception e) {
						return JadxMcpServer.errorResult("Failed to decompile: " + e.getMessage());
					}
				});
	}
}
