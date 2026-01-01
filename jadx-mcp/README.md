# jadx-mcp

Claude Code에서 APK/DEX 파일을 분석할 수 있도록 jadx를 MCP (Model Context Protocol) 서버로 래핑한 모듈입니다.

## 빌드

```bash
./gradlew :jadx-mcp:installDist
```

빌드 결과물: `jadx-mcp/build/install/jadx-mcp/bin/jadx-mcp`

## Claude Desktop 설정

`~/.claude/claude_desktop_config.json` 파일에 다음을 추가합니다:

```json
{
  "mcpServers": {
    "jadx": {
      "command": "/path/to/jadx/jadx-mcp/build/install/jadx-mcp/bin/jadx-mcp"
    }
  }
}
```

## 사용 가능한 Tool

### 파일 로드

| Tool | 설명 | 파라미터 |
|------|------|---------|
| `jadx_load_file` | APK/DEX 파일 로드 | `path` (필수) |
| `jadx_get_file_info` | 로드된 파일 정보 조회 | 없음 |

### 클래스/메서드 탐색

| Tool | 설명 | 파라미터 |
|------|------|---------|
| `jadx_list_classes` | 클래스 목록 조회 | `package` (선택), `limit` (기본값: 100) |
| `jadx_get_class_code` | 클래스 소스코드 조회 | `class_name` (필수) |
| `jadx_list_methods` | 메서드 목록 조회 | `class_name` (필수) |
| `jadx_get_method_code` | 메서드 소스코드 조회 | `class_name` (필수), `method_name` (필수) |

### 검색

| Tool | 설명 | 파라미터 |
|------|------|---------|
| `jadx_search_classes` | 클래스 검색 (와일드카드 지원) | `pattern` (필수), `limit` (기본값: 50) |
| `jadx_search_methods` | 메서드 검색 | `pattern` (필수), `class_pattern` (선택), `limit` (기본값: 50) |

### 리소스

| Tool | 설명 | 파라미터 |
|------|------|---------|
| `jadx_list_resources` | 리소스 목록 조회 | `type` (선택: xml, png, etc.) |
| `jadx_get_resource` | 리소스 내용 조회 | `name` (필수) |

### 이름 변경 (Rename/Alias)

| Tool | 설명 | 파라미터 |
|------|------|---------|
| `jadx_rename_class` | 클래스 이름 변경 | `class_name` (필수), `new_name` (필수) |
| `jadx_rename_method` | 메서드 이름 변경 | `class_name` (필수), `method_name` (필수), `new_name` (필수) |
| `jadx_rename_field` | 필드 이름 변경 | `class_name` (필수), `field_name` (필수), `new_name` (필수) |
| `jadx_rename_variable` | 변수 이름 변경 | `class_name` (필수), `method_name` (필수), `new_name` (필수), `arg_index` 또는 (`reg_num`, `ssa_version`) |
| `jadx_list_renames` | 적용된 이름 변경 목록 | `type` (선택: CLASS, METHOD, FIELD, PKG) |
| `jadx_remove_rename` | 이름 변경 제거 | `class_name` (필수), `type` (필수), `member_name` (선택) |

## 사용 예시

Claude Code에서 다음과 같이 사용할 수 있습니다:

1. **APK 파일 로드**
   ```
   jadx_load_file로 /path/to/app.apk 파일을 로드해줘
   ```

2. **클래스 검색**
   ```
   MainActivity가 포함된 클래스를 검색해줘
   ```

3. **소스코드 조회**
   ```
   com.example.app.MainActivity 클래스의 소스코드를 보여줘
   ```

4. **난독화된 이름 변경**
   ```
   a.b.c 클래스의 이름을 NetworkManager로 변경해줘
   ```

## 지원 파일 형식

- APK (Android Package)
- DEX (Dalvik Executable)
- AAB (Android App Bundle)
- AAR (Android Archive)
- JAR (Java Archive)
- XAPK, APKM, APKS (Split APK 형식)
