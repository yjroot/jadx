package jadx.mcp;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;

import jadx.mcp.tools.GetClassCodeTool;
import jadx.mcp.tools.GetFileInfoTool;
import jadx.mcp.tools.GetMethodCodeTool;
import jadx.mcp.tools.GetResourceTool;
import jadx.mcp.tools.ListClassesTool;
import jadx.mcp.tools.ListMethodsTool;
import jadx.mcp.tools.ListRenamesTool;
import jadx.mcp.tools.ListResourcesTool;
import jadx.mcp.tools.LoadFileTool;
import jadx.mcp.tools.RemoveRenameTool;
import jadx.mcp.tools.RenameClassTool;
import jadx.mcp.tools.RenameFieldTool;
import jadx.mcp.tools.RenameMethodTool;
import jadx.mcp.tools.RenameVariableTool;
import jadx.mcp.tools.SearchClassesTool;
import jadx.mcp.tools.SearchMethodsTool;

public class JadxMcpServer {
	private static final String SERVER_NAME = "jadx-mcp";
	private static final String SERVER_VERSION = "1.0.0";

	public static void main(String[] args) {
		JadxMcpContext context = new JadxMcpContext();

		ObjectMapper objectMapper = new ObjectMapper();
		StdioServerTransportProvider transport = new StdioServerTransportProvider(objectMapper);

		McpSyncServer server = McpServer.sync(transport)
				.serverInfo(SERVER_NAME, SERVER_VERSION)
				.capabilities(ServerCapabilities.builder()
						.tools(true)
						.build())
				.build();

		registerTools(server, context);

		Runtime.getRuntime().addShutdownHook(new Thread(context::close));
	}

	private static void registerTools(McpSyncServer server, JadxMcpContext context) {
		// File operations
		server.addTool(LoadFileTool.create(context));
		server.addTool(GetFileInfoTool.create(context));

		// Class/Method browsing
		server.addTool(ListClassesTool.create(context));
		server.addTool(GetClassCodeTool.create(context));
		server.addTool(ListMethodsTool.create(context));
		server.addTool(GetMethodCodeTool.create(context));

		// Search
		server.addTool(SearchClassesTool.create(context));
		server.addTool(SearchMethodsTool.create(context));

		// Resources
		server.addTool(ListResourcesTool.create(context));
		server.addTool(GetResourceTool.create(context));

		// Rename/Alias
		server.addTool(RenameClassTool.create(context));
		server.addTool(RenameMethodTool.create(context));
		server.addTool(RenameFieldTool.create(context));
		server.addTool(RenameVariableTool.create(context));
		server.addTool(ListRenamesTool.create(context));
		server.addTool(RemoveRenameTool.create(context));
	}

	public static McpSchema.CallToolResult successResult(String message) {
		return new McpSchema.CallToolResult(
				List.of(new McpSchema.TextContent(message)),
				false);
	}

	public static McpSchema.CallToolResult errorResult(String message) {
		return new McpSchema.CallToolResult(
				List.of(new McpSchema.TextContent("Error: " + message)),
				true);
	}

	public static String getString(Map<String, Object> args, String key) {
		Object value = args.get(key);
		return value != null ? value.toString() : null;
	}

	public static int getInt(Map<String, Object> args, String key, int defaultValue) {
		Object value = args.get(key);
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		return defaultValue;
	}
}
