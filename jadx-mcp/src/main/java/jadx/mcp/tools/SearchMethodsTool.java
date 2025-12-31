package jadx.mcp.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import jadx.api.JavaClass;
import jadx.api.JavaMethod;
import jadx.mcp.JadxMcpContext;
import jadx.mcp.JadxMcpServer;

public class SearchMethodsTool {
	private static final String TOOL_NAME = "jadx_search_methods";
	private static final String DESCRIPTION = "Search for methods by name pattern across all classes";
	private static final String SCHEMA = "{"
			+ "\"type\": \"object\","
			+ "\"properties\": {"
			+ "\"pattern\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Method name pattern with wildcards (e.g., get*, on*Click)\""
			+ "},"
			+ "\"class_pattern\": {"
			+ "\"type\": \"string\","
			+ "\"description\": \"Optional class name pattern to filter (e.g., *Activity)\""
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

					String classPattern = JadxMcpServer.getString(args, "class_pattern");
					int limit = JadxMcpServer.getInt(args, "limit", 50);

					try {
						Pattern methodPattern = Pattern.compile(
								wildcardToRegex(pattern), Pattern.CASE_INSENSITIVE);
						Pattern classFilter = classPattern != null
								? Pattern.compile(wildcardToRegex(classPattern), Pattern.CASE_INSENSITIVE)
								: null;

						List<String> matches = new ArrayList<>();

						for (JavaClass cls : context.getClasses()) {
							if (matches.size() >= limit) {
								break;
							}

							String className = cls.getFullName();
							if (classFilter != null && !classFilter.matcher(className).matches()) {
								continue;
							}

							for (JavaMethod method : cls.getMethods()) {
								if (matches.size() >= limit) {
									break;
								}

								if (methodPattern.matcher(method.getName()).matches()) {
									matches.add(className + "." + method.getName());
								}
							}
						}

						if (matches.isEmpty()) {
							return JadxMcpServer.successResult("No methods found matching: " + pattern);
						}

						String result = String.format("Found %d methods matching '%s':\n%s",
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
