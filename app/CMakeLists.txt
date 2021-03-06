# Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
# Copyright (C) 2017-2020 Pacien TRAN-GIRARD
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.

cmake_minimum_required(VERSION 3.4.1)
include(ExternalProject)

set(CC_OPTIMISATION_FLAGS "-ffunction-sections -fdata-sections")
set(LD_OPTIMISATION_FLAGS "-Wl,--gc-sections")

set(xCONFIG
  "CC=${CMAKE_C_COMPILER} \
    ${CMAKE_C_COMPILE_OPTIONS_EXTERNAL_TOOLCHAIN}${CMAKE_C_COMPILER_EXTERNAL_TOOLCHAIN} \
    ${CMAKE_C_COMPILE_OPTIONS_TARGET}${CMAKE_C_COMPILER_TARGET} \
    ${CMAKE_C_COMPILE_OPTIONS_SYSROOT}${CMAKE_SYSROOT}"
  "LD=${CMAKE_LINKER}"
  "AR=${CMAKE_AR}"
  "RANLIB=${CMAKE_RANLIB}"
  "CFLAGS=${CMAKE_C_FLAGS} ${CC_OPTIMISATION_FLAGS}"
  "LDFLAGS=${CMAKE_SHARED_LINKER_FLAGS} ${LD_OPTIMISATION_FLAGS}"
  "--host=${CMAKE_C_COMPILER_TARGET}"
)

ExternalProject_Add(lzo
  URL               http://www.oberhumer.com/opensource/lzo/download/lzo-2.10.tar.gz
  URL_HASH          SHA1=4924676a9bae5db58ef129dc1cebce3baa3c4b5d
  CONFIGURE_COMMAND <SOURCE_DIR>/configure ${xCONFIG} --disable-shared
  BUILD_COMMAND     make -j8
  INSTALL_COMMAND   make install DESTDIR=${CMAKE_CURRENT_BINARY_DIR} &&
                    rm -r <BINARY_DIR>
)

ExternalProject_Add(libressl
  URL               https://ftp.openbsd.org/pub/OpenBSD/LibreSSL/libressl-3.0.2.tar.gz
  URL_HASH          SHA256=df7b172bf79b957dd27ef36dcaa1fb162562c0e8999e194aa8c1a3df2f15398e
  CONFIGURE_COMMAND <SOURCE_DIR>/configure ${xCONFIG} --disable-shared
  BUILD_COMMAND     make -j8 -C crypto
  INSTALL_COMMAND   make -C crypto install DESTDIR=${CMAKE_CURRENT_BINARY_DIR} &&
                    make -C include install DESTDIR=${CMAKE_CURRENT_BINARY_DIR} &&
                    rm -r <BINARY_DIR>
)

ExternalProject_Add(tinc
  DEPENDS           lzo libressl
  URL               https://github.com/leptonyu/tinc/releases/download/1.1pre17-36-gf46d99f/tinc-1.1pre17-36-gf46d99f.tar.gz 
  URL_HASH          SHA256=56db0d44d02ee5d42f0eaa3238c39f98bf04eb81d5056447acc8add422656d82
  CONFIGURE_COMMAND autoreconf -fsi <SOURCE_DIR> &&
                    <SOURCE_DIR>/configure ${xCONFIG}
                      --with-openssl=${CMAKE_CURRENT_BINARY_DIR}/usr/local
                      --with-lzo=${CMAKE_CURRENT_BINARY_DIR}/usr/local
                      --disable-curses
                      --disable-readline
  BUILD_COMMAND     make -j8 -C src
  INSTALL_COMMAND   make -C src install DESTDIR=${CMAKE_CURRENT_BINARY_DIR} &&
                    ${CMAKE_COMMAND} -E copy
                      ${CMAKE_CURRENT_BINARY_DIR}/usr/local/sbin/tinc ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libtinc.so &&
                    ${CMAKE_COMMAND} -E copy
                      ${CMAKE_CURRENT_BINARY_DIR}/usr/local/sbin/tincd ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libtincd.so &&
                    rm -r <BINARY_DIR>
)

add_library(main SHARED src/main/c/main.c)
add_dependencies(main tinc)
