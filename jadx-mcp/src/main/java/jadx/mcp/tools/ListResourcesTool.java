package jadx.mcp.tools;

import java.util.List;
import java.util.stream.Collectors;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import jadx.api.ResourceFile;
import jadx.mcp.JadxMcpContext;
import jadx.mcp.JadxMcpServer;

public class ListResourcesTool {
	private static final String TOOL_NAME = "jadx_list_resources";
	private static final String DESCRIPTION = "List all resources in the loaded file";
	private static final String SCHEMA = "{"
			+ "\"type\": \"object\","
			+ "\"properties\": {"
			+ "\"type\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Filter by resource type (MANIFEST, XML, ARSC, IMG, or other)\""
			+ "},"
			+ "\"limit\": {"
			+ "\"type\": \"integer\","
			+ "\"description\": \"Maximum number of resources to return (default: 100)\""
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

					String typeFilter = JadxMcpServer.getString(args, "type");
					int limit = JadxMcpServer.getInt(args, "limit", 100);

					List<ResourceFile> resources = context.getResources();
					List<String> resourceInfos = resources.stream()
							.filter(r -> typeFilter == null || r.getType().name().equalsIgnoreCase(typeFilter))
							.limit(limit)
							.map(r -> String.format("[%s] %s", r.getType(), r.getOriginalName()))
							.collect(Collectors.toList());

					if (resourceInfos.isEmpty()) {
						return JadxMcpServer.successResult("No resources found.");
					}

					String result = String.format("Resources (%d of %d):\n%s",
							resourceInfos.size(), resources.size(),
							String.join("\n", resourceInfos));
					return JadxMcpServer.successResult(result);
				});
	}
}
