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

set(FUNCTION_TYPES "${SOURCE_DIR}/include/sol/function_types.hpp")
set(USER_CONTAINER "${SOURCE_DIR}/include/sol/usertype_container.hpp")
set(STACK_FIELD "${SOURCE_DIR}/include/sol/stack_field.hpp")

replace_once("${FUNCTION_TYPES}"
[=[lua_CFunction freefunc = &function_detail::upvalue_this_member_variable<C, Fx>::template call<is_yielding, no_trampoline>;]=]
[=[lua_CFunction freefunc = &function_detail::upvalue_this_member_variable<C, Fx>::real_call;]=])

replace_once("${FUNCTION_TYPES}"
[=[= &function_detail::upvalue_this_member_variable<typename Tu::type, Fx>::template call<is_yielding, no_trampoline>;]=]
[=[= &function_detail::upvalue_this_member_variable<typename Tu::type, Fx>::real_call;]=])

replace_once("${USER_CONTAINER}"
[=[auto& end = i.end();]=]
[=[auto& end = i.sen();]=])

replace_once("${STACK_FIELD}"
[=[lua_getfield(L, tableindex, &key[0]);]=]
[=[if constexpr (std::is_same_v<std::decay_t<Key>, const char*>) {
							if (key != nullptr) {
								lua_getfield(L, tableindex, key);
							}
							else {
								push(L, lua_nil);
							}
						}
						else {
							lua_getfield(L, tableindex, key.c_str());
						}]=])
