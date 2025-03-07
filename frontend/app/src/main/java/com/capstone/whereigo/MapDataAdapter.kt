package com.capstone.whereigo

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.capstone.whereigo.databinding.ItemMapDataBinding

class MapDataAdapter(
    private val context: Context,
    private val mapList: MutableList<MapData> // 리스트를 MutableList로 변경 (삭제를 위해)
) : RecyclerView.Adapter<MapDataAdapter.ViewHolder>() {

    // ViewHolder에서 ViewBinding을 사용
    class ViewHolder(private val binding: ItemMapDataBinding) : RecyclerView.ViewHolder(binding.root) {
        val fileName = binding.tvFileName
        val fileSize = binding.tvFileSize
        val saveDate = binding.tvDate
        val btnFavorite = binding.btnFavorite
        val btnDelete = binding.btnDelete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMapDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mapData = mapList[position]

        holder.fileName.text = mapData.fileName
        holder.fileSize.text = mapData.fileSize
        holder.saveDate.text = mapData.saveDate

        holder.btnFavorite.setImageResource(
            if (mapData.isFavorite) R.drawable.ic_favorite_selected else R.drawable.ic_favorite
        )

        // 삭제 버튼 클릭 이벤트
        holder.btnDelete.setOnClickListener {
            AlertDialog.Builder(context).apply {
                setTitle("삭제 확인")
                setMessage("${mapData.fileName}을(를) 삭제하시겠습니까?")
                setPositiveButton("삭제") { _, _ ->
                    mapList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, mapList.size)  // 삭제된 이후의 아이템 position을 갱신
                    Toast.makeText(context, "${mapData.fileName}이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
                setNegativeButton("취소", null)
                show()
            }
        }

        // 즐겨찾기 버튼 클릭 이벤트
        holder.btnFavorite.setOnClickListener {
            mapData.isFavorite = !mapData.isFavorite  // 즐겨찾기 상태 변경

            holder.btnFavorite.setImageResource(
                if (mapData.isFavorite) R.drawable.ic_favorite_selected else R.drawable.ic_favorite
            )

            // 토스트 메시지 출력
            val message = if (mapData.isFavorite) {
                "${mapData.fileName}이 즐겨찾기에 추가되었습니다."
            } else {
                "${mapData.fileName}이 즐겨찾기에서 제거되었습니다."
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = mapList.size
}
