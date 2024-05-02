package com.melis.anemia_detection.parsingRepository

import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.melis.anemia_detection.anemiaModels.RefinedValues
import java.util.Locale
import java.util.Vector

class ParsingEngine {

    companion object {
        fun extractAnemiaRawData(
            text: FirebaseVisionText,
            targetTexts: List<String>,
            tolerance: Int
        ): String {
            val hemoglobinLine = mutableListOf<FirebaseVisionText.Line>()
            val mcvLine = mutableListOf<FirebaseVisionText.Line>()

            text.textBlocks.forEach { block ->
                block.lines.forEach { line ->
                    if (targetTexts.any { target ->
                            target.lowercase(Locale.getDefault()) in line.text.lowercase(
                                Locale.getDefault()
                            )
                        }) {
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
                                    if (line.text.lowercase(Locale.getDefault())
                                            .contains("hemoglobin") || line.text.lowercase(Locale.getDefault())
                                            .contains("hgb")
                                    )
                                        hemoglobinLine.add(innerLine)
                                    else if (line.text.lowercase(Locale.getDefault())
                                            .contains("mcv")
                                    ) {
                                        mcvLine.add(innerLine)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Join the lines within tolerance
//            return textLines.joinToString("\n")
            val result = extractAnemiaRefinedData(mcvLine, hemoglobinLine)
            return result.joinToString()
        }


        private fun extractAnemiaRefinedData(
            mcvLines: MutableList<FirebaseVisionText.Line>,
            hemoglobinLines: MutableList<FirebaseVisionText.Line>
        ): List<RefinedValues> {

            var refinedValues: MutableList<RefinedValues> = mutableListOf()

            var mcvLine: FirebaseVisionText.Line? = null
            val mcvIterator = mcvLines.iterator()
            while (mcvIterator.hasNext()) {
                val line = mcvIterator.next()
                if (line.text.contains("mcv", ignoreCase = true)) {
                    mcvLine = line
                    mcvIterator.remove()
                    break
                }
            }

            var hemoglobinLine: FirebaseVisionText.Line? = null
            val hemoglobinIterator = hemoglobinLines.iterator()
            while (hemoglobinIterator.hasNext()) {
                val line = hemoglobinIterator.next()
                if (line.text.contains("hemoglobin", ignoreCase = true) || line.text.contains(
                        "hgb",
                        ignoreCase = true
                    )
                ) {
                    hemoglobinLine = line
                    hemoglobinIterator.remove()
                    break
                }
            }

            if (mcvLine != null && hemoglobinLine != null)
                refinedValues =
                    extractSingularValues(mcvLine, hemoglobinLine, mcvLines, hemoglobinLines)

            return refinedValues
        }

        private fun extractSingularValues(
            mcvLine: FirebaseVisionText.Line,
            hemoglobinLine: FirebaseVisionText.Line,
            mcvLines: MutableList<FirebaseVisionText.Line>,
            hemoglobinLines: MutableList<FirebaseVisionText.Line>,
        ): MutableList<RefinedValues> {
            val extractedSingularValues = Vector<RefinedValues>()

            val mcvExtractedValues = HashMap<FirebaseVisionText.Line, Int>()

            mcvLine?.let { mcv ->
                // Find the line with the left coordinate closest to the MCV line
                mcvLines.forEach {
                    mcvExtractedValues[it] = Math.abs(
                        (it.boundingBox?.left ?: 0) - (mcv.boundingBox?.right ?: 0)
                    )
                }

                val neededMcvValue = mcvExtractedValues.minByOrNull {
                    it.value
                }?.key?.text?.extractNumericPart()?.toDouble() ?: 0.0

                extractedSingularValues.add(RefinedValues("MCV", neededMcvValue))
            }

            val hemoglobinExtractedValues = HashMap<FirebaseVisionText.Line, Int>()

            hemoglobinLine?.let { hemoglobin ->
                // Find the line with the left coordinate closest to the MCV line
                hemoglobinLines.forEach {
                    hemoglobinExtractedValues[it] = Math.abs(
                        (it.boundingBox?.left ?: 0) - (hemoglobin.boundingBox?.right ?: 0)
                    )
                }

                val neededHemoglobinValue = hemoglobinExtractedValues.minByOrNull {
                    it.value
                }?.key?.text?.extractNumericPart()?.toDouble() ?: 0.0

                extractedSingularValues.add(RefinedValues("Hemoglobin", neededHemoglobinValue))
            }

            return extractedSingularValues
        }

        private fun String.extractNumericPart(): String? {
            val regex = Regex("([0-9]+(\\.[0-9]+)?)")
            val matchResult = regex.find(this)
            return matchResult?.value
        }

    }
}