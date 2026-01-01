package jadx.mcp.tools;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import jadx.api.JavaClass;
import jadx.mcp.JadxMcpContext;
import jadx.mcp.JadxMcpServer;

public class RenameClassTool {
	private static final String TOOL_NAME = "jadx_rename_class";
	private static final String DESCRIPTION = "Rename a class to a new alias name";
	private static final String SCHEMA = "{"
			+ "\"type\": \"object\","
			+ "\"properties\": {"
			+ "\"class_name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Full class name to rename (e.g., com.example.MainActivity)\""
			+ "},"
			+ "\"new_name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"New alias name for the class (short name only, e.g., RenamedActivity)\""
			+ "}"
			+ "},"
			+ "\"required\": [\"class_name\", \"new_name\"]"
			+ "}";

	public static McpServerFeatures.SyncToolSpecification create(JadxMcpContext context) {
		return new McpServerFeatures.SyncToolSpecification(
				new McpSchema.Tool(TOOL_NAME, DESCRIPTION, SCHEMA),
				(exchange, args) -> {
					if (!context.isLoaded()) {
						return JadxMcpServer.errorResult("No file loaded. Use jadx_load_file first.");
					}

					String className = JadxMcpServer.getString(args, "class_name");
					String newName = JadxMcpServer.getString(args, "new_name");

					if (className == null || className.isEmpty()) {
						return JadxMcpServer.errorResult("class_name is required");
					}
					if (newName == null || newName.isEmpty()) {
						return JadxMcpServer.errorResult("new_name is required");
					}

					JavaClass javaClass = context.findClass(className);
					if (javaClass == null) {
						return JadxMcpServer.errorResult("Class not found: " + className);
					}

					try {
						context.renameClass(className, newName);
						return JadxMcpServer.successResult(
								String.format("Renamed class '%s' to '%s'", className, newName));
					} catch (Exception e) {
						return JadxMcpServer.errorResult("Failed to rename: " + e.getMessage());
					}
				});
	}
}
