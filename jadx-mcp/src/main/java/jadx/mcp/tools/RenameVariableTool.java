package jadx.mcp.tools;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import jadx.api.JavaClass;
import jadx.api.JavaMethod;
import jadx.mcp.JadxMcpContext;
import jadx.mcp.JadxMcpServer;

public class RenameVariableTool {
	private static final String TOOL_NAME = "jadx_rename_variable";
	private static final String DESCRIPTION = "Rename a method argument or local variable";
	private static final String SCHEMA = "{"
			+ "\"type\": \"object\","
			+ "\"properties\": {"
			+ "\"class_name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Full class name containing the method\""
			+ "},"
			+ "\"method_name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Method name containing the variable\""
			+ "},"
			+ "\"arg_index\": {"
			+ "\"type\": \"integer\","
			+ "\"description\": \"Argument index (0-based) - use this for method arguments\""
			+ "},"
			+ "\"reg_num\": {"
			+ "\"type\": \"integer\","
			+ "\"description\": \"Register number - use this with ssa_version for local variables\""
			+ "},"
			+ "\"ssa_version\": {"
			+ "\"type\": \"integer\","
			+ "\"description\": \"SSA version number - use this with reg_num for local variables\""
			+ "},"
			+ "\"new_name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"New name for the variable\""
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

					int argIndex = JadxMcpServer.getInt(args, "arg_index", -1);
					int regNum = JadxMcpServer.getInt(args, "reg_num", -1);
					int ssaVersion = JadxMcpServer.getInt(args, "ssa_version", -1);

					try {
						String shortId = context.getMethodShortId(method);

						if (argIndex >= 0) {
							// Rename method argument
							context.renameMethodArg(className, shortId, argIndex, newName);
							return JadxMcpServer.successResult(
									String.format("Renamed argument %d of '%s.%s' to '%s'",
											argIndex, className, methodName, newName));
						} else if (regNum >= 0 && ssaVersion >= 0) {
							// Rename local variable
							context.renameVariable(className, shortId, regNum, ssaVersion, newName);
							return JadxMcpServer.successResult(
									String.format("Renamed variable (reg=%d, ssa=%d) in '%s.%s' to '%s'",
											regNum, ssaVersion, className, methodName, newName));
						} else {
							return JadxMcpServer.errorResult(
									"Either arg_index OR (reg_num and ssa_version) must be provided");
						}
					} catch (Exception e) {
						return JadxMcpServer.errorResult("Failed to rename: " + e.getMessage());
					}
				});
	}
}
