package com.melis.anemia_detection.parsingRepository

import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.melis.anemia_detection.anemiaModels.ParameterValues
import com.melis.anemia_detection.anemiaModels.RefinedValues
import java.util.Vector

class ParsingEngine {

    companion object {
        fun extractAnemiaData(
            text: FirebaseVisionText,
            tolerance: Int
        ): List<RefinedValues> {
            val hemoglobinLine = Vector<FirebaseVisionText.Line>()
            val mcvLine = Vector<FirebaseVisionText.Line>()

            ParsingHelper.extractRawDataForValues(
                listOf(ParameterValues.MCV.value),
                text,
                tolerance
            ) {
                mcvLine.add(it)
            }

            ParsingHelper.extractRawDataForValues(
                listOf(
                    ParameterValues.HEMOGLOBIN_VAR1.value,
                    ParameterValues.HEMOGLOBIN_VAR2.value
                ), text, tolerance
            ) {
                hemoglobinLine.add(it)
            }

            return extractAnemiaRefinedData(mcvLine, hemoglobinLine)
        }


        private fun extractAnemiaRefinedData(
            mcvLines: Vector<FirebaseVisionText.Line>,
            hemoglobinLines: Vector<FirebaseVisionText.Line>
        ): List<RefinedValues> {

            var refinedValues: Vector<RefinedValues> = Vector()

            val mcvLine: FirebaseVisionText.Line? =
                ParsingHelper.extractRefinedDataForValues(
                    listOf(ParameterValues.MCV.value),
                    mcvLines
                )

            val hemoglobinLine: FirebaseVisionText.Line? =
                ParsingHelper.extractRefinedDataForValues(
                    listOf(
                        ParameterValues.HEMOGLOBIN_VAR1.value,
                        ParameterValues.HEMOGLOBIN_VAR2.value
                    ),
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

            ParsingHelper.extractSingularDataForValue(
                ParameterValues.MCV.value,
                mcvLine,
                mcvLines
            ) {
                extractedSingularValues.add(it)
            }

            ParsingHelper.extractSingularDataForValue(
                ParameterValues.HEMOGLOBIN_VAR1.value,
                hemoglobinLine,
                hemoglobinLines
            ) {
                extractedSingularValues.add(it)
            }

            return extractedSingularValues
        }


    }
}