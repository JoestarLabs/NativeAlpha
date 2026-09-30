cmake_minimum_required(VERSION 3.22.1)

project(adblock-client)

# Size and symbol export optimizations:
# Keep only exported JNI symbols (JNIEXPORT), eliminate unreferenced sections,
# and link with 16 KB max page size for Android 15+ compatibility.
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fvisibility=hidden -fvisibility-inlines-hidden -ffunction-sections -fdata-sections")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fvisibility=hidden -ffunction-sections -fdata-sections")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--gc-sections -Wl,--exclude-libs,ALL -Wl,-z,max-page-size=16384")

add_library(
    adblock-client
    SHARED
    src/main/cpp/adblockclient-lib.cpp
    src/main/cpp/third-party/ad-block/ad_block_client.cc
    src/main/cpp/third-party/ad-block/cosmetic_filter.cc
    src/main/cpp/third-party/ad-block/filter.cc
    src/main/cpp/third-party/ad-block/no_fingerprint_domain.cc
    src/main/cpp/third-party/ad-block/context_domain.cc
    src/main/cpp/third-party/ad-block/protocol.cc
    src/main/cpp/third-party/bloom-filter-cpp/BloomFilter.cpp
    src/main/cpp/third-party/hashset-cpp/hashFn.cc
    src/main/cpp/third-party/hashset-cpp/hash_set.cc
)

find_library(
    log-lib
    log
)

target_link_libraries(
    adblock-client
    ${log-lib}
)
