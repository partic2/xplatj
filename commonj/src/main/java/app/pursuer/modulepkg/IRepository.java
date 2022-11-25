package app.pursuer.modulepkg;



import java.io.IOException;


public interface IRepository {
	public static class PackageNotFoundException extends Exception{
		private String missingPkgId;
		public PackageNotFoundException setMissingPackage(String pkgId) {
			missingPkgId=pkgId;
			return this;
		}
		public String getMissingPackage() {
			return missingPkgId;
		}
		@Override
		public String getMessage() {
			return "Missing Package:"+missingPkgId;
		}
		private static final long serialVersionUID = 1L;
	}

	JavaPackage getPackage(String pkgId,int minimalVersion) throws PackageNotFoundException, IOException;
	void deleteCache();
}
