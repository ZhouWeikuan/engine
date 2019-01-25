LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := cclua_android

LOCAL_MODULE_FILENAME := libluaccandroid

LOCAL_ARM_MODE := arm

LUASRC := $(LOCAL_PATH)/../../../../../../../3rd/lua_5_3

LOCAL_SRC_FILES := ../manual/platform/android/CCLuaJavaBridge.cpp \
                   ../manual/platform/android/jni/Java_org_cocos2dx_lib_Cocos2dxLuaJavaBridge.cpp

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../.. \
					$(LUASRC) \
                    $(LOCAL_PATH)/../manual \
                    $(LOCAL_PATH)/../../../../external/lua/tolua \
                    $(LOCAL_PATH)/../manual/platform/android \
                    $(LOCAL_PATH)/../manual/platform/android/jni

LOCAL_EXPORT_LDLIBS := -lGLESv2 \
                       -llog \
                       -landroid


include $(BUILD_STATIC_LIBRARY)

#==============================================================

include $(CLEAR_VARS)

LOCAL_MODULE    := cclua_static

LOCAL_MODULE_FILENAME := libluacc

LOCAL_ARM_MODE := arm

LUASRC := $(LOCAL_PATH)/../../../../../../../3rd/lua

LOCAL_SRC_FILES := ../manual/CCLuaBridge.cpp \
          ../manual/CCLuaEngine.cpp \
          ../manual/CCLuaStack.cpp \
          ../manual/CCLuaValue.cpp \
          ../manual/Cocos2dxLuaLoader.cpp \
          ../manual/LuaBasicConversions.cpp \
          ../manual/lua_module_register.cpp \
          ../auto/lua_cocos2dx_auto.cpp \
          ../auto/lua_cocos2dx_physics_auto.cpp \
          ../auto/lua_cocos2dx_experimental_auto.cpp \
          ../manual/cocos2d/lua_cocos2dx_deprecated.cpp \
          ../manual/cocos2d/lua_cocos2dx_experimental_manual.cpp \
          ../manual/cocos2d/lua_cocos2dx_manual.cpp \
          ../manual/cocos2d/lua_cocos2dx_physics_manual.cpp \
          ../manual/cocos2d/LuaOpengl.cpp \
          ../manual/cocos2d/LuaScriptHandlerMgr.cpp \
          ../manual/tolua_fix.cpp \
          ../../../../external/lua/tolua/tolua_event.c \
          ../../../../external/lua/tolua/tolua_is.c \
          ../../../../external/lua/tolua/tolua_map.c \
          ../../../../external/lua/tolua/tolua_push.c \
          ../../../../external/lua/tolua/tolua_to.c \
          ../../../../external/xxtea/xxtea.cpp \
		  $(LUASRC)/lapi.c \
		  $(LUASRC)/lauxlib.c \
		  $(LUASRC)/lbaselib.c \
		  $(LUASRC)/lbitlib.c \
		  $(LUASRC)/lcode.c \
		  $(LUASRC)/lcorolib.c \
		  $(LUASRC)/lctype.c \
		  $(LUASRC)/ldblib.c \
		  $(LUASRC)/ldebug.c \
		  $(LUASRC)/ldo.c \
		  $(LUASRC)/ldump.c \
		  $(LUASRC)/lfunc.c \
		  $(LUASRC)/lgc.c \
		  $(LUASRC)/linit.c \
		  $(LUASRC)/liolib.c \
		  $(LUASRC)/llex.c \
		  $(LUASRC)/lmathlib.c \
		  $(LUASRC)/lmem.c \
		  $(LUASRC)/loadlib.c \
		  $(LUASRC)/lobject.c \
		  $(LUASRC)/lopcodes.c \
		  $(LUASRC)/loslib.c \
		  $(LUASRC)/lparser.c \
		  $(LUASRC)/lstate.c \
		  $(LUASRC)/lstring.c \
		  $(LUASRC)/lstrlib.c \
		  $(LUASRC)/ltable.c \
		  $(LUASRC)/ltablib.c \
		  $(LUASRC)/ltm.c \
		  $(LUASRC)/lua.c \
		  $(LUASRC)/lundump.c \
		  $(LUASRC)/lutf8lib.c \
		  $(LUASRC)/lvm.c \
		  $(LUASRC)/lzio.c \
          ../auto/lua_cocos2dx_audioengine_auto.cpp \
          ../manual/audioengine/lua_cocos2dx_audioengine_manual.cpp

#Component
LOCAL_SRC_FILES += ../manual/CCComponentLua.cpp \

#3d
LOCAL_SRC_FILES += ../manual/3d/lua_cocos2dx_3d_manual.cpp \
                   ../auto/lua_cocos2dx_3d_auto.cpp

#cocosdenshion
LOCAL_SRC_FILES += ../manual/cocosdenshion/lua_cocos2dx_cocosdenshion_manual.cpp \
                   ../auto/lua_cocos2dx_cocosdenshion_auto.cpp

#network
LOCAL_SRC_FILES += ../manual/network/lua_cocos2dx_network_manual.cpp \
                   ../manual/network/lua_extensions.c \
                   ../manual/network/lua_downloader.cpp \
                   ../manual/network/Lua_web_socket.cpp \
                   ../manual/network/lua_xml_http_request.cpp \
                   ../../../../external/lua/luasocket/auxiliar.c \
                   ../../../../external/lua/luasocket/buffer.c \
                   ../../../../external/lua/luasocket/except.c \
                   ../../../../external/lua/luasocket/inet.c \
                   ../../../../external/lua/luasocket/io.c \
                   ../../../../external/lua/luasocket/luasocket.c \
                   ../../../../external/lua/luasocket/luasocket_scripts.c \
                   ../../../../external/lua/luasocket/mime.c \
                   ../../../../external/lua/luasocket/options.c \
                   ../../../../external/lua/luasocket/select.c \
                   ../../../../external/lua/luasocket/serial.c \
                   ../../../../external/lua/luasocket/tcp.c \
                   ../../../../external/lua/luasocket/timeout.c \
                   ../../../../external/lua/luasocket/udp.c \
                   ../../../../external/lua/luasocket/unix.c \
                   ../../../../external/lua/luasocket/usocket.c

#cocosbuilder
LOCAL_SRC_FILES += ../manual/cocosbuilder/lua_cocos2dx_cocosbuilder_manual.cpp \
                   ../manual/cocosbuilder/CCBProxy.cpp \
                   ../auto/lua_cocos2dx_cocosbuilder_auto.cpp

#cocostudio
LOCAL_SRC_FILES += ../manual/cocostudio/lua_cocos2dx_coco_studio_manual.cpp \
                   ../manual/cocostudio/CustomGUIReader.cpp \
                   ../manual/cocostudio/lua_cocos2dx_csloader_manual.cpp \
                   ../auto/lua_cocos2dx_csloader_auto.cpp \
                   ../auto/lua_cocos2dx_studio_auto.cpp \
                   ../manual/cocostudio/lua-cocos-studio-conversions.cpp

#spine
LOCAL_SRC_FILES += ../manual/spine/lua_cocos2dx_spine_manual.cpp \
                   ../manual/spine/LuaSkeletonAnimation.cpp \
                   ../auto/lua_cocos2dx_spine_auto.cpp

#ui
LOCAL_SRC_FILES += ../manual/ui/lua_cocos2dx_experimental_webview_manual.cpp \
                   ../manual/ui/lua_cocos2dx_experimental_video_manual.cpp \
                   ../manual/ui/lua_cocos2dx_ui_manual.cpp \
                   ../auto/lua_cocos2dx_experimental_video_auto.cpp \
                   ../auto/lua_cocos2dx_ui_auto.cpp \
                   ../auto/lua_cocos2dx_experimental_webview_auto.cpp

#extension
LOCAL_SRC_FILES += ../manual/extension/lua_cocos2dx_extension_manual.cpp \
                   ../auto/lua_cocos2dx_extension_auto.cpp \

#physics3d
LOCAL_SRC_FILES += ../manual/physics3d/lua_cocos2dx_physics3d_manual.cpp \
                   ../auto/lua_cocos2dx_physics3d_auto.cpp \

#navmesh
LOCAL_SRC_FILES += ../manual/navmesh/lua_cocos2dx_navmesh_conversions.cpp \
                   ../manual/navmesh/lua_cocos2dx_navmesh_manual.cpp \
                   ../auto/lua_cocos2dx_navmesh_auto.cpp \

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../../../external/lua/tolua \
					$(LUASRC) \
                    $(LOCAL_PATH)/../../../2d \
                    $(LOCAL_PATH)/../../../3d \
                    $(LOCAL_PATH)/../../../network \
                    $(LOCAL_PATH)/../../../editor-support/cocosbuilder \
                    $(LOCAL_PATH)/../../../editor-support/cocostudio \
                    $(LOCAL_PATH)/../../../editor-support/cocostudio/ActionTimeline \
                    $(LOCAL_PATH)/../../../editor-support/spine \
                    $(LOCAL_PATH)/../../../ui \
                    $(LOCAL_PATH)/../../../physics3d \
                    $(LOCAL_PATH)/../../../navmesh \
                    $(LOCAL_PATH)/../../../../extensions \
                    $(LOCAL_PATH)/../auto \
                    $(LOCAL_PATH)/../manual \
                    $(LOCAL_PATH)/../manual/cocos2d \
                    $(LOCAL_PATH)/../manual/3d \
                    $(LOCAL_PATH)/../manual/cocosdenshion \
                    $(LOCAL_PATH)/../manual/audioengine \
                    $(LOCAL_PATH)/../manual/network \
                    $(LOCAL_PATH)/../manual/extension \
                    $(LOCAL_PATH)/../manual/cocostudio \
                    $(LOCAL_PATH)/../manual/cocosbuilder \
                    $(LOCAL_PATH)/../manual/spine \
                    $(LOCAL_PATH)/../manual/ui \
                    $(LOCAL_PATH)/../manual/navmesh \
                    $(LOCAL_PATH)/../../../../external/xxtea \
                    $(LOCAL_PATH)/../../../.. \
                    $(LOCAL_PATH)/../../../../external/lua

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../../../../external/lua/tolua \
							$(LUASRC) \
                           $(LOCAL_PATH)/../auto \
                           $(LOCAL_PATH)/../manual \
                           $(LOCAL_PATH)/../manual/cocos2d \
                           $(LOCAL_PATH)/../manual/3d \
                           $(LOCAL_PATH)/../manual/cocosdenshion \
                           $(LOCAL_PATH)/../manual/audioengine \
                           $(LOCAL_PATH)/../manual/network \
                           $(LOCAL_PATH)/../manual/cocosbuilder \
                           $(LOCAL_PATH)/../manual/cocostudio \
                           $(LOCAL_PATH)/../manual/spine \
                           $(LOCAL_PATH)/../manual/extension \
                           $(LOCAL_PATH)/../manual/ui \
                           $(LOCAL_PATH)/../manual/navmesh \
                           $(LOCAL_PATH)/../../../..

LOCAL_WHOLE_STATIC_LIBRARIES := cclua_android

LOCAL_STATIC_LIBRARIES := cc_static

LOCAL_CFLAGS += -Wno-psabi
LOCAL_CFLAGS += -DLUA_COMPAT_5_1
LOCAL_CFLAGS += -DLUA_COMPAT_APIINTCASTS
LOCAL_EXPORT_CFLAGS += -DLUA_COMPAT_5_1
LOCAL_EXPORT_CFLAGS += -DLUA_COMPAT_APIINTCASTS

include $(BUILD_STATIC_LIBRARY)

$(call import-module,$(LUA_IMPORT_PATH))
$(call import-module, cocos)
