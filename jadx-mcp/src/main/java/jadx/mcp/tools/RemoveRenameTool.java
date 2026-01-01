package jadx.mcp.tools;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import jadx.api.data.IJavaNodeRef;
import jadx.mcp.JadxMcpContext;
import jadx.mcp.JadxMcpServer;

public class RemoveRenameTool {
	private static final String TOOL_NAME = "jadx_remove_rename";
	private static final String DESCRIPTION = "Remove an applied rename/alias";
	private static final String SCHEMA = "{"
			+ "\"type\": \"object\","
			+ "\"properties\": {"
			+ "\"class_name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Full class name (or the class containing the method/field)\""
			+ "},"
			+ "\"type\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Type of rename to remove: CLASS, METHOD, or FIELD\""
			+ "},"
			+ "\"member_name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Method or field name (optional, only for METHOD/FIELD types)\""
			+ "}"
			+ "},"
			+ "\"required\": [\"class_name\", \"type\"]"
			+ "}";

	public static McpServerFeatures.SyncToolSpecification create(JadxMcpContext context) {
		return new McpServerFeatures.SyncToolSpecification(
				new McpSchema.Tool(TOOL_NAME, DESCRIPTION, SCHEMA),
				(exchange, args) -> {
					if (!context.isLoaded()) {
						return JadxMcpServer.errorResult("No file loaded. Use jadx_load_file first.");
					}

					String className = JadxMcpServer.getString(args, "class_name");
					String typeStr = JadxMcpServer.getString(args, "type");
					String memberName = JadxMcpServer.getString(args, "member_name");

					if (className == null || className.isEmpty()) {
						return JadxMcpServer.errorResult("class_name is required");
					}
					if (typeStr == null || typeStr.isEmpty()) {
						return JadxMcpServer.errorResult("type is required");
					}

					IJavaNodeRef.RefType refType;
					try {
						refType = IJavaNodeRef.RefType.valueOf(typeStr.toUpperCase());
					} catch (IllegalArgumentException e) {
						return JadxMcpServer.errorResult("Invalid type. Use: CLASS, METHOD, or FIELD");
					}

					try {
						String shortId = null;
						if (refType == IJavaNodeRef.RefType.METHOD && memberName != null) {
							var method = context.findMethod(className, memberName);
							if (method != null) {
								shortId = context.getMethodShortId(method);
							}
						} else if (refType == IJavaNodeRef.RefType.FIELD && memberName != null) {
							var field = context.findField(className, memberName);
							if (field != null) {
								shortId = context.getFieldShortId(field);
							}
						}

						boolean removed = context.removeRename(className, refType, shortId);
						if (removed) {
							return JadxMcpServer.successResult("Rename removed successfully");
						} else {
							return JadxMcpServer.successResult("No matching rename found");
						}
					} catch (Exception e) {
						return JadxMcpServer.errorResult("Failed to remove rename: " + e.getMessage());
					}
				});
	}
}
