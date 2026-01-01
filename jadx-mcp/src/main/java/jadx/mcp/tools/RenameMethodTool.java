package jadx.mcp.tools;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import jadx.api.JavaClass;
import jadx.api.JavaMethod;
import jadx.mcp.JadxMcpContext;
import jadx.mcp.JadxMcpServer;

public class RenameMethodTool {
	private static final String TOOL_NAME = "jadx_rename_method";
	private static final String DESCRIPTION = "Rename a method to a new alias name";
	private static final String SCHEMA = "{"
			+ "\"type\": \"object\","
			+ "\"properties\": {"
			+ "\"class_name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Full class name containing the method\""
			+ "},"
			+ "\"method_name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Current method name to rename\""
			+ "},"
			+ "\"new_name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"New alias name for the method\""
			+ "}"
			+ "},"
			+ "\"required\": [\"class_name\", \"method_name\", \"new_name\"]"
			+ "}";

	public static McpServerFeatures.SyncToolSpecification create(JadxMcpContext context) {
		return new McpServerFeatures.SyncToolSpecification(
				new McpSchema.Tool(TOOL_NAME, DESCRIPTION, SCHEMA),
				(exchange, args) -> {
					if (!context.isLoaded()) {
						return JadxMcpServer.errorResult("No file loaded. Use jadx_load_file first.");
					}

					String className = JadxMcpServer.getString(args, "class_name");
					String methodName = JadxMcpServer.getString(args, "method_name");
					String newName = JadxMcpServer.getString(args, "new_name");

					if (className == null || className.isEmpty()) {
						return JadxMcpServer.errorResult("class_name is required");
					}
					if (methodName == null || methodName.isEmpty()) {
						return JadxMcpServer.errorResult("method_name is required");
					}
					if (newName == null || newName.isEmpty()) {
						return JadxMcpServer.errorResult("new_name is required");
					}

					JavaClass javaClass = context.findClass(className);
					if (javaClass == null) {
						return JadxMcpServer.errorResult("Class not found: " + className);
					}

					JavaMethod method = context.findMethod(className, methodName);
					if (method == null) {
						return JadxMcpServer.errorResult(
								String.format("Method '%s' not found in class '%s'", methodName, className));
					}

					try {
						String shortId = context.getMethodShortId(method);
						context.renameMethod(className, shortId, newName);
						return JadxMcpServer.successResult(
								String.format("Renamed method '%s.%s' to '%s'", className, methodName, newName));
					} catch (Exception e) {
						return JadxMcpServer.errorResult("Failed to rename: " + e.getMessage());
					}
				});
	}
}
