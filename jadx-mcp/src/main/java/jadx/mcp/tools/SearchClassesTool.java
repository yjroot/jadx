package jadx.mcp.tools;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import jadx.api.JavaClass;
import jadx.mcp.JadxMcpContext;
import jadx.mcp.JadxMcpServer;

public class SearchClassesTool {
	private static final String TOOL_NAME = "jadx_search_classes";
	private static final String DESCRIPTION = "Search for classes by pattern (supports wildcards: * and ?)";
	private static final String SCHEMA = "{"
			+ "\"type\": \"object\","
			+ "\"properties\": {"
			+ "\"pattern\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Search pattern with wildcards (e.g., *Activity, com.*.Main*)\""
			+ "},"
			+ "\"limit\": {"
			+ "\"type\": \"integer\","
			+ "\"description\": \"Maximum number of results (default: 50)\""
			+ "}"
			+ "},"
			+ "\"required\": [\"pattern\"]"
			+ "}";

	public static McpServerFeatures.SyncToolSpecification create(JadxMcpContext context) {
		return new McpServerFeatures.SyncToolSpecification(
				new McpSchema.Tool(TOOL_NAME, DESCRIPTION, SCHEMA),
				(exchange, args) -> {
					if (!context.isLoaded()) {
						return JadxMcpServer.errorResult("No file loaded. Use jadx_load_file first.");
					}

					String pattern = JadxMcpServer.getString(args, "pattern");
					if (pattern == null || pattern.isEmpty()) {
						return JadxMcpServer.errorResult("pattern is required");
					}

					int limit = JadxMcpServer.getInt(args, "limit", 50);

					try {
						String regex = wildcardToRegex(pattern);
						Pattern compiledPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

						List<String> matches = context.getClasses().stream()
								.map(JavaClass::getFullName)
								.filter(name -> compiledPattern.matcher(name).matches())
								.limit(limit)
								.collect(Collectors.toList());

						if (matches.isEmpty()) {
							return JadxMcpServer.successResult("No classes found matching: " + pattern);
						}

						String result = String.format("Found %d classes matching '%s':\n%s",
								matches.size(), pattern,
								String.join("\n", matches));
						return JadxMcpServer.successResult(result);
					} catch (Exception e) {
						return JadxMcpServer.errorResult("Search failed: " + e.getMessage());
					}
				});
	}

	private static String wildcardToRegex(String wildcard) {
		StringBuilder regex = new StringBuilder();
		for (char c : wildcard.toCharArray()) {
			switch (c) {
				case '*':
					regex.append(".*");
					break;
				case '?':
					regex.append(".");
					break;
				case '.':
				case '(':
				case ')':
				case '[':
				case ']':
				case '{':
				case '}':
				case '\\':
				case '^':
				case '$':
				case '|':
				case '+':
					regex.append("\\").append(c);
					break;
				default:
					regex.append(c);
			}
		}
		return regex.toString();
	}
}
