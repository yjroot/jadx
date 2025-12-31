# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소에서 작업할 때 참고하는 가이드입니다.

## 공통 규칙

- 모든 문서는 한글로 작성합니다.
- 코드나 문서 변경 후 사용자가 별도 지시를 하지 않아도 즉시 커밋하고 푸시합니다.

## 프로젝트 개요

JADX는 Dex to Java 디컴파일러입니다. Android Dex 및 APK 파일에서 Java 소스 코드를 생성하는 CLI 및 GUI 도구를 제공합니다. Java 11+ 및 Gradle (Kotlin DSL) 빌드 시스템을 사용합니다.

## 빌드 명령어

```bash
# 테스트 포함 전체 빌드
./gradlew clean build

# 배포용 zip 빌드 (build/jadx-<version>.zip에 출력)
./gradlew dist

# 테스트 실행
./gradlew test
./gradlew :jadx-core:test              # Core 모듈만
./gradlew :jadx-core:test --tests "jadx.tests.integration.TestClassName"  # 단일 테스트 클래스

# 코드 포맷팅 (커밋 전 필수)
./gradlew spotlessCheck                # 포맷 검사
./gradlew spotlessApply                # 자동 포맷 수정

# 로컬 실행
./gradlew :jadx-cli:installDist && build/jadx-cli/bin/jadx [options] <input>
./gradlew :jadx-gui:installDist && build/jadx-gui/bin/jadx-gui

# 의존성 업데이트 확인
./gradlew dependencyUpdates
```

## 모듈 구조

```
jadx-core/          # 핵심 디컴파일 엔진 - 메인 로직
jadx-cli/           # CLI 인터페이스 (JCommander)
jadx-gui/           # 데스크톱 GUI (Swing + FlatLaf + RxJava)
jadx-commons/
  jadx-app-commons/ # 크로스 플랫폼 설정/캐시 유틸리티
  jadx-zip/         # 보안 ZIP 리더
jadx-plugins/
  jadx-input-api/   # 입력 플러그인용 기본 API
  jadx-dex-input/   # Android DEX 바이트코드 로딩
  jadx-java-input/  # Java 바이트코드 지원
  jadx-smali-input/ # Smali 어셈블리 지원
  jadx-java-convert/# Java to DEX 변환
  jadx-rename-mappings/  # 난독화 매핑 지원
  jadx-kotlin-metadata/  # Kotlin 언어 지원
  jadx-script/      # Kotlin 스크립팅 프레임워크
  jadx-*-input/     # 다양한 패키지 포맷 플러그인 (AAB, XAPK, APKM 등)
jadx-plugins-tools/ # 플러그인 관리 유틸리티
```

## 아키텍처

### 핵심 디컴파일 파이프라인

1. **입력 로딩** - 플러그인이 바이트코드 로드 (DEX, Java, Smali)
2. **파싱** - 바이트코드를 내부 IR (Intermediate Representation)로 변환
3. **분석** - 타입 추론, 데이터 흐름 분석, SSA 변환
4. **변환** - Visitor 패스가 코드를 최적화 및 재구조화
5. **코드 생성** - IR을 Java 소스로 변환

### 주요 클래스

- `JadxDecompiler` (jadx.api) - 메인 API 진입점
- `RootNode` - 디컴파일된 코드 트리의 루트
- `ClassNode`, `MethodNode`, `FieldNode` - AST 구성 요소
- `IDexTreeVisitor` - AST 순회 및 변환용 Visitor 인터페이스
- `JadxArgs` - 설정 옵션

### Visitor 패턴

핵심 로직은 `IDexTreeVisitor`를 통한 Visitor 패턴을 광범위하게 사용합니다. 디컴파일 패스는 AST를 순회하고 변환하는 Visitor로 구현됩니다. Visitor는 순차적으로 적용되며 순서가 중요합니다.

주요 Visitor 패키지:
- `jadx.core.dex.visitors.*` - 메인 변환 Visitor
- `jadx.core.dex.visitors.ssa.*` - SSA 형식 분석
- `jadx.core.dex.visitors.typeinference.*` - 타입 시스템
- `jadx.core.dex.regions.*` - 제어 흐름 분석

### 플러그인 시스템

플러그인은 `JadxPlugin` 인터페이스를 구현합니다. 주요 플러그인 유형:
- **입력 플러그인** - 다양한 바이트코드 포맷 로드 (`ICodeLoader` 구현)
- **패스 플러그인** - 변환 패스 추가
- **GUI 플러그인** - UI 확장

플러그인은 `JadxPluginManager`를 통해 이벤트 기반으로 관리됩니다.

## 코드 스타일

- Java 포맷팅: Eclipse formatter (`config/code-formatter/`에 설정)
- Kotlin 포맷팅: ktlint (탭 사용)
- 줄 끝: Unix (LF)
- 인코딩: UTF-8
- 커밋 전 `./gradlew spotlessApply` 실행 필수

## 기여 요구사항

- Java 11 이하의 기능 및 API만 사용
- 제출 전 전체 빌드 실행: `./gradlew clean build dist`
- 순수 스타일/의존성 업데이트 PR 금지
- 디컴파일 이슈의 경우 APK 파일 첨부 (GitHub용으로 .apk.zip으로 이름 변경)

## 환경 변수

- `JADX_VERSION` - 버전 문자열 오버라이드
- `JADX_BUILD_JAVA_VERSION` - 특정 Java 버전으로 빌드
- `JADX_TEST_JAVA_VERSION` - 특정 Java 버전으로 테스트 실행
- `JADX_DISABLE_XML_SECURITY` - XML 보안 검사 비활성화
- `JADX_DISABLE_ZIP_SECURITY` - ZIP 보안 검사 비활성화
