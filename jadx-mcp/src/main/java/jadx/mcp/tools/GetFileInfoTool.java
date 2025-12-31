package jadx.mcp.tools;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import jadx.mcp.JadxMcpContext;
import jadx.mcp.JadxMcpServer;

public class GetFileInfoTool {
	private static final String TOOL_NAME = "jadx_get_file_info";
	private static final String DESCRIPTION = "Get information about the currently loaded file";
	private static final String SCHEMA = "{"
			+ "\"type\": \"object\","
			+ "\"properties\": {}"
			+ "}";

	public static McpServerFeatures.SyncToolSpecification create(JadxMcpContext context) {
		return new McpServerFeatures.SyncToolSpecification(
				new McpSchema.Tool(TOOL_NAME, DESCRIPTION, SCHEMA),
				(exchange, args) -> {
					if (!context.isLoaded()) {
						return JadxMcpServer.errorResult("No file loaded. Use jadx_load_file first.");
					}

					String result = String.format(
							"Loaded file: %s\nClasses: %d\nResources: %d",
							context.getLoadedFilePath(),
							context.getClassCount(),
							context.getResourceCount());
					return JadxMcpServer.successResult(result);
				});
	}
}
