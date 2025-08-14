package com.example.WeConnect_BE.Util;

public class RenameFile {
    public static String rename(String originalName, String newBaseName) {
        if (originalName == null || originalName.isBlank()) return newBaseName;
        int dotIndex = originalName.lastIndexOf('.');
        String extension = dotIndex >= 0 ? originalName.substring(dotIndex) : "";
        return newBaseName + extension;
    }

}
