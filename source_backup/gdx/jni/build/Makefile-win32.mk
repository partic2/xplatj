build-all: $(srcFiles) showSrcFile
	$(CXX) $(GdxCxxFlags) $(srcFiles)
	
	
	
showSrcFile:
	echo $(srcFiles) 
	
