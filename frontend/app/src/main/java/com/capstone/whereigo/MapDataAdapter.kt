import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.test_map.MapData
import com.example.test_map.R

class MapDataAdapter(
    private val context: Context,
    private val mapList: MutableList<MapData> // 리스트를 MutableList로 변경 (삭제를 위해)
) : RecyclerView.Adapter<MapDataAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.tvFileName)
        val fileSize: TextView = view.findViewById(R.id.tvFileSize)
        val saveDate: TextView = view.findViewById(R.id.tvDate)
        val btnFavorite: ImageButton = view.findViewById(R.id.btnFavorite)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_map_data, parent, false)
        return ViewHolder(view)
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
//                setPositiveButton("삭제") { _, _ ->
//                    // 리스트에서 삭제 후 RecyclerView 갱신
//                    mapList.removeAt(position)
//                    notifyItemRemoved(position)
//                    Toast.makeText(context, "${mapData.fileName}이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
//                }
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

//data 위치 찾는 코드
//holder.btnDelete.setOnClickListener {
//    AlertDialog.Builder(context).apply {
//        setTitle("삭제 확인")
//        setMessage("${mapData.fileName}을(를) 삭제하시겠습니까?")
//        setPositiveButton("삭제") { _, _ ->
//            val index = mapList.indexOfFirst { it.fileName == mapData.fileName }
//            if (index != -1) {
//                mapList.removeAt(index)
//                notifyItemRemoved(index)
//                Toast.makeText(context, "${mapData.fileName}이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
//            }
//        }
//        setNegativeButton("취소", null)
//        show()
//    }
//}