package com.melis.anemia_detection

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.melis.anemia_detection.mainRepository.MainViewModel
import com.melis.anemia_detection.ui.theme.AnemiaDetectionTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnemiaDetectionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Test()
                }
            }
        }
    }
}

@Preview
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun Test() {
    val viewModel by remember {
        mutableStateOf(MainViewModel())
    }
    val context = LocalContext.current

    val pickImage =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
            uri?.let { imageUri ->
                viewModel.parseText(context, imageUri, {})
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Button(onClick = { pickImage.launch("image/*") }) {
            Text("Choose Image")
        }
        Spacer(modifier = Modifier.padding(vertical = 16.dp))
        viewModel.refinedValues.joinToString().let { text ->
            Text(text)
        }
        Spacer(modifier = Modifier.padding(vertical = 16.dp))
        if (viewModel.refinedValues.isNotEmpty())
            Button(onClick = { viewModel.checkForAnemia() }) {
                Text("Check for anemia")
            }
        Spacer(modifier = Modifier.padding(vertical = 16.dp))
        Text(text = viewModel.result.value)
        Spacer(modifier = Modifier.padding(vertical = 16.dp))
        Button(onClick = { viewModel.result.value = "" }) {
            Text(text = "Clear response")
        }
    }

}
