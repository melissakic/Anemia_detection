package com.melis.anemia_detection.parsingRepository

import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.melis.anemia_detection.anemiaModels.RefinedValues
import java.util.Locale
import java.util.Vector
import kotlin.math.abs

sealed class ParsingHelper {

    companion object {

        fun extractRawDataForValues(
            values: List<String>,
            input: FirebaseVisionText,
            tolerance: Int,
            onFindAction: (FirebaseVisionText.Line) -> Unit
        ) {
            input.textBlocks.forEach { block ->
                block.lines.forEach { line ->
                    if (values.any { target ->
                            target.lowercase(Locale.getDefault()) in line.text.lowercase(
                                Locale.getDefault()
                            )
                        }) {
                        
                        val targetTop = line.boundingBox?.top ?: 0
                        val targetBottom = line.boundingBox?.bottom ?: 0

                        input.textBlocks.forEach { innerBlock ->
                            innerBlock.lines.forEach { innerLine ->
                                val topWithinTolerance =
                                    innerLine.boundingBox?.top?.let {
                                        it in (targetTop - tolerance)..(targetTop + tolerance)
                                    } ?: false
                                val bottomWithinTolerance =
                                    innerLine.boundingBox?.bottom?.let {
                                        it in (targetBottom - tolerance)..(targetBottom + tolerance)
                                    } ?: false

                                if (topWithinTolerance || bottomWithinTolerance) {
                                    onFindAction(innerLine)
                                }
                            }
                        }
                    }
                }
            }
        }

        fun extractRefinedDataForValues(
            values: List<String>,
            lines: Vector<FirebaseVisionText.Line>
        ): FirebaseVisionText.Line? {

            var lineData: FirebaseVisionText.Line? = null

            val valueIterator = lines.iterator()
            while (valueIterator.hasNext()) {
                val line = valueIterator.next()
                if (values.any { line.text.contains(it, ignoreCase = true) }) {
                    lineData = line
                    valueIterator.remove()
                    break
                }
            }

            return lineData
        }

        fun extractSingularDataForValue(
            value: String,
            lineData: FirebaseVisionText.Line?,
            allLinesData: Vector<FirebaseVisionText.Line>,
            onSuccessAction: (RefinedValues) -> Unit
        ) {
            val extractedValues = HashMap<FirebaseVisionText.Line, Int>()

            lineData.let { data ->
                allLinesData.forEach {
                    extractedValues[it] = abs(
                        (it.boundingBox?.left ?: 0) - (data?.boundingBox?.right ?: 0)
                    )
                }

                val neededMcvValue = extractedValues.minByOrNull {
                    it.value
                }?.key?.text?.extractNumericPart()?.toDouble() ?: 0.0

                onSuccessAction(RefinedValues(value, neededMcvValue))
            }
        }

        private fun String.extractNumericPart(): String? {
            val regex = Regex("([0-9]+(\\.[0-9]+)?)")
            val matchResult = regex.find(this)
            return matchResult?.value
        }

    }
}