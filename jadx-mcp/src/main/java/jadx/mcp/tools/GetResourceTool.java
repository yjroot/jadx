package jadx.mcp.tools;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import jadx.api.ResourceFile;
import jadx.core.xmlgen.ResContainer;
import jadx.mcp.JadxMcpContext;
import jadx.mcp.JadxMcpServer;

public class GetResourceTool {
	private static final String TOOL_NAME = "jadx_get_resource";
	private static final String DESCRIPTION = "Get the content of a specific resource (XML files are decoded)";
	private static final String SCHEMA = "{"
			+ "\"type\": \"object\","
			+ "\"properties\": {"
			+ "\"name\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Resource name (e.g., AndroidManifest.xml, res/values/strings.xml)\""
			+ "}"
			+ "},"
			+ "\"required\": [\"name\"]"
			+ "}";

	public static McpServerFeatures.SyncToolSpecification create(JadxMcpContext context) {
		return new McpServerFeatures.SyncToolSpecification(
				new McpSchema.Tool(TOOL_NAME, DESCRIPTION, SCHEMA),
				(exchange, args) -> {
					if (!context.isLoaded()) {
						return JadxMcpServer.errorResult("No file loaded. Use jadx_load_file first.");
					}

					String name = JadxMcpServer.getString(args, "name");
					if (name == null || name.isEmpty()) {
						return JadxMcpServer.errorResult("name is required");
					}

					ResourceFile resource = context.getResources().stream()
							.filter(r -> r.getOriginalName().equals(name)
									|| r.getOriginalName().endsWith("/" + name)
									|| r.getDeobfName().equals(name))
							.findFirst()
							.orElse(null);

					if (resource == null) {
						return JadxMcpServer.errorResult("Resource not found: " + name);
					}

					try {
						ResContainer content = resource.loadContent();
						if (content == null) {
							return JadxMcpServer.errorResult("Failed to load resource content");
						}

						switch (content.getDataType()) {
							case TEXT:
								return JadxMcpServer.successResult(content.getText().getCodeStr());
							case DECODED_DATA:
								byte[] data = content.getDecodedData();
								return JadxMcpServer.successResult(
										String.format("[Binary data: %d bytes]", data.length));
							case RES_TABLE:
								return JadxMcpServer.successResult("[Resource table - use specific resource path]");
							default:
								return JadxMcpServer.successResult(
										"[Unsupported resource type: " + content.getDataType() + "]");
						}
					} catch (Exception e) {
						return JadxMcpServer.errorResult("Failed to read resource: " + e.getMessage());
					}
				});
	}
}
