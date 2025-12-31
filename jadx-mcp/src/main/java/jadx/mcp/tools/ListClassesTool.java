package jadx.mcp.tools;

import java.util.List;
import java.util.stream.Collectors;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import jadx.api.JavaClass;
import jadx.mcp.JadxMcpContext;
import jadx.mcp.JadxMcpServer;

public class ListClassesTool {
	private static final String TOOL_NAME = "jadx_list_classes";
	private static final String DESCRIPTION = "List all classes in the loaded file";
	private static final String SCHEMA = "{"
			+ "\"type\": \"object\","
			+ "\"properties\": {"
			+ "\"package\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Filter by package name (e.g., com.example)\""
			+ "},"
			+ "\"limit\": {"
			+ "\"type\": \"integer\","
			+ "\"description\": \"Maximum number of classes to return (default: 100)\""
			+ "}"
			+ "}"
			+ "}";

	public static McpServerFeatures.SyncToolSpecification create(JadxMcpContext context) {
		return new McpServerFeatures.SyncToolSpecification(
				new McpSchema.Tool(TOOL_NAME, DESCRIPTION, SCHEMA),
				(exchange, args) -> {
					if (!context.isLoaded()) {
						return JadxMcpServer.errorResult("No file loaded. Use jadx_load_file first.");
					}

					String packageFilter = JadxMcpServer.getString(args, "package");
					int limit = JadxMcpServer.getInt(args, "limit", 100);

					List<JavaClass> classes = context.getClasses();
					List<String> classNames = classes.stream()
							.map(JavaClass::getFullName)
							.filter(name -> packageFilter == null || name.startsWith(packageFilter))
							.limit(limit)
							.collect(Collectors.toList());

					if (classNames.isEmpty()) {
						return JadxMcpServer.successResult("No classes found.");
					}

					String result = String.format("Classes (%d of %d):\n%s",
							classNames.size(), classes.size(),
							String.join("\n", classNames));
					return JadxMcpServer.successResult(result);
				});
	}
}
