package app.pursuer.modulepkg;


public interface IJavaPackageEntry {

	<T> T getInstance(Class<T> cls, String id);
}
