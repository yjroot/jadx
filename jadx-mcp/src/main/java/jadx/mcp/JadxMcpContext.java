package jadx.mcp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.api.JavaField;
import jadx.api.JavaMethod;
import jadx.api.ResourceFile;
import jadx.api.data.ICodeRename;
import jadx.api.data.IJavaNodeRef;
import jadx.api.data.impl.JadxCodeData;
import jadx.api.data.impl.JadxCodeRef;
import jadx.api.data.impl.JadxCodeRename;
import jadx.api.data.impl.JadxNodeRef;
import jadx.api.impl.NoOpCodeCache;
import jadx.plugins.tools.JadxExternalPluginsLoader;

public class JadxMcpContext {
	private JadxDecompiler decompiler;
	private String loadedFilePath;
	private JadxCodeData codeData;
	private Set<ICodeRename> renames;

	public synchronized void loadFile(String path) {
		close();

		codeData = new JadxCodeData();
		renames = new HashSet<>();

		JadxArgs args = new JadxArgs();
		args.getInputFiles().add(new File(path));
		args.setCodeCache(new NoOpCodeCache());
		args.setPluginLoader(new JadxExternalPluginsLoader());
		args.setSkipResources(false);
		args.setSkipSources(false);
		args.setCodeData(codeData);

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
			codeData = null;
			renames = null;
		}
	}

	// ===== Rename API =====

	public synchronized void renameClass(String className, String newName) {
		checkLoaded();
		ICodeRename rename = new JadxCodeRename(JadxNodeRef.forCls(className), newName);
		addRename(rename);
	}

	public synchronized void renameMethod(String className, String methodShortId, String newName) {
		checkLoaded();
		JadxNodeRef nodeRef = new JadxNodeRef(IJavaNodeRef.RefType.METHOD, className, methodShortId);
		ICodeRename rename = new JadxCodeRename(nodeRef, newName);
		addRename(rename);
	}

	public synchronized void renameField(String className, String fieldShortId, String newName) {
		checkLoaded();
		JadxNodeRef nodeRef = new JadxNodeRef(IJavaNodeRef.RefType.FIELD, className, fieldShortId);
		ICodeRename rename = new JadxCodeRename(nodeRef, newName);
		addRename(rename);
	}

	public synchronized void renameMethodArg(String className, String methodShortId, int argIndex, String newName) {
		checkLoaded();
		JadxNodeRef nodeRef = new JadxNodeRef(IJavaNodeRef.RefType.METHOD, className, methodShortId);
		JadxCodeRef codeRef = JadxCodeRef.forMthArg(argIndex);
		ICodeRename rename = new JadxCodeRename(nodeRef, codeRef, newName);
		addRename(rename);
	}

	public synchronized void renameVariable(String className, String methodShortId, int regNum, int ssaVersion,
			String newName) {
		checkLoaded();
		JadxNodeRef nodeRef = new JadxNodeRef(IJavaNodeRef.RefType.METHOD, className, methodShortId);
		JadxCodeRef codeRef = JadxCodeRef.forVar(regNum, ssaVersion);
		ICodeRename rename = new JadxCodeRename(nodeRef, codeRef, newName);
		addRename(rename);
	}

	private void addRename(ICodeRename rename) {
		// Remove existing rename for same node
		renames.removeIf(r -> r.getNodeRef().equals(rename.getNodeRef())
				&& ((r.getCodeRef() == null && rename.getCodeRef() == null)
						|| (r.getCodeRef() != null && r.getCodeRef().equals(rename.getCodeRef()))));
		renames.add(rename);
		applyRenames();
	}

	public synchronized boolean removeRename(String className, IJavaNodeRef.RefType refType,
			@Nullable String shortId) {
		checkLoaded();
		boolean removed = renames.removeIf(r -> {
			IJavaNodeRef nodeRef = r.getNodeRef();
			if (nodeRef.getType() != refType) {
				return false;
			}
			if (!nodeRef.getDeclaringClass().equals(className)) {
				return false;
			}
			if (shortId != null && !shortId.equals(nodeRef.getShortId())) {
				return false;
			}
			return true;
		});
		if (removed) {
			applyRenames();
		}
		return removed;
	}

	private void applyRenames() {
		codeData.setRenames(new ArrayList<>(renames));
		decompiler.reloadCodeData();
	}

	public List<ICodeRename> getRenames() {
		if (renames == null) {
			return Collections.emptyList();
		}
		return new ArrayList<>(renames);
	}

	@Nullable
	public JavaField findField(String className, String fieldName) {
		JavaClass cls = findClass(className);
		if (cls == null) {
			return null;
		}
		for (JavaField field : cls.getFields()) {
			if (field.getName().equals(fieldName)) {
				return field;
			}
		}
		return null;
	}

	public String getMethodShortId(JavaMethod method) {
		return method.getMethodNode().getMethodInfo().getShortId();
	}

	public String getFieldShortId(JavaField field) {
		return field.getFieldNode().getFieldInfo().getShortId();
	}

	private void checkLoaded() {
		if (!isLoaded()) {
			throw new IllegalStateException("No file loaded");
		}
	}
}
