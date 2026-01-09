package com.example.arweld.feature.supervisor.export

import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class ZipPackager @Inject constructor() {
    fun zipFiles(outputFile: File, files: List<File>, baseDir: File) {
        ZipOutputStream(outputFile.outputStream()).use { zip ->
            files.filter { it.exists() }.forEach { file ->
                val relative = baseDir.toPath().relativize(file.toPath()).toString()
                    .replace(File.separatorChar, '/')
                zip.putNextEntry(ZipEntry(relative))
                FileInputStream(file).use { input ->
                    input.copyTo(zip)
                }
                zip.closeEntry()
            }
        }
    }
}
