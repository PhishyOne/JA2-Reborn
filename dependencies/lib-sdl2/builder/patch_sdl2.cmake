# CMake script to patch SDL_androidsensor.c
# Replaces ALooper_pollAll with ALooper_pollOnce for NDK 27 compatibility
if(NOT EXISTS "${INPUT_FILE}")
    message(WARNING "SDL_androidsensor.c not found at ${INPUT_FILE} - skipping patch")
    return()
endif()
file(READ "${INPUT_FILE}" CONTENT)
string(REPLACE "ALooper_pollAll" "ALooper_pollOnce" CONTENT "${CONTENT}")
file(WRITE "${INPUT_FILE}" "${CONTENT}")
message(STATUS "Patched SDL_androidsensor.c: ALooper_pollAll -> ALooper_pollOnce")
