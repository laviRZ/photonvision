/*
 * Copyright (C) Photon Vision.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.photonvision.jni;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import org.photonvision.common.hardware.Platform;
import org.photonvision.common.logging.LogGroup;
import org.photonvision.common.logging.Logger;

public abstract class PhotonJniCommon {
    static boolean libraryLoaded = false;
    protected static Logger logger = null;

    protected static synchronized void forceLoad(Class<?> clazz, List<String> libraries) {
        if (libraryLoaded) return;
        if (logger == null) logger = new Logger(clazz, LogGroup.Camera);

        for (var libraryName : libraries) {
            try {
                // We always extract the shared object (we could hash each so, but that's a lot of work)
                System.out.println("Loading library " + libraryName);
                var arch_name = Platform.getNativeLibraryFolderName();
                var nativeLibName = System.mapLibraryName(libraryName);
                var in = clazz.getResourceAsStream("/nativelibraries/" + arch_name + "/" + nativeLibName);
                System.out.println("Loading library " + arch_name + "/" + nativeLibName);

                if (in == null) {
                    libraryLoaded = false;
                    System.out.println("Couldn't find library " + arch_name + "/" + nativeLibName);
                    return;
                }

                // It's important that we don't mangle the names of these files on Windows at least
                File temp = new File(System.getProperty("java.io.tmpdir"), nativeLibName);
                FileOutputStream fos = new FileOutputStream(temp);

                int read = -1;
                byte[] buffer = new byte[1024];
                while ((read = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                fos.close();
                in.close();
                System.out.println("Loaded library " + temp.getAbsolutePath());

                System.load(temp.getAbsolutePath());

                logger.info("Successfully loaded shared object " + temp.getName());

            } catch (Exception e) {
                logger.error("Couldn't load shared object " + libraryName, e);
                e.printStackTrace();
                // logger.error(System.getProperty("java.library.path"));
                break;
            }
        }
        libraryLoaded = true;
    }

    protected static synchronized void forceLoad(Class<?> clazz, String libraryName) {
        forceLoad(clazz, List.of(libraryName));
    }

    protected static synchronized void unpack(String libraryName) {
        if (logger == null) logger = new Logger(PhotonJniCommon.class, LogGroup.Camera);

        try {
            // We always extract the shared object (we could hash each so, but that's a lot of work)
            System.out.println("Loading library " + libraryName);
            var arch_name = Platform.getNativeLibraryFolderName();
            var nativeLibName = System.mapLibraryName(libraryName);
            // check if already at /usr/lib
            if (new File("/usr/lib", nativeLibName).exists()) {
                System.out.println("Already loaded library " + arch_name + "/" + nativeLibName);
                return;
            }
            var in =
                    PhotonJniCommon.class.getResourceAsStream(
                            "/nativelibraries/" + arch_name + "/" + nativeLibName);
            System.out.println("Loading library " + arch_name + "/" + nativeLibName);

            if (in == null) {
                libraryLoaded = false;
                System.out.println("Couldn't find library " + arch_name + "/" + nativeLibName);
                return;
            }

            // It's important that we don't mangle the names of these files on Windows at least
            File temp = new File(System.getProperty("java.io.tmpdir"), nativeLibName);
            FileOutputStream fos = new FileOutputStream(temp);

            int read = -1;
            byte[] buffer = new byte[1024];
            while ((read = in.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.close();
            in.close();
            System.out.println("Loaded library " + temp.getAbsolutePath());
            if (Platform.isLinux()) {
                // copy to /usr/lib
                File lib = new File("/usr/lib", nativeLibName);
                fos = new FileOutputStream(lib);
                in =
                        PhotonJniCommon.class.getResourceAsStream(
                                "/nativelibraries/" + arch_name + "/" + nativeLibName);
                while ((read = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                fos.close();
                in.close();
                System.out.println("Loaded library " + lib.getAbsolutePath());
            }

        } catch (Exception e) {
            logger.error("Couldn't load shared object " + libraryName, e);
            e.printStackTrace();
            // logger.error(System.getProperty("java.library.path"));
        }
    }

    public static boolean isWorking() {
        return libraryLoaded;
    }
}
