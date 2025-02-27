package com.example.khadra.presentation.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.khadra.presentation.viewmodel.AddTreeViewModel
import com.example.khadra.ui.theme.KhadraGreen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddScreen(viewModel: AddTreeViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(150.dp))
        OutlinedTextField(
            value = state.name,
            onValueChange = { viewModel.onEvent(AddTreeViewModel.AddTreeEvent.NameChanged(it)) },
            //label = { Text("اسم الشجرة") },
            placeholder = { Text("اسم الشجرة", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())},

            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 18.sp,
                textDirection = TextDirection.Rtl // Align text to the right
            ),shape = RoundedCornerShape(14.dp)
        )


        OutlinedTextField(
            value = state.type,
            onValueChange = { viewModel.onEvent(AddTreeViewModel.AddTreeEvent.TypeSelected(it)) },
            //label = { Text("نوع الشجرة", fontSize = 16.sp) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
            placeholder = { Text("نوع الشجرة", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())},
            textStyle = TextStyle(
                textDirection = TextDirection.Rtl // Forces the cursor to follow the text correctly in RTL
            )
        )


        OutlinedTextField(
            value = state.status,
            onValueChange = { viewModel.onEvent(AddTreeViewModel.AddTreeEvent.StatusSelected(it)) },
            //label = { Text(" حالة الشجرة") },
            modifier = Modifier.fillMaxWidth() ,shape = RoundedCornerShape(14.dp),
            placeholder = { Text("حالة الشجرة", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())},
            textStyle = TextStyle(
                textDirection = TextDirection.Rtl // Forces the cursor to follow the text correctly in RTL
            )

        )

        OutlinedTextField(
            value = state.imageUrl,
            onValueChange = { viewModel.onEvent(AddTreeViewModel.AddTreeEvent.ImageUrlChanged(it)) },
            //label = { Text("رابط صورة الشجرة", textAlign = TextAlign.End) }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),shape = RoundedCornerShape(14.dp),
            placeholder = { Text("رابط صورة الشجرة", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())}


        )

        OutlinedTextField(
            value = state.location,
            onValueChange = {viewModel.onEvent(AddTreeViewModel.AddTreeEvent.LocationChanged(it))},
            //label = { Text("  موقع الشجرة مثال: البياضة") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = false,shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("موقع الشجرة مثال: البياضة", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())},
            textStyle = TextStyle(
                textDirection = TextDirection.Rtl // Forces the cursor to follow the text correctly in RTL
            )

        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.onEvent(AddTreeViewModel.AddTreeEvent.Submit) },
            modifier = Modifier
                .width(300.dp)
                .height(50.dp),
            enabled = !state.isLoading, colors = ButtonColors(containerColor = KhadraGreen, contentColor = Color.White, disabledContentColor = Color.Black, disabledContainerColor = Color.Gray),
            elevation = ButtonDefaults.buttonElevation(6.dp), shape = RoundedCornerShape(14.dp)
        ) {
            Text(if (state.isLoading) "اضافة..." else "غرس", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        if (state.error != null) {
            Text(state.error!!, color = MaterialTheme.colorScheme.error)
        }

        if (state.isSuccess) {
            Text("Tree added successfully!", color = MaterialTheme.colorScheme.primary)
        }
    }
}


