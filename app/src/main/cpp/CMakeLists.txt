cmake_minimum_required(VERSION 3.18.1)

project("Flam")

add_library(
        native-lib
        SHARED
        native_bridge.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        native-lib
        ${log-lib}
)