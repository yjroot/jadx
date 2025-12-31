package jadx.mcp.tools;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import jadx.api.JavaClass;
import jadx.api.JavaMethod;
import jadx.mcp.JadxMcpContext;
import jadx.mcp.JadxMcpServer;

public class GetMethodCodeTool {
	private static final String TOOL_NAME = "jadx_get_method_code";
	private static final String DESCRIPTION = "Get the decompiled code for a specific method";
	private static final String SCHEMA = "{"
			+ "\"type\": \"object\","
			+ "\"properties\": {"
			+ "\"class_name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Full class name (e.g., com.example.MainActivity)\""
			+ "},"
			+ "\"method_name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Method name to retrieve\""
			+ "}"
			+ "},"
			+ "\"required\": [\"class_name\", \"method_name\"]"
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

					if (className == null || className.isEmpty()) {
						return JadxMcpServer.errorResult("class_name is required");
					}
					if (methodName == null || methodName.isEmpty()) {
						return JadxMcpServer.errorResult("method_name is required");
					}

					JavaClass javaClass = context.findClass(className);
					if (javaClass == null) {
						return JadxMcpServer.errorResult("Class not found: " + className);
					}

					JavaMethod targetMethod = null;
					for (JavaMethod method : javaClass.getMethods()) {
						if (method.getName().equals(methodName)) {
							targetMethod = method;
							break;
						}
					}

					if (targetMethod == null) {
						return JadxMcpServer.errorResult(
								String.format("Method '%s' not found in class '%s'", methodName, className));
					}

					try {
						// Get method code from class code
						javaClass.getCode(); // Ensure class is decompiled
						String methodCode = targetMethod.getCodeStr();
						if (methodCode == null || methodCode.isEmpty()) {
							return JadxMcpServer.successResult("// Method body not available");
						}
						return JadxMcpServer.successResult(methodCode);
					} catch (Exception e) {
						return JadxMcpServer.errorResult("Failed to get method code: " + e.getMessage());
					}
				});
	}
}
