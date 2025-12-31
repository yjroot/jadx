package jadx.mcp.tools;

import java.util.List;
import java.util.stream.Collectors;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import jadx.api.JavaClass;
import jadx.api.JavaMethod;
import jadx.mcp.JadxMcpContext;
import jadx.mcp.JadxMcpServer;

public class ListMethodsTool {
	private static final String TOOL_NAME = "jadx_list_methods";
	private static final String DESCRIPTION = "List all methods in a specific class";
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

					List<JavaMethod> methods = javaClass.getMethods();
					if (methods.isEmpty()) {
						return JadxMcpServer.successResult("No methods found in " + className);
					}

					List<String> methodInfos = methods.stream()
							.map(m -> formatMethod(m))
							.collect(Collectors.toList());

					String result = String.format("Methods in %s (%d):\n%s",
							className, methods.size(),
							String.join("\n", methodInfos));
					return JadxMcpServer.successResult(result);
				});
	}

	private static String formatMethod(JavaMethod method) {
		StringBuilder sb = new StringBuilder();
		sb.append(method.getAccessFlags().makeString(false));
		sb.append(method.getReturnType());
		sb.append(" ");
		sb.append(method.getName());
		sb.append("(");
		sb.append(method.getArguments().stream()
				.map(Object::toString)
				.collect(Collectors.joining(", ")));
		sb.append(")");
		return sb.toString();
	}
}
