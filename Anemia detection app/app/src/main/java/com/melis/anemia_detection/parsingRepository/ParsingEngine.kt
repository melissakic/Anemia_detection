package com.melis.anemia_detection.parsingRepository

import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.melis.anemia_detection.anemiaModels.RefinedValues
import java.util.Vector

class ParsingEngine {

    companion object {
        fun extractAnemiaData(
            text: FirebaseVisionText,
            tolerance: Int
        ): String {
            val hemoglobinLine = Vector<FirebaseVisionText.Line>()
            val mcvLine = Vector<FirebaseVisionText.Line>()

            ParsingHelper.extractRawDataForValues(listOf("MCV"), text, tolerance) {
                mcvLine.add(it)
            }

            ParsingHelper.extractRawDataForValues(listOf("Hemoglobin", "HGB"), text, tolerance) {
                hemoglobinLine.add(it)
            }

            val result = extractAnemiaRefinedData(mcvLine, hemoglobinLine)
            return result.joinToString()
        }


        private fun extractAnemiaRefinedData(
            mcvLines: Vector<FirebaseVisionText.Line>,
            hemoglobinLines: Vector<FirebaseVisionText.Line>
        ): List<RefinedValues> {

            var refinedValues: Vector<RefinedValues> = Vector()

            val mcvLine: FirebaseVisionText.Line? =
                ParsingHelper.extractRefinedDataForValues(listOf("mcv"), mcvLines)

            val hemoglobinLine: FirebaseVisionText.Line? =
                ParsingHelper.extractRefinedDataForValues(
                    listOf("hemoglobin", "hgb"),
                    hemoglobinLines
                )

            if (mcvLine != null && hemoglobinLine != null)
                refinedValues =
                    extractSingularValues(mcvLine, hemoglobinLine, mcvLines, hemoglobinLines)

            return refinedValues
        }


        private fun extractSingularValues(
            mcvLine: FirebaseVisionText.Line,
            hemoglobinLine: FirebaseVisionText.Line,
            mcvLines: Vector<FirebaseVisionText.Line>,
            hemoglobinLines: Vector<FirebaseVisionText.Line>,
        ): Vector<RefinedValues> {

            val extractedSingularValues = Vector<RefinedValues>()

            ParsingHelper.extractSingularDataForValue("MCV", mcvLine, mcvLines) {
                extractedSingularValues.add(it)
            }

            ParsingHelper.extractSingularDataForValue(
                "Hemoglobin",
                hemoglobinLine,
                hemoglobinLines
            ) {
                extractedSingularValues.add(it)
            }

            return extractedSingularValues
        }


    }
}