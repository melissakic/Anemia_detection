package com.melis.anemia_detection.errorModels

enum class ErrorTypes(val description: String) {
    INPUT_STREAM_ERROR("Error with input stream, retake or select picture"),
    FILE_ERROR("File error, please select only picture, other files can't be processed"),
    BITMAP_ERROR("Bitmap error, you selected invalid image format, we cannot analyze this image"),
    EXIF_ERROR("Exif error, this image have corrupted meta data, we cannot analyze this image"),
    OCR_ERROR("Error with OCR analysis, check internet connection")
}