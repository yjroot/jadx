package jadx.mcp;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.api.JavaMethod;
import jadx.api.ResourceFile;
import jadx.api.impl.NoOpCodeCache;
import jadx.plugins.tools.JadxExternalPluginsLoader;

public class JadxMcpContext {
	private JadxDecompiler decompiler;
	private String loadedFilePath;

	public synchronized void loadFile(String path) {
		close();

		JadxArgs args = new JadxArgs();
		args.getInputFiles().add(new File(path));
		args.setCodeCache(new NoOpCodeCache());
		args.setPluginLoader(new JadxExternalPluginsLoader());
		args.setSkipResources(false);
		args.setSkipSources(false);

		decompiler = new JadxDecompiler(args);
		decompiler.load();
		loadedFilePath = path;
	}

	public boolean isLoaded() {
		return decompiler != null;
	}

	public String getLoadedFilePath() {
		return loadedFilePath;
	}

	public JadxDecompiler getDecompiler() {
		return decompiler;
	}

	public List<JavaClass> getClasses() {
		if (!isLoaded()) {
			return Collections.emptyList();
		}
		return decompiler.getClasses();
	}

	public List<ResourceFile> getResources() {
		if (!isLoaded()) {
			return Collections.emptyList();
		}
		return decompiler.getResources();
	}

	@Nullable
	public JavaClass findClass(String fullName) {
		if (!isLoaded()) {
			return null;
		}
		JavaClass cls = decompiler.searchJavaClassByOrigFullName(fullName);
		if (cls == null) {
			cls = decompiler.searchJavaClassByAliasFullName(fullName);
		}
		return cls;
	}

	@Nullable
	public JavaMethod findMethod(String className, String methodName) {
		JavaClass cls = findClass(className);
		if (cls == null) {
			return null;
		}
		for (JavaMethod method : cls.getMethods()) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}
		return null;
	}

	public int getClassCount() {
		return isLoaded() ? decompiler.getClasses().size() : 0;
	}

	public int getResourceCount() {
		return isLoaded() ? decompiler.getResources().size() : 0;
	}

	public synchronized void close() {
		if (decompiler != null) {
			decompiler.close();
			decompiler = null;
			loadedFilePath = null;
		}
	}
}
