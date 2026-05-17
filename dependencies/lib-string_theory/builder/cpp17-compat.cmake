function(replace_once FILE_PATH OLD_TEXT NEW_TEXT)
    file(READ "${FILE_PATH}" CONTENT)
    string(FIND "${CONTENT}" "${OLD_TEXT}" OLD_POS)
    string(FIND "${CONTENT}" "${NEW_TEXT}" NEW_POS)

    if(OLD_POS GREATER_EQUAL 0)
        string(REPLACE "${OLD_TEXT}" "${NEW_TEXT}" CONTENT "${CONTENT}")
        file(WRITE "${FILE_PATH}" "${CONTENT}")
    elseif(NEW_POS GREATER_EQUAL 0)
        message(STATUS "Already patched: ${FILE_PATH}")
    else()
        message(FATAL_ERROR "Could not find patch target in ${FILE_PATH}")
    endif()
endfunction()

set(FORMATTER "${SOURCE_DIR}/include/st_formatter.h")

replace_once("${FORMATTER}"
[=[return [value](const ST::format_spec &format, ST::format_writer &output) {
            format_type(format, output, value);
        };
    }
]=]
[=[return [value](const ST::format_spec &format, ST::format_writer &output) mutable {
            format_type(format, output, value);
        };
    }

    inline void format_type(const ST::format_spec &format, ST::format_writer &output,
                            std::string_view value)
    {
        ST::format_string(format, output, value.data(), value.size());
    }
]=])
