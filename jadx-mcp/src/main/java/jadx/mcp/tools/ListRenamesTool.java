package jadx.mcp.tools;

import java.util.List;
import java.util.stream.Collectors;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import jadx.api.data.ICodeRename;
import jadx.api.data.IJavaNodeRef;
import jadx.mcp.JadxMcpContext;
import jadx.mcp.JadxMcpServer;

public class ListRenamesTool {
	private static final String TOOL_NAME = "jadx_list_renames";
	private static final String DESCRIPTION = "List all currently applied renames/aliases";
	private static final String SCHEMA = "{"
			+ "\"type\": \"object\","
			+ "\"properties\": {"
			+ "\"type\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Filter by type: CLASS, METHOD, FIELD, or PKG\""
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
					IJavaNodeRef.RefType refType = null;
					if (typeFilter != null && !typeFilter.isEmpty()) {
						try {
							refType = IJavaNodeRef.RefType.valueOf(typeFilter.toUpperCase());
						} catch (IllegalArgumentException e) {
							return JadxMcpServer.errorResult(
									"Invalid type. Use: CLASS, METHOD, FIELD, or PKG");
						}
					}

					List<ICodeRename> renames = context.getRenames();
					if (renames.isEmpty()) {
						return JadxMcpServer.successResult("No renames applied.");
					}

					final IJavaNodeRef.RefType filterType = refType;
					List<String> renameInfos = renames.stream()
							.filter(r -> filterType == null || r.getNodeRef().getType() == filterType)
							.map(r -> formatRename(r))
							.collect(Collectors.toList());

					if (renameInfos.isEmpty()) {
						return JadxMcpServer.successResult("No renames found for type: " + typeFilter);
					}

					String result = String.format("Renames (%d):\n%s",
							renameInfos.size(),
							String.join("\n", renameInfos));
					return JadxMcpServer.successResult(result);
				});
	}

	private static String formatRename(ICodeRename rename) {
		IJavaNodeRef nodeRef = rename.getNodeRef();
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(nodeRef.getType()).append("] ");
		sb.append(nodeRef.getDeclaringClass());
		if (nodeRef.getShortId() != null && !nodeRef.getShortId().isEmpty()) {
			sb.append(".").append(nodeRef.getShortId());
		}
		if (rename.getCodeRef() != null) {
			sb.append(" @").append(rename.getCodeRef().getAttachType());
			sb.append("[").append(rename.getCodeRef().getIndex()).append("]");
		}
		sb.append(" -> ").append(rename.getNewName());
		return sb.toString();
	}
}
