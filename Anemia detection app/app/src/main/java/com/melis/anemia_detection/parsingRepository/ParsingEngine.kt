package com.melis.anemia_detection.parsingRepository

import com.google.firebase.ml.vision.text.FirebaseVisionText

class ParsingEngine {

    companion object {
        fun extractTargetedTextAndValue(
            text: FirebaseVisionText,
            targetTexts: List<String>,
            tolerance: Int
        ): String {
            val textLines = mutableListOf<String>()
            text.textBlocks.forEach { block ->
                block.lines.forEach { line ->
                    if (targetTexts.any { it in line.text }) {
                        // Store the top and bottom coordinates of the line containing the target text
                        val targetTop = line.boundingBox?.top ?: 0
                        val targetBottom = line.boundingBox?.bottom ?: 0

                        // Iterate through all text lines again to find lines within tolerance
                        text.textBlocks.forEach { innerBlock ->
                            innerBlock.lines.forEach { innerLine ->
                                // Check if the line's top or bottom coordinate is within the tolerance range of the target line
                                val topWithinTolerance =
                                    innerLine.boundingBox?.top?.let {
                                        it in (targetTop - tolerance)..(targetTop + tolerance)
                                    } ?: false
                                val bottomWithinTolerance =
                                    innerLine.boundingBox?.bottom?.let {
                                        it in (targetBottom - tolerance)..(targetBottom + tolerance)
                                    } ?: false

                                // If the line's top or bottom coordinate is within the tolerance range, add it to the result
                                if (topWithinTolerance || bottomWithinTolerance) {
                                    textLines.add(innerLine.text)
                                }
                            }
                        }
                    }
                }
            }
            // Join the lines within tolerance
            return textLines.joinToString("\n")
        }
    }
}