
libName=gdx.dll

jniPlatform=win32

gdxCxxFlags=-Ijni-headers -Ijni-headers/$(jniPlatform) -I. -c
gdxLdFlags=-Wl,--kill-at -shared -static-libgcc -static-libstdc++ 

cxxSrcFiles=$(wildcard *.cpp)
cxxSrcFiles+=$(wildcard gdx2d/*.cpp)
cxxSrcFiles+=$(wildcard etc1/*.cpp)

cSrcFiles=$(wildcard *.c)

cSrcFiles+=$(wildcard gdx2d/*.c)


objFiles=$(patsubst %.cpp,%.o,$(notdir $(cxxSrcFiles)))

objFiles+=$(patsubst %.c,%.o,$(notdir $(cSrcFiles)))



all:compile-all link-all


compile-all: $(cxxSrcFiles) $(cSrcFiles)
	$(CXX) $(gdxCxxFlags) $(cxxSrcFiles)
	$(CC)  $(gdxCxxFlags) $(cSrcFiles)
	
link-all: $(objFiles)
	$(CXX) -s $(gdxLdFlags) -o $(libName) $(objFiles)
	
	
clean:
	rm *.o *.dll
	

