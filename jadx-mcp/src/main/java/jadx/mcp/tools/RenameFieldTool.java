package jadx.mcp.tools;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import jadx.api.JavaClass;
import jadx.api.JavaField;
import jadx.mcp.JadxMcpContext;
import jadx.mcp.JadxMcpServer;

public class RenameFieldTool {
	private static final String TOOL_NAME = "jadx_rename_field";
	private static final String DESCRIPTION = "Rename a field to a new alias name";
	private static final String SCHEMA = "{"
			+ "\"type\": \"object\","
			+ "\"properties\": {"
			+ "\"class_name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Full class name containing the field\""
			+ "},"
			+ "\"field_name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Current field name to rename\""
			+ "},"
			+ "\"new_name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"New alias name for the field\""
			+ "}"
			+ "},"
			+ "\"required\": [\"class_name\", \"field_name\", \"new_name\"]"
			+ "}";

	public static McpServerFeatures.SyncToolSpecification create(JadxMcpContext context) {
		return new McpServerFeatures.SyncToolSpecification(
				new McpSchema.Tool(TOOL_NAME, DESCRIPTION, SCHEMA),
				(exchange, args) -> {
					if (!context.isLoaded()) {
						return JadxMcpServer.errorResult("No file loaded. Use jadx_load_file first.");
					}

					String className = JadxMcpServer.getString(args, "class_name");
					String fieldName = JadxMcpServer.getString(args, "field_name");
					String newName = JadxMcpServer.getString(args, "new_name");

					if (className == null || className.isEmpty()) {
						return JadxMcpServer.errorResult("class_name is required");
					}
					if (fieldName == null || fieldName.isEmpty()) {
						return JadxMcpServer.errorResult("field_name is required");
					}
					if (newName == null || newName.isEmpty()) {
						return JadxMcpServer.errorResult("new_name is required");
					}

					JavaClass javaClass = context.findClass(className);
					if (javaClass == null) {
						return JadxMcpServer.errorResult("Class not found: " + className);
					}

					JavaField field = context.findField(className, fieldName);
					if (field == null) {
						return JadxMcpServer.errorResult(
								String.format("Field '%s' not found in class '%s'", fieldName, className));
					}

					try {
						String shortId = context.getFieldShortId(field);
						context.renameField(className, shortId, newName);
						return JadxMcpServer.successResult(
								String.format("Renamed field '%s.%s' to '%s'", className, fieldName, newName));
					} catch (Exception e) {
						return JadxMcpServer.errorResult("Failed to rename: " + e.getMessage());
					}
				});
	}
}
