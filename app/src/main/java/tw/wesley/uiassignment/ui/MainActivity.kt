package tw.wesley.uiassignment.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import tw.wesley.uiassignment.R
import tw.wesley.uiassignment.viewmodel.AirQualityViewModel

@AndroidEntryPoint // For HiltViewModel is used in our AirQualityViewModel
class MainActivity : AppCompatActivity() {

    private val viewModel: AirQualityViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            viewModel.printAirQualityInLog()
        }
    }
}