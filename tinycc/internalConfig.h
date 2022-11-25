

#ifdef TCC_TARGET_NATIVE_BUILD

#if defined _WIN32
#define TCC_TARGET_PE
#endif
#if defined __APPLE__
#define TCC_TARGET_MACHO
#endif

# if defined __i386__
#define TCC_TARGET_I386
#endif
# if defined __x86_64__
#define TCC_TARGET_X86_64
#endif
# if defined __arm__
#define TCC_TARGET_ARM
#endif
# if defined __aarch64__
#define TCC_TARGET_ARM64
#endif
# if defined __riscv && __LP64__
#define TCC_TARGET_RISCV64
#endif

//embed internal library for jit use. maybe move to stand-alone in future.
#include "lib/libtcc1.c"

#endif
