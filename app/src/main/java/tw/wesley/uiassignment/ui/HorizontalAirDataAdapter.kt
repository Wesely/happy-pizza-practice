package tw.wesley.uiassignment.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import tw.wesley.uiassignment.data.local.AirData
import tw.wesley.uiassignment.databinding.ItemHorizontalAirDataBinding

class HorizontalAirDataAdapter(
    private var airDataList: List<AirData>
) : RecyclerView.Adapter<HorizontalAirDataAdapter.HorizontalAirDataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalAirDataViewHolder {
        val binding = ItemHorizontalAirDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HorizontalAirDataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HorizontalAirDataViewHolder, position: Int) {
        val data = airDataList[position]
        holder.binding.apply {
            tvSiteId.text = data.siteId
            tvSiteName.text = data.siteName
            tvStatus.text = data.status
            tvCounty.text = data.county
            tvPM25.text = data.pm25.toString()
        }
    }

    override fun getItemCount(): Int {
        Timber.d("getItemCount/airDataList.size=${airDataList.size}")
        return airDataList.size
    }

    fun updateData(newData: List<AirData>) {
        airDataList = newData
    }

    class HorizontalAirDataViewHolder(val binding: ItemHorizontalAirDataBinding) : RecyclerView.ViewHolder(binding.root)
}
